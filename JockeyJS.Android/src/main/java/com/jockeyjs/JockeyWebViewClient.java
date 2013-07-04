package com.jockeyjs;

import java.net.URI;
import java.net.URISyntaxException;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

@SuppressLint("SetJavaScriptEnabled")
public class JockeyWebViewClient extends WebViewClient {
	
	private JockeyImpl _jockeyImpl;
	
	public JockeyWebViewClient(JockeyImpl jockey) {
		_jockeyImpl = jockey;
	}
	
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
	
	public JockeyWebViewPayload checkPayload(JockeyWebViewPayload fromJson) throws HostValidationException {
		validateHost(fromJson.host);
		return fromJson;
	}
	
	private void validateHost(String host) throws HostValidationException {
		_jockeyImpl.validate(host);
	}
}