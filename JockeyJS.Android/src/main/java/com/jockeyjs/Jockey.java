package com.jockeyjs;

import android.webkit.WebView;

/**
 * The primary interface for communicating between a WebView and the local android activity.
 * 
 * @author Paul
 *
 */
public interface Jockey {

	/**
	 * An interface for the app to check the incoming source of an event.
	 * 
	 * @author Paul
	 *
	 */
	public interface OnValidateListener {
		void validate(String host) throws HostValidationException;
	}

	/**
	 * Attaches a handler to this jockey instance so that is may receive events of the provided type
	 * from a webpage
	 * 
	 * @param type
	 * @param handler
	 */
	public void on(String type, JockeyHandler handler);
	
	/**
	 * Removes a handler of the specified name
	 * 
	 * @param type
	 */
	public void off(String type);
	
	
	/**
	 * Sends a new event to the webview
	 * 
	 * Equivalent to calling send(type, toWebView, null, null);
	 * 
	 * @param type
	 * @param toWebView
	 */
	public void send(String type, WebView toWebView);
	
	
	/**
	 * Sends a new event to the webview with the included payload
	 * 
	 * Equivalent to calling send(type, toWebView, payload, null)
	 * 
	 * @param type
	 * @param toWebView
	 * @param withPayload
	 */
	public void send(String type, WebView toWebView, Object withPayload);
	
	
	/**
	 * Sends the new event to the webview and registers a callback to listen for the returned value
	 * 
	 * Equivalent to calling send(type, toWebView, null, complete)
	 * 
	 * @param type
	 * @param toWebView
	 * @param complete
	 */
	public void send(String type, WebView toWebView, JockeyCallback complete);
	
	
	/**
	 * Sends the new event to the webview with a payload and a callback to listen for the webpage response.
	 * 
	 * @param type
	 * @param toWebView
	 * @param withPayload
	 * @param complete
	 */
	public void send(String type, WebView toWebView, Object withPayload, JockeyCallback complete);
	
	/**
	 * Triggers the callback on the webview with the appropriate messageId
	 * 
	 * @param webView
	 * @param messageId
	 */
	public void triggerCallbackOnWebView(WebView webView, int messageId);
	
	/**
	 * Configures the WebView to be able to receive and send events with Jockey
	 * 
	 * @param webView
	 */
	public void configure(WebView webView);

	/**
	 * Returns if the Jockey implementation handles the event
	 * 
	 * @param string
	 * @return
	 */
	public boolean handles(String string);
	
	public void setOnValidateListener(OnValidateListener listener);
	
}
