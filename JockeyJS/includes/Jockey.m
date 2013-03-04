//
//  Jockey.m
//  JockeyJS
//
//  Created by Tim Coulter on 3/3/13.
//  Copyright (c) 2013 Corkboardme. All rights reserved.
//

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
    
    [self on:type performExtended:extended];
}

+ (void)on:(NSString *)type performExtended:(JockeyExtendedHandler)handler
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
    
    for (JockeyExtendedHandler handler in listenerList) {
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
