//
//  JockeyViewController.h
//  JockeyJS
//
//  Created by Tim Coulter on 3/3/13.
//  Copyright (c) 2013 Corkboardme. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface JockeyViewController : UIViewController <UIWebViewDelegate>

@property (weak, nonatomic) IBOutlet UIWebView *webView;

- (IBAction)colorButtonPressed:(UIBarButtonItem *)sender;
- (IBAction)refreshButtonPressed:(UIBarButtonItem *)sender;
- (IBAction)showImageButtonPressed:(UIBarButtonItem *)sender;

- (void)refresh;
- (void)toggleFullscreen:(void(^)())complete withDuration:(NSTimeInterval)duration;
- (void)enterFullscreen:(void(^)())complete withDuration:(NSTimeInterval)duration;
- (void)exitFullscreen:(void(^)())complete withDuration:(NSTimeInterval)duration;

@property (weak, nonatomic) IBOutlet UIToolbar *topToolbar;
@property (weak, nonatomic) IBOutlet UIToolbar *bottomToolbar;

@property (nonatomic, assign) BOOL fullscreenToggled;

@end
