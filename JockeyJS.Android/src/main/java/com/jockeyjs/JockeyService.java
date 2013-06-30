/*******************************************************************************
 * Copyright (c) 2013,  Paul Daniels
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.jockeyjs;

import java.net.URI;
import java.net.URISyntaxException;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

public class JockeyService extends Service implements Jockey {

	private final IBinder _binder = new JockeyBinder();
	
	private JockeyImpl _jockeyImpl = JockeyImpl.getDefault();

	private final JockeyWebViewClient _webViewClient = new JockeyWebViewClient();

	/**
	 * Convenience method for binding to the JockeyService
	 * 
	 * @param context
	 * @param connection
	 */
	public static boolean bind(Context context, ServiceConnection connection) {
		return context.bindService(new Intent(context, JockeyService.class),
				connection, Context.BIND_AUTO_CREATE);
	}

	public static void unbind(Context context, ServiceConnection connection) {
		context.unbindService(connection);
	}

	protected WebViewClient getWebViewClient() {
		return _webViewClient;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return _binder;
	}

	public class JockeyBinder extends Binder {
	
		public Jockey getService() {
			return JockeyService.this;
		}
	}

	@Override
	public void setOnValidateListener(OnValidateListener listener) {
		_jockeyImpl.setOnValidateListener(listener);
	}

	public void on(String type, JockeyHandler handler) {
		_jockeyImpl.on(type, handler);
	}

	@Override
	public void off(String type) {
		_jockeyImpl.off(type);
	}

	public void send(String type, WebView toWebView) {
		send(type, toWebView, null);
	}

	public void send(String type, WebView toWebView, Object withPayload) {
		send(type, toWebView, withPayload, null);
	}

	public void send(String type, WebView toWebView, JockeyCallback complete) {
		send(type, toWebView, null, complete);
	}

	public void send(String type, WebView toWebView, Object withPayload,
			JockeyCallback complete) {
		_jockeyImpl.send(type, toWebView, withPayload, complete);
	}

	public void triggerCallbackOnWebView(WebView webView, int messageId) {
		String url = String.format("javascript:Jockey.triggerCallback(\"%d\")",
				messageId);
		webView.loadUrl(url);
	}

	@SuppressLint("SetJavaScriptEnabled")
	public class JockeyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			try {
				URI uri = new URI(url);

				if (isJockeyScheme(uri)) {
					processUri(view, uri);
					return true;
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (HostValidationException e) {
				e.printStackTrace();
				Log.e("Jockey", "The source of the event could not be validated!");
			}
			return false;
		}

		private boolean isJockeyScheme(URI uri) {
			return uri.getScheme().equals("jockey")
					&& !uri.getQuery().equals("");
		}

		private void processUri(WebView view, URI uri) throws HostValidationException {
			String[] parts = uri.getPath().replaceAll("^\\/", "").split("/");
			String host = uri.getHost();
			Gson gson = new Gson();

			JockeyWebViewPayload payload = checkPayload(gson.fromJson(
					uri.getQuery(), JockeyWebViewPayload.class));

			if (parts.length > 0) {
				if (host.equals("event")) {
					_jockeyImpl.triggerEventFromWebView(view, payload);
				} else if (host.equals("callback")) {
					_jockeyImpl.triggerCallbackForMessage(Integer
							.parseInt(parts[0]));
				}
			}
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void configure(WebView webView) {
		_jockeyImpl.configure(webView);
		webView.setWebViewClient(this.getWebViewClient());
	}

	public JockeyWebViewPayload checkPayload(JockeyWebViewPayload fromJson) throws HostValidationException {
		validateHost(fromJson.host);
		return fromJson;
	}
	
	

	private void validateHost(String host) throws HostValidationException {
		_jockeyImpl.validate(host);
	}

	@Override
	public boolean handles(String eventName) {
		return _jockeyImpl.handles(eventName);
	}

}
