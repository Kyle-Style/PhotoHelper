package com.xperfect.cn.helper.photo.lib.gestures;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;

public class MultiPointerGestureDetector {

  public static final String TAG = MultiPointerGestureDetector.class.getSimpleName();

  public interface GestureListener {

    void onGestureBegin(MultiPointerGestureDetector detector);

    void onGestureUpdate(MultiPointerGestureDetector detector);

    void onGestureEnd(MultiPointerGestureDetector detector);

    void click(int count, MultiPointerGestureDetector detector);
  }

  private static final int MAX_POINTERS = 3;

  private boolean mGestureInProgress;
  private int mCount;
  private final int mPointerId[] = new int[MAX_POINTERS];
  private final float mStartX[] = new float[MAX_POINTERS];
  private final float mStartY[] = new float[MAX_POINTERS];
  private final float mCurrentX[] = new float[MAX_POINTERS];
  private final float mCurrentY[] = new float[MAX_POINTERS];
  private final long mStartTimeTemple[] = new long[MAX_POINTERS];
  private final long mCurrentTimeTemple[] = new long[MAX_POINTERS];
  private final long mStartTime[] = new long[MAX_POINTERS];
  private final long mCurrentTime[] = new long[MAX_POINTERS];

  private GestureListener mListener = null;

  public MultiPointerGestureDetector() {
    reset();
  }

  public static MultiPointerGestureDetector newInstance() {
    return new MultiPointerGestureDetector();
  }

  public void setGestureListener(GestureListener listener) {
    mListener = listener;
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  public void reset() {
    mCount = 0;
    mGestureInProgress = false;
    for (int i = 0; i < MAX_POINTERS; i++) {
      mPointerId[i] = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ?
          -1 : MotionEvent.INVALID_POINTER_ID;
    }
  }

  protected boolean shouldStartGesture() {
    return true;
  }

  private void startGesture() {
    if (!mGestureInProgress) {
      mGestureInProgress = true;
      if (mListener != null) {
        mListener.onGestureBegin(this);
      }
    }
  }

  private void stopGesture() {
    if (mGestureInProgress) {
      mGestureInProgress = false;
      if (mListener != null) {
        mListener.onGestureEnd(this);
      }
    }
  }

  private int getPressedPointerIndex(MotionEvent event, int position) {
    final int count = event.getPointerCount();
    final int action = event.getActionMasked();
    final int index = event.getActionIndex();
    if (action == MotionEvent.ACTION_UP ||
        action == MotionEvent.ACTION_POINTER_UP) {
      if (position >= index) {
        position++;
      }
    }
    return (position < count) ? position : -1;
  }

  public boolean onTouchEvent(final MotionEvent event) {
    int pointerCount = event.getPointerCount();
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_POINTER_DOWN:
      case MotionEvent.ACTION_DOWN: {
        if (pointerCount == 1) {
          reset();
          startGesture();
        }
        for (int position = 0; position < MAX_POINTERS; position++) {
          int pointerIndex = getPressedPointerIndex(event, position);
          if (pointerIndex == -1) {
            mCurrentX[position] = mStartX[position] = 0;
            mCurrentY[position] = mStartY[position] = 0;
            mStartTime[position] = 0;
            continue;
          }
          mStartTime[position] = System.currentTimeMillis();
          mPointerId[position] = event.getPointerId(pointerIndex);
          mCurrentX[position] = mStartX[position] = event.getX(pointerIndex);
          mCurrentY[position] = mStartY[position] = event.getY(pointerIndex);
          mCount++;
        }
        break;
      }
      case MotionEvent.ACTION_POINTER_UP:
      case MotionEvent.ACTION_UP: {
        boolean wasGestureInProgress = mGestureInProgress;
        // update pointers
        boolean isAllDouble = false;
        for (int position = 0; position < MAX_POINTERS; position++) {
          int index = getPressedPointerIndex(event, position);
          if (index == -1) {
            if (pointerCount == 1) {
              mCurrentTime[position] = System.currentTimeMillis();
              mCurrentX[position] = event.getX();
              mCurrentY[position] = event.getY();
              long deltaStartTime = mStartTime[position] - mStartTimeTemple[position];
              long deltaCurrentTime = mCurrentTime[position] - mCurrentTimeTemple[position];
              if (deltaStartTime < 500 && deltaCurrentTime < 500 && mCount == 1) {
                Log.e(TAG,
                    "click  single position   " + position + "  deltaStartTime " + deltaStartTime
                        + "  deltaCurrentTime  " + deltaCurrentTime);
                isAllDouble = true;
              } else {
                isAllDouble = false;
              }
              mStartTimeTemple[position] = mStartTime[position];
              mCurrentTimeTemple[position] = mCurrentTime[position];
              break;
            } else {
              mCurrentTime[position] = 0;
              mCurrentX[position] = 0;
              mCurrentY[position] = 0;
            }
            continue;
          }
          mCurrentTime[position] = System.currentTimeMillis();
          mPointerId[position] = event.getPointerId(index);
          mCurrentX[position] = event.getX(index);
          mCurrentY[position] = event.getY(index);
          long deltaStartTime = mStartTimeTemple[position] - mStartTime[position];
          long deltaCurrentTime = mCurrentTimeTemple[position] - mCurrentTime[position];
          if (deltaStartTime < 500 && deltaCurrentTime < 500) {
            Log.e(TAG,
                "click  multiple position   " + position + "  deltaStartTime " + deltaStartTime
                    + "  deltaCurrentTime  " + deltaCurrentTime);
            isAllDouble = true;
          } else {
            isAllDouble = false;
          }
          mStartTimeTemple[position] = mStartTime[position];
          mCurrentTimeTemple[position] = mCurrentTime[position];
        }
        if (isAllDouble && mListener != null) {
          Log.e(TAG, "isAllDouble " + isAllDouble + " mGestureInProgress  " + mGestureInProgress
              + "  mListener != null " + (mListener != null));
          mListener.click(event.getPointerCount(), this);
        }
        if (wasGestureInProgress && mCount > 0) {
          startGesture();
        } else {
          stopGesture();
          reset();
        }
        break;
      }
      case MotionEvent.ACTION_MOVE: {
        // update pointers
        for (int position = 0; position < MAX_POINTERS; position++) {
          int index = event.findPointerIndex(mPointerId[position]);
          if (index != -1) {
            mCurrentX[position] = event.getX(index);
            mCurrentY[position] = event.getY(index);
          }
        }
        // start a new gesture if not already started
        if (!mGestureInProgress && shouldStartGesture()) {
          startGesture();
        }
        // notify listener
        if (mGestureInProgress && mListener != null) {
          mListener.onGestureUpdate(this);
        }
        break;
      }
      case MotionEvent.ACTION_CANCEL: {
        Log.e(TAG, "ACTION_CANCEL");
        stopGesture();
        reset();
        break;
      }
    }
    return true;
  }

  public void restartGesture() {
    if (!mGestureInProgress) {
      return;
    }
    stopGesture();
    for (int i = 0; i < MAX_POINTERS; i++) {
      mStartX[i] = mCurrentX[i];
      mStartY[i] = mCurrentY[i];
    }
    startGesture();
  }

  public boolean isGestureInProgress() {
    return mGestureInProgress;
  }

  public int getCount() {
    return mCount;
  }

  public float[] getStartX() {
    return mStartX;
  }

  public float[] getStartY() {
    return mStartY;
  }

  public float[] getCurrentX() {
    return mCurrentX;
  }

  public float[] getCurrentY() {
    return mCurrentY;
  }
}