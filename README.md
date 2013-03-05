JockeyJS
========

JockeyJS is an iOS and JS library to facilitate two-way communication between iOS apps and Javascript apps running inside them. 

There's a desire for Android support and the Javascript dispatcher has been stubbed out. If you can contribute, please do!

Setup
-----

JockeyJS will help your iOS app communicate with a Javascript application running inside a UIWebview.

1. Download the latest JockeyJS into your iOS project directory.
1. Add `JockeyJS/includes/Jockey.m` and `Jockey.h` to your project by right clicking inside XCode's Project Navigator and selecting "Add Files to \<YourProject\>"
1. In your web app, make sure to include `JockeyJS/js/jockey.js` as a script tag. 
1. Last, set your ViewController (`JockeyViewController` in the example code) as the delegate of your UIWebView, then add the following code to your ViewController's `.m` file:
   
```objective-c
-(BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
	return [Jockey webView:_webView withUrl:[request URL]];
}
```

You're all set! Now you can start passing events. 

Sending events from iOS to Javascript
-------------------------------------
JockeyJS allows you to not only send events to the JavaScript application, but you can also receive a callback in the form of a block when all JavaScript listeners have finished executing. There are two methods avaiable:

```objective-c
// Send an event to Javascript, passing a payload. 
// payload can be an NSDictionary or NSArray, or anything that is serializable to JSON.
// It can be nil.
[Jockey send:@"event-name" withPayload:payload toWebView:webView];

// If you want to send an event and also execute code within the iOS app when all
// Javascript listeners have finished processing. 
[Jockey send:@"event-name" withPayload:payload toWebView:webView perform:^{
  // Respond to callback.
}];
```

Receiving events from iOS in Javascript
---------------------------------------
Event listeners in Jockey are model after JQuery's event listeners (not far less featureful). To receive the above events in Javascript, simply add the following to your Javascript application:

```javascript
// Listen for an event from iOS and log the payload.
Jockey.on("event-name", function(payload) {
  console.log(payload);
});
```

You can also pass a slightly different function to `on()` in cases where your listener fires off other events and you don't want to send a callback to iOS until those events are completed. e.g.,

```javascript
// Listen for an event from iOS, but don't notify iOS we've completed processing
// until an event has been run (in this case a timeout).
Jockey.on("event-name", function(payload, complete) {
  // Example of event'ed handler.
  setTimeout(function() {
    alert("Timeout over!");
    complete();
  }, 1000);
  
  // Note: You MUST return false in this case to tell Jockey you plan on calling
  // the complete function on your own once events have finished.
  return false;
});
```

Sending events from Javascript to iOS
-------------------------------------
Similar to iOS above, Jockey's Javascript library has similar methods to help you pass events from your Javascript application to your iOS app.

```javascript
// Send an event to iOS.
Jockey.send("event-name");

// Send an event to iOS, passing an optional payload. 
Jockey.send("event-name", {
  key: "value"
});

// Send an event to iOS, pass an optional payload, and catch the callback when all the 
// iOS listeners have finished processing.
Jockey.send("event-name", {
  key: "value"
}, function() {
  alert("iOS has finished processing!");
});
```

Receiving events from Javascript in iOS
---------------------------------------
Like Javascript above, Jockey's iOS library has methods to easily help you listen for events sent from your Javascript application:

```objective-c

// Listen for an event from Javascript and log the payload.
[Jockey on:@"event-name" perform:^(NSDictionary *payload) {
  NSLog(@"payload = %@", payload);
}];

// Listen for an event from Javascript, but don't notify the Javascript that 
// the listener has completed until another function has finished. This call
// is taken from the example app in the repo. 
[Jockey on:@"toggle-fullscreen-with-callback" performWithCallback:^(NSDictionary *payload, void (^complete)()) {
  [self toggleFullscreen:complete withDuration:duration];
}];
```


