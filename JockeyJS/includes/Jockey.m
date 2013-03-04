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
            jockey.listeners = [[NSMutableDictionary alloc] init];
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
    NSError *err;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:payload options:NSJSONWritingPrettyPrinted error:&err];
    
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    NSString *javascript = [NSString stringWithFormat:@"Jockey.trigger(\"%@\", %@);", type, jsonString];
    
    [webView stringByEvaluatingJavaScriptFromString:javascript];
}

+ (BOOL)webView:(UIWebView*)webView withUrl:(NSURL*)url
{
    if ( [[url scheme] isEqualToString:@"jockey"] )
    {
        NSString *query = [url query];
        NSString *jsonString = [query stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        
        NSError *error;
        NSDictionary *JSON = [NSJSONSerialization JSONObjectWithData: [jsonString dataUsingEncoding:NSUTF8StringEncoding]
                                                             options: NSJSONReadingMutableContainers
                                                               error: &error];
        
        [[self getInstance] triggerEventFromWebView:webView withData:JSON];
        
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

@end
