package com.xperfect.cn.helper.photo.lib.gestures;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.MotionEvent;

public class MultiPointerGestureDetector {

  public static final String TAG = MultiPointerGestureDetector.class.getSimpleName();

  public interface GestureListener {

    void onGestureBegin(MultiPointerGestureDetector detector);

    void onGestureUpdate(MultiPointerGestureDetector detector);

    void onGestureEnd(MultiPointerGestureDetector detector);

    void click(int count, MultiPointerGestureDetector detector);
  }

  private static final int MAX_POINTERS = 10;
  private static final int DEFAULT_ID = -1;
  private static final int DEFAULT_VALUE = 0;

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

  public boolean onTouchEvent(final MotionEvent event) {
    int pointerCount = event.getPointerCount();
    int actionIndex = event.getActionIndex();
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        switch (event.getActionMasked()) {
          case MotionEvent.ACTION_DOWN:
            stopGesture();
            reset();
            mCount = pointerCount;
            break;
          case MotionEvent.ACTION_POINTER_DOWN:
          default:
            mCount = pointerCount < mCount ? mCount
                : (pointerCount > MAX_POINTERS ? MAX_POINTERS : pointerCount);
        }
        for (int position = 0; position < MAX_POINTERS; position++) {
          //有效区间
          if (position < pointerCount) {
            mPointerId[position] = event.getPointerId(position);
          } else {
            mPointerId[position] = DEFAULT_ID;
          }
          int index = event.findPointerIndex(mPointerId[position]);
          if (index != -1) {
            mCurrentX[position] = mStartX[position] = event.getX(index);
            mCurrentY[position] = mStartY[position] = event.getY(index);
            if (position == actionIndex) {
              mStartTime[position] = System.currentTimeMillis();
            }
          } else {
            mCurrentX[position] = mStartX[position] = DEFAULT_VALUE;
            mCurrentY[position] = mStartY[position] = DEFAULT_VALUE;
            mStartTime[position] = DEFAULT_VALUE;
          }
        }
        if (mGestureInProgress && mCount > 0) {
          startGesture();
        } else {
        }
        break;
      case MotionEvent.ACTION_POINTER_UP:
      case MotionEvent.ACTION_UP: {
        boolean isAllDouble = false;
        for (int position = 0; position < MAX_POINTERS; position++) {
          //有效区间, pointerCount 比实际触点多1
          if (position < pointerCount - 1) {
            if (position < actionIndex) {
              mPointerId[position] = event.getPointerId(position);
            } else {
              mPointerId[position] = event.getPointerId(position + 1);
              mStartTime[position] = mStartTime[position + 1];
              mStartTimeTemple[position] = mStartTimeTemple[position + 1];
              mCurrentTime[position] = mCurrentTime[position + 1];
              mCurrentTimeTemple[position] = mCurrentTimeTemple[position + 1];
              mStartX[position] = mStartX[position + 1];
              mStartY[position] = mStartY[position + 1];
              mCurrentX[position] = mCurrentX[position + 1];
              mCurrentY[position] = mCurrentY[position + 1];
            }
          } else if (position < pointerCount) {
            mPointerId[position] = pointerCount == 1 ? event.getPointerId(0) : DEFAULT_ID;
          }
          if (mPointerId[position] == -1) {
            mCurrentX[position] = mStartX[position] = DEFAULT_VALUE;
            mCurrentY[position] = mStartY[position] = DEFAULT_VALUE;
            mStartTime[position] = mCurrentTime[position] = DEFAULT_VALUE;
          } else {
            if (position == actionIndex) {
              mCurrentTime[position] = System.currentTimeMillis();
            }
            long deltaStartTime = mStartTime[position] - mStartTimeTemple[position];
            long deltaCurrentTime = mCurrentTime[position] - mCurrentTimeTemple[position];
            if (deltaStartTime < 500 && deltaCurrentTime < 500) {
              isAllDouble = true;
            } else {
              isAllDouble = false;
            }
            mStartTimeTemple[position] = mStartTime[position];
            mCurrentTimeTemple[position] = mCurrentTime[position];
          }
        }
        if (isAllDouble && mCount == pointerCount && mListener != null) {
          mListener.click(event.getPointerCount(), this);
        }
        switch (event.getActionMasked()) {
          case MotionEvent.ACTION_UP:
            stopGesture();
            reset();
            mCount = 0;
            break;
          case MotionEvent.ACTION_POINTER_UP:
            if (mGestureInProgress) {
              startGesture();
            } else {
            }
            break;
          default:
        }
        break;
      }
      case MotionEvent.ACTION_MOVE: {
        for (int position = 0; position < MAX_POINTERS; position++) {
          int index = event.findPointerIndex(mPointerId[position]);
          if (index != -1) {
            mCurrentX[position] = event.getX(index);
            mCurrentY[position] = event.getY(index);
          }
        }
        if (!mGestureInProgress && shouldStartGesture()) {
          startGesture();
        }
        if (mGestureInProgress && mListener != null) {
          mListener.onGestureUpdate(this);
        }
        break;
      }
      case MotionEvent.ACTION_CANCEL: {
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