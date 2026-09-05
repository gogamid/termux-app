package com.termux.app;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/**
 * Detects a three-finger upward swipe on the terminal view and reports it to a listener
 * (which toggles the soft keyboard).
 *
 * <p>Note: three-finger swipe-DOWN commonly maps to the system "take screenshot" gesture on
 * many devices, so this detector only reacts to upward swipes.</p>
 *
 * <p>Attached as an {@link View.OnTouchListener} on the {@code TerminalView}. It always returns
 * {@code false} so that the terminal's own touch handling (scroll, selection, zoom, mouse
 * reporting, ...) keeps working exactly as before. An {@link View.OnTouchListener} is invoked by
 * {@link View#dispatchTouchEvent(MotionEvent)} before {@link View#onTouchEvent(MotionEvent)}, and
 * returning {@code false} lets the event fall through to the terminal's normal handling.</p>
 */
public final class TermuxThreeFingerSwipeDetector implements View.OnTouchListener {

    /** Listener notified once per three-finger swipe-up gesture. */
    public interface OnThreeFingerSwipeUpListener {
        void onThreeFingerSwipeUp();
    }

    private static final int MIN_POINTERS = 3;
    /** Total upward travel (of the average pointer Y) required to trigger the gesture. */
    private static final float SWIPE_THRESHOLD_DP = 120f;

    private final float mSwipeThresholdPx;
    private final OnThreeFingerSwipeUpListener mListener;

    private boolean mTracking;
    private float mLastAverageY;
    private float mAccumulatedDy;

    public TermuxThreeFingerSwipeDetector(Context context, OnThreeFingerSwipeUpListener listener) {
        mSwipeThresholdPx = SWIPE_THRESHOLD_DP * context.getResources().getDisplayMetrics().density;
        mListener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int actionMasked = event.getActionMasked();
        final int pointerCount = event.getPointerCount();

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerCount >= MIN_POINTERS) {
                    // Start tracking once at least three fingers are down.
                    mTracking = true;
                    mAccumulatedDy = 0f;
                    mLastAverageY = getAverageY(event);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mTracking && pointerCount >= MIN_POINTERS) {
                    float averageY = getAverageY(event);
                    float dy = averageY - mLastAverageY;
                    mLastAverageY = averageY;
                    if (dy < 0f) { // only accumulate upward movement (finger Y decreases)
                        mAccumulatedDy += -dy;
                        if (mAccumulatedDy >= mSwipeThresholdPx) {
                            mTracking = false;
                            mAccumulatedDy = 0f;
                            mListener.onThreeFingerSwipeUp();
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
                if (pointerCount < MIN_POINTERS) {
                    mTracking = false;
                    mAccumulatedDy = 0f;
                }
                break;
        }

        // Never consume the event; let the terminal handle it normally.
        return false;
    }

    private float getAverageY(MotionEvent event) {
        float sum = 0f;
        for (int i = 0; i < event.getPointerCount(); i++) {
            sum += event.getY(i);
        }
        return sum / event.getPointerCount();
    }

}
