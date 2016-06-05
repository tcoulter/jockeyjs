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

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

/**
 * The primary interface for communicating between a XWalkView and the local android activity.
 *
 * @author Paul
 */
public interface Jockey {

  /**
   * Attaches one or more handlers to this jockey instance so that is may receive events of the provided type
   * from a webpage
   */
  public void on (String type, JockeyHandler... handler);

  /**
   * Removes all handlers of the specified name
   */
  public void off (String type);

  /**
   * Sends a new event to the XWalkView
   *
   * Equivalent to calling send(type, toXWalkView, null, null);
   */
  public void send (String type, XWalkView toXWalkView);

  /**
   * Sends a new event to the XWalkView with the included payload
   *
   * Equivalent to calling send(type, toXWalkView, payload, null)
   */
  public void send (String type, XWalkView toXWalkView, Object withPayload);

  /**
   * Sends the new event to the XWalkView and registers a callback to listen for the returned value
   *
   * Equivalent to calling send(type, toXWalkView, null, complete)
   */
  public void send (String type, XWalkView toXWalkView, JockeyCallback complete);

  /**
   * Sends the new event to the XWalkView with a payload and a callback to listen for the webpage response.
   */
  public void send (String type, XWalkView toXWalkView, Object withPayload, JockeyCallback complete);

  /**
   * Triggers the callback on the XWalkView with the appropriate messageId
   */
  public void triggerCallbackOnXWalkView (XWalkView XWalkView, int messageId);

  /**
   * Configures the XWalkView to be able to receive and send events with Jockey
   * @param XWalkView
   */
  public void configure (XWalkView XWalkView);

  /**
   * Returns if the Jockey implementation handles the event
   */
  public boolean handles (String string);

  /**
   * Sets the imageListener that will be called when validation needs to be performed
   * on the incoming host before a redirect
   */
  public void setOnValidateListener (OnValidateListener listener);

  public void setXWalkViewClient (XWalkResourceClient client);

  /**
   * An interface for the app to check the incoming source of an event.
   *
   * @author Paul
   */
  public interface OnValidateListener {

    boolean validate (String host);
  }
}
