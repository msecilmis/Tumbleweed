package aurelienribon.tweenengine;

import aurelienribon.tweenengine.TweenCallback.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public abstract class TimelineObject {

	// -------------------------------------------------------------------------
	// Abstract stuff
	// -------------------------------------------------------------------------

	/**
	 * If you want to manually manage your tweens and timelines (without using a
	 * TweenManager), and you enabled object pooling, then you need to call
	 * this method on your tweens and timelines once they are finished (see
	 * <i>isFinished()</i> method).
	 */
	public abstract void free();

	protected abstract void initializeOverride();
	protected abstract void computeOverride(int iteration, int lastIteration, int deltaMillis);
	protected abstract void forceStartValues(int iteration);
	protected abstract void forceEndValues(int iteration);

	protected abstract int getChildrenCount();
	protected abstract void killTarget(Object target);
	protected abstract void killTarget(Object target, int tweenType);
	protected abstract boolean containsTarget(Object target);
	protected abstract boolean containsTarget(Object target, int tweenType);

	// -------------------------------------------------------------------------
	// Attributes
	// -------------------------------------------------------------------------
	
	// General
	protected boolean isPooled;
	private boolean isYoyo;
	private boolean isComputeIteration;
	private int iteration;
	private int repeatCnt;

	// Timings
	protected int delayMillis;
	protected int durationMillis;
	protected int repeatDelayMillis;
	protected int currentMillis;
	protected boolean isStarted; // true when the object is started
	protected boolean isInitialized; // true after the delay
	protected boolean isFinished; // true when all repetitions are done or the object has been killed

	// Callbacks
	private List<TweenCallback> beginCallbacks;
	private List<TweenCallback> startCallbacks;
	private List<TweenCallback> endCallbacks;
	private List<TweenCallback> completeCallbacks;
	private List<TweenCallback> backStartCallbacks;
	private List<TweenCallback> backEndCallbacks;
	private List<TweenCallback> backCompleteCallbacks;

	// Misc
	private Object userData;

	// -------------------------------------------------------------------------

	protected void reset() {
		isPooled = Tween.isPoolingEnabled();

		isYoyo = isComputeIteration = false;
		iteration = repeatCnt = 0;
		
		delayMillis = durationMillis = repeatDelayMillis = currentMillis = 0;
		isStarted = isInitialized = isFinished = false;

		if (beginCallbacks != null) beginCallbacks.clear();
		if (startCallbacks != null) startCallbacks.clear();
		if (endCallbacks != null) endCallbacks.clear();
		if (completeCallbacks != null) completeCallbacks.clear();
		if (backStartCallbacks != null) backStartCallbacks.clear();
		if (backEndCallbacks != null) backEndCallbacks.clear();
		if (backCompleteCallbacks != null) backCompleteCallbacks.clear();

		userData = null;
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	/**
	 * Kills the interpolation. If pooling was enabled when this tween was
	 * created, the tween will be freed, cleared, and returned to the pool. As
	 * a result, you shouldn't use it anymore.
	 */
	public void kill() {
		isFinished = true;
	}

	/**
	 * Repeats the tween for a given number of times.
	 * @param count The number of desired repetition. For infinite repetition,
	 * use Tween.INFINITY, or a negative number.
	 * @param millis A setDelay before each repetition.
	 * @return The current tween for chaining instructions.
	 */
	public TimelineObject repeat(int count, int delayMillis) {
		repeatCnt = count;
		repeatDelayMillis = delayMillis >= 0 ? delayMillis : 0;
		isYoyo = false;
		return this;
	}

	/**
	 * Repeats the tween for a given number of times. Every two iterations, the
	 * tween will be played backwards.
	 * @param count The number of desired repetition. For infinite repetition,
	 * use Tween.INFINITY, or a negative number.
	 * @param millis A setDelay before each repetition.
	 * @return The current tween for chaining instructions.
	 */
	public TimelineObject repeatYoyo(int count, int delayMillis) {
		repeatCnt = count;
		repeatDelayMillis = delayMillis >= 0 ? delayMillis : 0;
		isYoyo = true;
		return this;
	}

	/**
	 * Adds a callback to the tween. The moment when the callback is triggered
	 * depends on its type:
	 * <br/><br/>
	 *
	 * BEGIN: at first START, right after the setDelay
	 * START: at each iteration beginning
	 * END: at each iteration ending, before the repeat setDelay
	 * COMPLETE: at last END
	 * BACK_START: at each bacwards iteration beginning, after the repeat setDelay
	 * BACK_END: at each backwards iteration ending
	 * BACK_COMPLETE: at last BACK_END
	 * <br/>
	 *
	 * <pre>
	 * forwards :         BEGIN                                   COMPLETE
	 * forwards :         START    END      START    END      START    END
	 * |------------------[XXXXXXXXXX]------[XXXXXXXXXX]------[XXXXXXXXXX]
	 * backwards:         bEND  bSTART      bEND  bSTART      bEND  bSTART
	 * backwards:         bCOMPLETE
	 * </pre>
	 *
	 *
	 * @param callbackType The callback type.
	 * @param callback A callback.
	 * @return The current tween for chaining instructions.
	 */
	public TimelineObject addCallback(Types callbackType, TweenCallback callback) {
		List<TweenCallback> callbacks = null;

		switch (callbackType) {
			case BEGIN: callbacks = beginCallbacks; break;
			case START: callbacks = startCallbacks; break;
			case END: callbacks = endCallbacks; break;
			case COMPLETE: callbacks = completeCallbacks; break;
			case BACK_START: callbacks = backStartCallbacks; break;
			case BACK_END: callbacks = backEndCallbacks; break;
			case BACK_COMPLETE: callbacks = backCompleteCallbacks; break;
		}

		if (callbacks == null) callbacks = new ArrayList<TweenCallback>(1);
		callbacks.add(callback);

		switch (callbackType) {
			case BEGIN: beginCallbacks = callbacks; break;
			case START: startCallbacks = callbacks; break;
			case END: endCallbacks = callbacks; break;
			case COMPLETE: completeCallbacks = callbacks; break;
			case BACK_START: backStartCallbacks = callbacks; break;
			case BACK_END: backEndCallbacks = callbacks; break;
			case BACK_COMPLETE: backCompleteCallbacks = callbacks; break;
		}

		return this;
	}

	/**
	 * Sets an object attached to this tween. It can be useful in order to
	 * retrieve some data from a TweenCallback.
	 * @param data Any kind of object.
	 * @return The current tween for chaining instructions.
	 */
	public TimelineObject setUserData(Object data) {
		userData = data;
		return this;
	}

	// -------------------------------------------------------------------------
	// Getters
	// -------------------------------------------------------------------------

	public int getDelay() {
		return delayMillis;
	}

	public int getDuration() {
		return durationMillis;
	}

	public int getRepeatCount() {
		return repeatCnt;
	}

	public int getRepeatDelay() {
		return repeatDelayMillis;
	}

	/**
	 * Returns the complete duration of a timeline, including its setDelay and its
	 * repetitions. The formula is as follows:
	 * <br/><br/>
	 *
	 * fullDuration = setDelay + duration + (repeatDelay + duration) * repeatCnt
	 */
	public int getFullDuration() {
		return delayMillis + durationMillis + (repeatDelayMillis + durationMillis) * repeatCnt;
	}

	/**
	 * Gets the attached user data, or null if none.
	 */
	public Object getUserData() {
		return userData;
	}

	public boolean isStarted() {
		return isStarted;
	}

	/**
	 * Returns true if the tween is finished (i.e. if the tween has reached
	 * its end or has been killed). If you don't use a TweenManager, and enabled
	 * object pooling, then don't forget to call <i>Tween.free()</i> on your
	 * tweens once <i>isFinished()</i> returns true.
	 */
	public boolean isFinished() {
		return isFinished;
	}

	public boolean isPooled() {
		return isPooled;
	}

	// -------------------------------------------------------------------------
	// Protected API
	// -------------------------------------------------------------------------

	protected boolean isIterationYoyo(int iteration) {
		return isYoyo && Math.abs(iteration%4) == 2;
	}

	protected void forceToStart() {
		currentMillis = -delayMillis;
		iteration = -1;
		isComputeIteration = false;
		forceStartValues(0);
	}

	protected void forceToEnd(int millis) {
		currentMillis = millis - getFullDuration();
		iteration = repeatCnt*2 + 1;
		isComputeIteration = false;
		forceEndValues(repeatCnt*2);
	}

	protected void callCallbacks(Types type) {
		List<TweenCallback> callbacks = null;

		switch (type) {
			case BEGIN: callbacks = beginCallbacks; break;
			case START: callbacks = startCallbacks; break;
			case END: callbacks = endCallbacks; break;
			case COMPLETE: callbacks = completeCallbacks; break;
			case BACK_START: callbacks = backStartCallbacks; break;
			case BACK_END: callbacks = backEndCallbacks; break;
			case BACK_COMPLETE: callbacks = backCompleteCallbacks; break;
		}

		if (callbacks != null && !callbacks.isEmpty())
			for (int i=0, n=callbacks.size(); i<n; i++)
				callbacks.get(i).tweenEventOccured(type, null);
	}

	// -------------------------------------------------------------------------
	// Update engine
	// -------------------------------------------------------------------------

	/**
	 * Updates the timeline state. <b>You may want to use a TweenManager to
	 * update timelines for you.</b> Slow motion, fast motion and backwards play
	 * can be easily achieved by tweaking the deltaMillis given as parameter.
	 * @param deltaMillis A delta time, in milliseconds, between now and the
	 * last call.
	 */
	public void update(int deltaMillis) {
		if (!isStarted) return;

		int lastIteration = iteration;
		currentMillis += deltaMillis;

		initialize();

		if (isInitialized) {
			testRelaunch();
			updateIteration();
			testInnerTransition(lastIteration);
			testLimitTransition(lastIteration);
			testCompletion();
			if (isComputeIteration) compute(lastIteration, deltaMillis);
		}
	}

	private void initialize() {
		if (!isInitialized && currentMillis >= delayMillis) {
			initializeOverride();
			isInitialized = true;
			isComputeIteration = true;
			currentMillis -= delayMillis;
			callCallbacks(Types.BEGIN);
			callCallbacks(Types.START);
		}
	}

	private void testRelaunch() {
		if (repeatCnt >= 0 && iteration > repeatCnt*2 && currentMillis <= 0) {
			assert iteration == repeatCnt*2 + 1;
			isComputeIteration = true;
			currentMillis += durationMillis;
			iteration = repeatCnt*2;

		} else if (repeatCnt >= 0 && iteration < 0 && currentMillis >= 0) {
			assert iteration == -1;
			isComputeIteration = true;
			iteration = 0;
		}
	}

	private void updateIteration() {
		while (isValid(iteration)) {
			if (!isComputeIteration && currentMillis <= 0) {
				isComputeIteration = true;
				currentMillis += durationMillis;
				iteration -= 1;
				callCallbacks(Types.BACK_START);

			} else if (!isComputeIteration && currentMillis >= repeatDelayMillis) {
				isComputeIteration = true;
				currentMillis -= repeatDelayMillis;
				iteration += 1;
				callCallbacks(Types.START);

			} else if (isComputeIteration && currentMillis < 0) {
				isComputeIteration = false;
				currentMillis += isValid(iteration-1) ? repeatDelayMillis : 0;
				iteration -= 1;
				callCallbacks(Types.BACK_END);

			} else if (isComputeIteration && currentMillis > durationMillis) {
				isComputeIteration = false;
				currentMillis -= durationMillis;
				iteration += 1;
				callCallbacks(Types.END);

			} else break;
		}
	}

	private void testInnerTransition(int lastIteration) {
		if (isComputeIteration) return;
		if (iteration > lastIteration) forceEndValues(iteration-1);
		else if (iteration < lastIteration) forceStartValues(iteration+1);
	}

	private void testLimitTransition(int lastIteration) {
		if (repeatCnt < 0 || iteration == lastIteration) return;
		if (iteration > repeatCnt*2) callCallbacks(Types.COMPLETE);
		else if (iteration < 0) callCallbacks(Types.BACK_COMPLETE);
	}

	private void testCompletion() {
		isFinished = (repeatCnt >= 0 && iteration > repeatCnt*2) || (repeatCnt >= 0 && iteration < 0);
	}

	private void compute(int lastIteration, int deltaMillis) {
		assert currentMillis >= 0;
		assert currentMillis <= durationMillis;
		assert isInitialized;
		assert !isFinished;
		assert isComputeIteration;
		assert isValid(iteration);
		computeOverride(iteration, lastIteration, deltaMillis);
	}

	private boolean isValid(int iteration) {
		return (iteration >= 0 && iteration <= repeatCnt*2) || repeatCnt < 0;
	}
}