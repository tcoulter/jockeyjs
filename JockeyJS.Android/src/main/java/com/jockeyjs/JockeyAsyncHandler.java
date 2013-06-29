package com.jockeyjs;

import java.util.Map;

import com.jockeyjs.util.BackgroundExecutor;

/**
 * Base class for JockeyHandlers that will perform their work in a separate thread.
 * 
 * 
 * @author Paul
 *
 */
public abstract class JockeyAsyncHandler extends JockeyHandler {
	
	@Override
	public final void perform(final Map<Object, Object> payload, final OnCompletedListener listener) {
		BackgroundExecutor.execute(new Runnable() {
			@Override
			public void run() {
				JockeyAsyncHandler.super.perform(payload, listener);				
			}
		});
		
	}
}
