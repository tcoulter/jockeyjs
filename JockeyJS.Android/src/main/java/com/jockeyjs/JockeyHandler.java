package com.jockeyjs;

import java.util.Map;

public abstract class JockeyHandler {

	public interface OnCompletedListener {
		public void onCompleted();
	}

	
	/**
	 * Executes this handler with a given payload
	 * 
	 * @param payload
	 */
	public void perform(Map<Object, Object> payload) {
		perform(payload, null);
	}
	
	/**
	 * Executes this handler with a given payload and 
	 * a listener that will be notified when this operation is complete
	 * 
	 * @param payload
	 * @param listener
	 */
	public void perform(Map<Object, Object> payload, OnCompletedListener listener) {
		doPerform(payload);
		completed(listener);
	}
	
	protected void completed(OnCompletedListener listener) {
		if (listener != null)
			listener.onCompleted();
	}

	protected abstract void doPerform(Map<Object, Object> payload);
}
