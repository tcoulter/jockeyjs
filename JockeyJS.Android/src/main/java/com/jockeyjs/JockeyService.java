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
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.jockeyjs.JockeyHandler.OnCompletedListener;

public class JockeyService extends Service implements Jockey {

	private final IBinder _binder = new JockeyBinder();

	private final JockeyWebViewClient _webViewClient = new JockeyWebViewClient();

	private int messageCount = 0;

	private Map<String, JockeyHandler> _listeners = new HashMap<String, JockeyHandler>();
	private Map<String, JockeyCallback> _callbacks = new HashMap<String, JockeyCallback>();

	private Handler _handler = new Handler();

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

	@Override
	public IBinder onBind(Intent arg0) {
		return _binder;
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
			_callbacks.put(Integer.toString(messageId), complete);
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

	public void triggerCallbackOnWebView(WebView webView, String messageId) {
		String url = String.format("javascript:Jockey.triggerCallback(\"%s\")",
				messageId);
		webView.loadUrl(url);
	}

	private void triggerEventFromWebView(final WebView webView,
			JockeyWebViewPayload envelope) {
		final String messageId = Integer.toString(envelope.id);
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

	private void triggerCallbackForMessage(String messageId) {
		if (_callbacks.containsKey(messageId)) {
			JockeyCallback complete = _callbacks.get(messageId);

			try {
				complete.call();
			} catch (Exception e) {
				e.printStackTrace();
			}

			_callbacks.remove(messageId);
		}
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
			}
			return false;
		}

		private boolean isJockeyScheme(URI uri) {
			return uri.getScheme().equals("jockey")
					&& !uri.getQuery().equals("");
		}

		private void processUri(WebView view, URI uri) {
			String[] parts = uri.getPath().replaceAll("^\\/", "").split("/");
			String host = uri.getHost();
			Gson gson = new Gson();

			JockeyWebViewPayload payload = gson.fromJson(uri.getQuery(),
					JockeyWebViewPayload.class);

			if (parts.length > 0) {
				if (host.equals("event")) {
					JockeyService.this.triggerEventFromWebView(view, payload);
				} else if (host.equals("callback")) {
					JockeyService.this.triggerCallbackForMessage(parts[0]);
				}
			}
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void configure(WebView webView) {
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(this._webViewClient);
	}

	@Override
	public boolean handles(String eventName) {
		return _listeners.containsKey(eventName);
	}

}
