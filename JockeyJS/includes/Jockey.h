//
//  Jockey.h
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

#import <Foundation/Foundation.h>

typedef void (^ JockeyHandler)(NSDictionary *payload);
typedef void (^ JockeyAsyncHandler)(NSDictionary *payload, void (^complete)());

@interface Jockey : NSObject

+ (void)on:(NSString*)type perform:(JockeyHandler)handler;
+ (void)on:(NSString*)type performAsync:(JockeyAsyncHandler)handler;
+ (void)send:(NSString*)type withPayload:(id)payload toWebView:(UIWebView*)webView;
+ (void)send:(NSString *)type withPayload:(id)payload toWebView:(UIWebView *)webView perform:(void(^)())complete;

+ (BOOL)webView:(UIWebView*)webView withUrl:(NSURL*)url;

// Internal

+ (Jockey*)getInstance;

- (void)triggerEventFromWebView:(UIWebView*)webView withData:(NSDictionary*)envelope;
- (void)triggerCallbackOnWebView:(UIWebView*)webView forMessage:(NSString*)messageId;
- (void)triggerCallbackForMessage:(NSNumber*)messageId;

@property (strong, atomic) NSNumber *messageCount;
@property (strong, nonatomic) NSMutableDictionary *listeners;
@property (strong, nonatomic) NSMutableDictionary *callbacks;

@end
