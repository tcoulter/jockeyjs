package com.jockeyjs;

import java.util.Map;

public abstract class JockeyHandler {

	public interface OnCompletedListener {
		public void onCompleted();
	}

	public void perform(Map<Object, Object> payload) {
		perform(payload, null);
	}
	
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
