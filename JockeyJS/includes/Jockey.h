//
//  Jockey.h
//  JockeyJS
//
//  Created by Tim Coulter on 3/3/13.
//  Copyright (c) 2013 Corkboardme. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef void (^ JockeyHandler)(NSDictionary *payload);
typedef void (^ JockeyExtendedHandler)(NSDictionary *payload, void (^complete)());

@interface Jockey : NSObject

+ (void)on:(NSString*)type perform:(JockeyHandler)handler;
+ (void)on:(NSString*)type performExtended:(JockeyExtendedHandler)handler;
+ (void)send:(NSString*)type withPayload:(id)payload toWebView:(UIWebView*)webView;
+ (void)send:(NSString *)type withPayload:(id)payload toWebView:(UIWebView *)webView perform:(void(^)())complete;

+ (BOOL)webView:(UIWebView*)webView withUrl:(NSURL*)url;

// Internal

+ (Jockey*)getInstance;

- (void)triggerEventFromWebView:(UIWebView*)webView withData:(NSDictionary*)envelope;
- (void)triggerCallbackOnWebView:(UIWebView*)webView forMessage:(NSString*)messageId;
- (void)triggerCallbackForMessage:(NSNumber*)messageId;

@property (nonatomic, assign) NSNumber *messageCount;
@property (strong, nonatomic) NSMutableDictionary *listeners;
@property (strong, nonatomic) NSMutableDictionary *callbacks;

@end
