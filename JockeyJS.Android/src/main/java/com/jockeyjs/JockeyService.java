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
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.jockeyjs.JockeyHandler.OnCompletedListener;

public class JockeyService extends Service implements Jockey {

	private final IBinder _binder = new JockeyBinder();

	private final JockeyWebViewClient _webViewClient = new JockeyWebViewClient();

	private int messageCount = 0;

	private Map<String, JockeyHandler> _listeners = new HashMap<String, JockeyHandler>();
	private SparseArray<JockeyCallback> _callbacks = new SparseArray<JockeyCallback>();

	private Handler _handler = new Handler();

	private OnValidateListener _onValidateListener;

	//A default Callback that does nothing.
	private static final JockeyCallback _DEFAULT = new JockeyCallback() {
		@Override
		public void call() {
		}
	};

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

	@Override
	public void setOnValidateListener(OnValidateListener listener) {
		_onValidateListener = listener;
	}

	public class JockeyBinder extends Binder {

		public Jockey getService() {
			return JockeyService.this;
		}
	}

	public void on(String type, JockeyHandler handler) {
		_listeners.put(type, handler);
	}

	@Override
	public void off(String type) {
		_listeners.remove(type);
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
		int messageId = messageCount;

		if (complete != null) {
			_callbacks.put(messageId, complete);
		}

		if (withPayload != null) {
			Gson gson = new Gson();
			withPayload = gson.toJson(withPayload);
		}

		String url = String.format("javascript:Jockey.trigger(\"%s\", %d, %s)",
				type, messageId, withPayload);
		toWebView.loadUrl(url);

		++messageCount;
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
					JockeyService.this.triggerEventFromWebView(view, payload);
				} else if (host.equals("callback")) {
					JockeyService.this.triggerCallbackForMessage(Integer
							.parseInt(parts[0]));
				}
			}
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void configure(WebView webView) {
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(this.getWebViewClient());
	}

	public JockeyWebViewPayload checkPayload(JockeyWebViewPayload fromJson) throws HostValidationException {
		validateHost(fromJson.host);
		return fromJson;
	}
	
	

	private void validateHost(String host) throws HostValidationException {
		if (_onValidateListener != null)
			_onValidateListener.validate(host);
	}

	@Override
	public boolean handles(String eventName) {
		return _listeners.containsKey(eventName);
	}

	private void triggerEventFromWebView(final WebView webView,
			JockeyWebViewPayload envelope) {
		final int messageId = envelope.id;
		String type = envelope.type;

		if (this.handles(type)) {
			JockeyHandler handler = _listeners.get(type);

			handler.perform(envelope.payload, new OnCompletedListener() {
				@Override
				public void onCompleted() {
					_handler.post(new Runnable() {
						@Override
						public void run() {
							triggerCallbackOnWebView(webView, messageId);
						}
					});
				}
			});
		}
	}

	private void triggerCallbackForMessage(int messageId) {
			try {
				JockeyCallback complete = _callbacks.get(messageId, _DEFAULT);
				complete.call();
			} catch (Exception e) {
				e.printStackTrace();
			}
			_callbacks.remove(messageId);
	}

}
