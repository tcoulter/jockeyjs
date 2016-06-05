package com.jockeyjs.util;
import android.net.http.SslError;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

public abstract class ForwardingWebViewClient extends XWalkResourceClient {

  public ForwardingWebViewClient (XWalkView view) {

    super(view);
  }

  protected abstract XWalkResourceClient delegate ();

  protected boolean hasDelegate () {

    return delegate() != null;
  }

  @Override public void onLoadFinished (XWalkView view, String url) {

    if (hasDelegate()) {
      delegate().onLoadFinished(view, url);
    } else {
      super.onLoadFinished(view, url);
    }
  }

  @Override public void onLoadStarted (XWalkView view, String url) {

    if (hasDelegate()) {
      delegate().onLoadStarted(view, url);
    } else {
      super.onLoadFinished(view, url);
    }
  }

  @Override public WebResourceResponse shouldInterceptLoadRequest (XWalkView view, String url) {

    if (hasDelegate()) {
      return delegate().shouldInterceptLoadRequest(view, url);
    } else {
      return super.shouldInterceptLoadRequest(view, url);
    }
  }

  @Override public void onReceivedSslError (XWalkView view, ValueCallback<Boolean> callback, SslError error) {

    super.onReceivedSslError(view, callback, error);
  }

  @Override public void onReceivedLoadError (XWalkView view, int errorCode, String description, String failingUrl) {

    super.onReceivedLoadError(view, errorCode, description, failingUrl);
  }

 }