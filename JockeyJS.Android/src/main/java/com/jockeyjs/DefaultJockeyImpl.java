package com.jockeyjs;

import com.google.gson.Gson;
import org.xwalk.core.XWalkView;

public class DefaultJockeyImpl extends JockeyImpl {

  private int messageCount = 0;

  private Gson gson = new Gson();

  public DefaultJockeyImpl (XWalkView view) {

    super(view);
  }

  @Override public void send (String type, XWalkView toWebView, Object withPayload, JockeyCallback complete) {

    int messageId = messageCount;

    if (complete != null) {
      add(messageId, complete);
    }

    String jsonPayload = null;
    if (withPayload != null) {
      jsonPayload = gson.toJson(withPayload);
    }

    String url = String.format("javascript:Jockey.trigger(\"%s\", %d, %s)", type, messageId, jsonPayload);
    toWebView.load(url, null);

    ++messageCount;
  }

  @Override public void triggerCallbackOnXWalkView (XWalkView webView, int messageId) {

    String url = String.format("javascript:Jockey.triggerCallback(\"%d\")", messageId);
    webView.load(url, null);
  }
}