package com.xperfect.cn.helper.photo.lib.gestures;

import android.view.MotionEvent;

public class DefaultGestureDetector extends BaseGestureDetector implements GestureDetector,
    MultiPointerGestureDetector.GestureListener {

  public static final String TAG = DefaultGestureDetector.class.getSimpleName();

  public interface GestureListener {

    void onGestureBegin(DefaultGestureDetector detector);

    void onGestureUpdate(DefaultGestureDetector detector);

    void onGestureEnd(DefaultGestureDetector detector);

    void click(int count, DefaultGestureDetector detector);
  }

  public static DefaultGestureDetector newInstance() {
    return new DefaultGestureDetector();
  }

  MultiPointerGestureDetector detector;

  GestureListener gestureListener;

  public DefaultGestureDetector() {
    detector = MultiPointerGestureDetector.newInstance();
    detector.setGestureListener(this);
  }

  public void setGestureListener(GestureListener gestureListener) {
    this.gestureListener = gestureListener;
  }

  public boolean onTouchEvent(final MotionEvent event) {
    return detector.onTouchEvent(event);
  }

  public void restartGesture() {
    detector.restartGesture();
  }

  @Override
  public float getPivotX() {
    return calcAverage(detector.getStartX(), detector.getCount());
  }

  @Override
  public float getPivotY() {
    return calcAverage(detector.getStartY(), detector.getCount());
  }

  @Override
  public float getTranslationX() {
    return calcAverage(detector.getCurrentX(), detector.getCount()) -
        calcAverage(detector.getStartX(), detector.getCount());
  }

  @Override
  public float getTranslationY() {
    return calcAverage(detector.getCurrentY(), detector.getCount()) -
        calcAverage(detector.getStartY(), detector.getCount());
  }

  @Override
  public float getScale() {
    if (detector.getCount() < 2) {
      return 1;
    } else {
      float startDeltaX = detector.getStartX()[1] - detector.getStartX()[0];
      float startDeltaY = detector.getStartY()[1] - detector.getStartY()[0];
      float currentDeltaX = detector.getCurrentX()[1] - detector.getCurrentX()[0];
      float currentDeltaY = detector.getCurrentY()[1] - detector.getCurrentY()[0];
      float startDist = (float) Math.hypot(startDeltaX, startDeltaY);
      float currentDist = (float) Math.hypot(currentDeltaX, currentDeltaY);
      return currentDist / startDist;
    }
  }

  @Override
  public float getRotation() {
    if (detector.getCount() < 2) {
      return 0;
    } else {
      float startDeltaX = detector.getStartX()[1] - detector.getStartX()[0];
      float startDeltaY = detector.getStartY()[1] - detector.getStartY()[0];
      float currentDeltaX = detector.getCurrentX()[1] - detector.getCurrentX()[0];
      float currentDeltaY = detector.getCurrentY()[1] - detector.getCurrentY()[0];
      float startAngle = (float) Math.atan2(startDeltaY, startDeltaX);
      float currentAngle = (float) Math.atan2(currentDeltaY, currentDeltaX);
      return currentAngle - startAngle;
    }
  }

  //时间反馈
  @Override
  public void onGestureBegin(MultiPointerGestureDetector detector) {
    if (gestureListener != null) {
      gestureListener.onGestureBegin(this);
    }
  }

  @Override
  public void onGestureUpdate(MultiPointerGestureDetector detector) {
    if (gestureListener != null) {
      gestureListener.onGestureUpdate(this);
    }
  }

  @Override
  public void onGestureEnd(MultiPointerGestureDetector detector) {
    if (gestureListener != null) {
      gestureListener.onGestureEnd(this);
    }
  }

  @Override
  public void click(int count, MultiPointerGestureDetector detector) {
    if (gestureListener != null) {
      gestureListener.click(count, this);
    }
  }
}