//
//  Jockey.h
//  JockeyJS
//
//  Copyright (c) 2013, Tim Coulter
//
//  Permission is hereby granted, free of charge, to any person obtaining
//  a copy of this software and associated documentation files (the
//  "Software"), to deal in the Software without restriction, including
//  without limitation the rights to use, copy, modify, merge, publish,
//  distribute, sublicense, and/or sell copies of the Software, and to
//  permit persons to whom the Software is furnished to do so, subject to
//  the following conditions:
//
//  The above copyright notice and this permission notice shall be
//  included in all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
//  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
//  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
//  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
//  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
//  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
//  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

#import <Foundation/Foundation.h>

typedef void (^ JockeyHandler)(NSDictionary *payload);
typedef void (^ JockeyAsyncHandler)(UIWebView *webView, NSDictionary *payload, void (^complete)());

@interface Jockey : NSObject

+ (void)on:(NSString*)type perform:(JockeyHandler)handler;
+ (void)on:(NSString*)type performAsync:(JockeyAsyncHandler)handler;
+ (void)off:(NSString *)type;
+ (void)send:(NSString*)type withPayload:(id)payload toWebView:(UIWebView*)webView;
+ (void)send:(NSString *)type withPayload:(id)payload toWebView:(UIWebView *)webView perform:(void(^)())complete;

+ (BOOL)webView:(UIWebView*)webView withUrl:(NSURL*)url;

@property (strong, atomic) NSNumber *messageCount;
@property (strong, nonatomic) NSMutableDictionary *listeners;
@property (strong, nonatomic) NSMutableDictionary *callbacks;

@end
