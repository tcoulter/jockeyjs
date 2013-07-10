package com.jockeyjs;

import java.net.URI;
import java.net.URISyntaxException;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.jockeyjs.util.ForwardingWebViewClient;

@SuppressLint("SetJavaScriptEnabled")
class JockeyWebViewClient extends ForwardingWebViewClient {

	private JockeyImpl _jockeyImpl;
	private WebViewClient _delegate;
	private Gson _gson;

	public JockeyWebViewClient(JockeyImpl jockey) {
		_gson = new Gson();
		_jockeyImpl = jockey;
	}

	public JockeyImpl getImplementation() {
		return _jockeyImpl;
	}

	protected void setDelegate(WebViewClient client) {
		_delegate = client;
	}

	public WebViewClient delegate() {
		return _delegate;
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
	
		if (delegate() != null
				&& delegate().shouldOverrideUrlLoading(view, url))
			return true;
	
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

	public boolean isJockeyScheme(URI uri) {
		return uri.getScheme().equals("jockey") && !uri.getQuery().equals("");
	}

	public void processUri(WebView view, URI uri)
			throws HostValidationException {
		String[] parts = uri.getPath().replaceAll("^\\/", "").split("/");
		String host = uri.getHost();

		JockeyWebViewPayload payload = checkPayload(_gson.fromJson(
				uri.getQuery(), JockeyWebViewPayload.class));

		if (parts.length > 0) {
			if (host.equals("event")) {
				getImplementation().triggerEventFromWebView(view, payload);
			} else if (host.equals("callback")) {
				getImplementation().triggerCallbackForMessage(
						Integer.parseInt(parts[0]));
			}
		}
	}

	public JockeyWebViewPayload checkPayload(JockeyWebViewPayload fromJson)
			throws HostValidationException {
		validateHost(fromJson.host);
		return fromJson;
	}

	private void validateHost(String host) throws HostValidationException {
		getImplementation().validate(host);
	}

}