//
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

function log(msg) {
	var writeMessage = msg;
	if(typeof msg != "string") {
		writeMessage = "<ul>";
		for(var x in msg) {
			writeMessage += "<li>" + x + "-" + msg[x] + "</li>";
		}
		writeMessage += "</ul>";
	}
	$("#console").append("<li>" + writeMessage + "</li>");
}

;(function () {

// Non-accessible variable to send to the app, to ensure events only
// come from the desired host.
var host = window.location.host;

var Dispatcher = {
    callbacks: {},

    send: function(envelope, complete) {
        this.dispatchMessage("event", envelope, complete);
    },

    sendCallback: function(messageId) {
        var envelope = Jockey.createEnvelope(messageId);

        this.dispatchMessage("callback", envelope, function() {});
    },

    triggerCallback: function(id) {
        var dispatcher = this;

        // Alerts within JS callbacks will sometimes freeze the iOS app.
        // Let's wrap the callback in a timeout to prevent this.
        setTimeout(function() {
            dispatcher.callbacks[id]();
        }, 0);
    },

    // `type` can either be "event" or "callback"
    dispatchMessage: function(type, envelope, complete) {
        // We send the message by navigating the browser to a special URL.
        // The iOS library will catch the navigation, prevent the UIWebView
        // from continuing, and use the data in the URL to execute code
        // within the iOS app.

        var dispatcher = this;

        this.callbacks[envelope.id] = function() {
            complete();

            delete dispatcher.callbacks[envelope.id];
        };

        window.location.href = "jockey://" + type + "/" + envelope.id + "?" + encodeURIComponent(JSON.stringify(envelope));
    }
};



var Jockey = {
    listeners: {},

    dispatcher: null,

    messageCount: 0,

    on: function(type, fn) {
        if (!this.listeners.hasOwnProperty(type) || !this.listeners[type] instanceof Array) {
            this.listeners[type] = [];
        }

        this.listeners[type].push(fn);
    },

    off: function(type) {
        if (!this.listeners.hasOwnProperty(type) || !this.listeners[type] instanceof Array) {
            this.listeners[type] = [];
        }

        this.listeners[type] = [];
    },

    send: function(type, payload, complete) {
        if (payload instanceof Function) {
            complete = payload;
            payload = null;
        }

        payload = payload || {};
        complete = complete || function() {};

        var envelope = this.createEnvelope(this.messageCount, type, payload);

        this.dispatcher.send(envelope, complete);

        this.messageCount += 1;
    },

    // Called by the native application when events are sent to JS from the app.
    // Will execute every function, FIFO order, that was attached to this event type.
    trigger: function(type, messageId, json) {
        var self = this;

        var listenerList = this.listeners[type] || [];

        var executedCount = 0;

        var complete = function() {
            executedCount += 1;

            if (executedCount >= listenerList.length) {
                self.dispatcher.sendCallback(messageId);
            }
        };

        for (var index = 0; index < listenerList.length; index++) {
            var listener = listenerList[index];

            // If it's a "sync" listener, we'll call the complete() function
            // after it has finished. If it's async, we expect it to call complete().
            if (listener.length <= 1) {
                listener(json);
                complete();
            } else {
                listener(json, complete);
            }
        }

    },

    // Called by the native application in response to an event sent to it.
    // This will trigger the callback passed to the send() function for
    // a given message.
    triggerCallback: function(id) {
        this.dispatcher.triggerCallback(id);
    },

    createEnvelope: function(id, type, payload) {
        return {
            id: id,
            type: type,
            host: host,
            payload: payload
        };
    }
};

// i.e., on a Desktop browser.
var nullDispatcher = {
    send: function() {},
    triggerCallback: function() {},
    sendCallback: function() {}
};

// Dispatcher detection. Currently only supports iOS.
// Looking for equivalent Android implementation.
var i = 0,
    iOS = false,
    iDevice = ['iPad', 'iPhone', 'iPod'];

for (; i < iDevice.length; i++) {
    if (navigator.platform.indexOf(iDevice[i]) >= 0) {
        iOS = true;
        break;
    }
}

// Detect UIWebview. In Mobile Safari proper, jockey urls cause a popup to
// be shown that says "Safari cannot open page because the URL is invalid."
// From here: http://stackoverflow.com/questions/4460205/detect-ipad-iphone-webview-via-javascript

var UIWebView = /(iPhone|iPod|iPad).*AppleWebKit(?!.*Safari)/i.test(navigator.userAgent);
var isAndroid = navigator.userAgent.toLowerCase().indexOf("android") > -1;

if ((iOS && UIWebView) || isAndroid) { 
    Jockey.dispatcher = Dispatcher;
} else {
    Jockey.dispatcher = nullDispatcher;
}

window.Jockey = Jockey;
})();