//
//  main.m
//  JockeyJS
//
//  Created by Tim Coulter on 3/3/13.
//  Copyright (c) 2013 Corkboardme. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "JockeyAppDelegate.h"

int main(int argc, char *argv[])
{
    int retVal;
    
    @autoreleasepool {
        @try {
            retVal = UIApplicationMain(argc, argv, nil, NSStringFromClass([JockeyAppDelegate class]));
        }
        @catch (NSException *exception) {
            NSLog(@"CRASH: %@", exception);
            NSLog(@"Stack Trace: %@", [exception callStackSymbols]);
            
        }
    }
    
    return retVal;
}
