//
//  Jockey.m
//  JockeyJS
//
//  Copyright (c) 2013, Tim Coulter
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//  (1) Redistributions of source code must retain the above copyright
//  notice, this list of conditions and the following disclaimer.
//
//  (2) Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in
//  the documentation and/or other materials provided with the
//  distribution.
//
//  (3)The name of the author may not be used to
//  endorse or promote products derived from this software without
//  specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
//  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//  DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
//  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
//  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
//  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
//  IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//  POSSIBILITY OF SUCH DAMAGE.

#import "Jockey.h"

@implementation Jockey

+ (Jockey*)getInstance
{
    static Jockey *jockey;
    
    @synchronized(self) {
        if (!jockey) {
            jockey = [Jockey alloc];
            jockey.messageCount = [NSNumber numberWithInteger:0];
            jockey.listeners = [[NSMutableDictionary alloc] init];
            jockey.callbacks = [[NSMutableDictionary alloc] init];
        }
    }
    
    return jockey;
}

+ (void)on:(NSString*)type perform:(JockeyHandler)handler
{
    void (^ extended)(NSDictionary *payload, void (^ complete)()) = ^(NSDictionary *payload, void(^ complete)()) {
        handler(payload);
        complete();
    };
    
    [self on:type performAsync:extended];
}

+ (void)on:(NSString *)type performAsync:(JockeyAsyncHandler)handler
{
    Jockey *instance = [Jockey getInstance];
    
    NSDictionary *listeners = [[Jockey getInstance] listeners];
    
    NSMutableArray *listenerList = [listeners objectForKey:type];
    
    if (listenerList == nil) {
        listenerList = [[NSMutableArray alloc] init];
        
        [instance.listeners setValue:listenerList forKey:type];
    }
    
    [listenerList addObject:handler];
}

+ (void)send:(NSString *)type withPayload:(id)payload toWebView:(UIWebView *)webView
{
    [self send:type withPayload:payload toWebView:webView perform:nil];
}

+ (void)send:(NSString *)type withPayload:(id)payload toWebView:(UIWebView *)webView perform:(void (^)())complete {
    Jockey *jockey = [Jockey getInstance];
    
    NSNumber *messageId = jockey.messageCount;
    
    if (complete != nil) {
        [jockey.callbacks setValue:complete forKey:[messageId stringValue]];
    }
    
    NSError *err;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:payload options:NSJSONWritingPrettyPrinted error:&err];
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *javascript = [NSString stringWithFormat:@"Jockey.trigger(\"%@\", %i, %@);", type, [messageId integerValue], jsonString];
    
    [webView stringByEvaluatingJavaScriptFromString:javascript];
    
    jockey.messageCount = [[NSNumber alloc] initWithInteger:[jockey.messageCount integerValue] + 1];
}

+ (BOOL)webView:(UIWebView*)webView withUrl:(NSURL*)url
{
    if ( [[url scheme] isEqualToString:@"jockey"] )
    {
        NSString *eventType = [url host];
        NSString *messageId = [[url path] substringFromIndex:1];
        NSString *query = [url query];
        NSString *jsonString = [query stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        
        NSError *error;
        NSDictionary *JSON = [NSJSONSerialization JSONObjectWithData: [jsonString dataUsingEncoding:NSUTF8StringEncoding]
                                                             options: NSJSONReadingMutableContainers
                                                               error: &error];
        
        if ([eventType isEqualToString:@"event"]) {
            [[self getInstance] triggerEventFromWebView:webView withData:JSON];
        } else if ([eventType isEqualToString:@"callback"]) {
            [[self getInstance] triggerCallbackForMessage:[NSNumber numberWithInteger:[messageId integerValue]]];
        }
        
        return NO;
    }
    return YES;
}

- (void)triggerEventFromWebView:(UIWebView*)webView withData:(NSDictionary*)envelope
{
    NSDictionary *listeners = [[Jockey getInstance] listeners];
    
    NSString *messageId = [envelope objectForKey:@"id"];
    NSString *type = [envelope objectForKey:@"type"];
    //NSString *host = [envelope objectForKey:@"host"];
    
    NSDictionary *payload = [envelope objectForKey:@"payload"];
    
    NSArray *listenerList = (NSArray*)[listeners objectForKey:type];

    __block NSInteger executedCount = 0;
    
    void (^complete)() = ^() {
        executedCount += 1;
        
        if (executedCount >= [listenerList count]) {
            [[Jockey getInstance] triggerCallbackOnWebView:webView forMessage:messageId];
        }
    };
    
    for (JockeyAsyncHandler handler in listenerList) {
        handler(payload, complete);
    }
}

- (void)triggerCallbackOnWebView:(UIWebView*)webView forMessage:(NSString*)messageId
{
    NSString *javascript = [NSString stringWithFormat:@"Jockey.triggerCallback(\"%@\");", messageId];
    
    [webView stringByEvaluatingJavaScriptFromString:javascript];
}

- (void)triggerCallbackForMessage:(NSNumber *)messageId {
    NSString *messageIdString = [messageId stringValue];
    
    void (^ callback)() = [_callbacks objectForKey:messageIdString];
    
    if (callback != nil) {
        callback();
    }
    
    [_callbacks removeObjectForKey:messageIdString];
}

@end
