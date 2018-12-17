package com.jockeyjs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Composes a group of JockeyHandlers into a single structure, <br>
 * When perform is invoked on this handler it will invoke it on all of the handlers.<br>
 * They will be invoked in FIFO order, however if any are asynchronous, no guarantees can be made for
 * execution order.
 * 
 * To maintain a single callback invariant, this handler will accumulate <br>
 * all the "complete" calls from the internal handlers.
 * <br><br>
 * Once all of the handlers have been called it this handler will signal completion.
 * 
 * @author Paul
 *
 */
public class CompositeJockeyHandler extends JockeyHandler {

	/**
	 * Accumulates all the "completed" calls from the contained Handlers
	 * Once all the handlers have completed this will signal completion
	 * 
	 * @author Paul
	 *
	 */
	private class AccumulatingListener implements OnCompletedListener {

		private int _size;
		private AtomicInteger _accumulated;
		private final OnCompletedListener _resultListener;

		private AccumulatingListener(OnCompletedListener listener) {
			this._size = _handlers.size();
			this._accumulated = new AtomicInteger(0);
			this._resultListener = listener;
		}

		@Override
		public void onCompleted(String data) {
			_accumulated.incrementAndGet();

			if (_accumulated.get() >= _size) {
				completed(_resultListener, data);
      }
		}
	}

	private List<JockeyHandler> _handlers = new ArrayList<JockeyHandler>();

	public CompositeJockeyHandler(JockeyHandler ... handlers) {
		add(handlers);
	}

	public void add(JockeyHandler ... handler) {
		_handlers.addAll(Arrays.asList(handler));
	}
	
	public void clear(JockeyHandler handler) {
		_handlers.clear();
	}
	
	@Override
	public void perform(Map<Object, Object> payload, OnCompletedListener listener) {
		for (JockeyHandler handler : _handlers) {
			handler.perform(payload, new AccumulatingListener(listener));
		}
	}
	
	@Override
	protected void doPerform(Map<Object, Object> payload) {}
	
	public static JockeyHandler compose(JockeyHandler ... handlers) {
		return new CompositeJockeyHandler(handlers);
	}

}
