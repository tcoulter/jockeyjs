;(function () {

	// Non-accessible variable to send to the app, to ensure events only
	// come from the desired host.
	var host = window.location.host;

	var iOSDispatcher = {
		callbacks: {},
	
		send: function(envelope, complete) {
			// We send the message by navigating the browser to a special URL.
			// The iOS library will catch the navigation, prevent the UIWebView
			// from continuing, and use the data in the URL to execute code
			// within the iOS app.
			
			var dispatcher = this;
			
			this.callbacks[envelope.id] = function() {
				complete();
				
				delete dispatcher.callbacks[envelope.id];
			};
			
			window.location.href = "jockey://" + envelope.id + "/?" + encodeURIComponent(JSON.stringify(envelope));
		},
		
		triggerCallback: function(id) {
			var dispatcher = this;
		
			// Alerts within JS callbacks will sometimes freeze the iOS app.
			// Let's wrap the callback in a timeout to prevent this.
			setTimeout(function() {
				dispatcher.callbacks[id]();
			}, 0);
		}
	};
	
	var AndroidDispatcher = {
		// Needs to be implemented. Any takers? 
		send: function(envelope, complete) {
			alert("JockeyJS's Android dispatcher and native listener aren't yet implemented. Any takers?");
		},
		
		triggerCallback: function(id) {
			// Needs to be implemented.
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
		
		send: function(type, payload, complete) {
			if (payload instanceof Function) {
				complete = payload;
				payload = null;
			}
			
			payload = payload || {};	
			complete = complete || function() {};
			
			var envelope = {
				id: this.messageCount,
				type: type,
				host: host, 
				payload: payload
			};	
			
			this.dispatcher.send(envelope, complete);
			
			this.messageCount += 1;
		},
		
		// Called by the native application when events are sent to JS from the app.
		// Will execute every function, FIFO order, that was attached to this event type.
		trigger: function(type, json) {
			var listenerList = this.listeners[type] || [];
			
			var executedCount = 0;
			
			var complete = function() {
				executedCount += 1;
				
				if (executedCount >= listenerList.length) {
					// Trigger callback;
				}
			};
			
			for (var index = 0; index < listenerList.length; index++) {
				var listener = listenerList[index];
				
				var returnVal = listener(json, complete);
				
				// Allow the listener to return false to signify that 
				// the handler will call the complete function later
				// (perhaps, after a long running function or event).
				if (returnVal !== false) {
					complete();
				}
			}
			
		},
		
		// Called by the native application in response to an event sent to it. 
		// This will trigger the callback passed to the send() function for
		// a given message.
		triggerCallback: function(id) {
			this.dispatcher.triggerCallback(id);
		}
	};
	
	// Dispatcher detection. Currently only supports iOS.
	// Looking for equivalent Android implementation. 
	Jockey.dispatcher = iOSDispatcher;
	
	Jockey.iOSDispatcher = iOSDispatcher;
	Jockey.AndroidDispatcher = AndroidDispatcher;
	
	window.Jockey = Jockey;
})();