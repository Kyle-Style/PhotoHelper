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
    return calcAverage(detector.getStartX());
  }

  @Override
  public float getPivotY() {
    return calcAverage(detector.getStartY());
  }

  @Override
  public float getTranslationX() {
    return calcAverage(detector.getCurrentX()) -
        calcAverage(detector.getStartX());
  }

  @Override
  public float getTranslationY() {
    return calcAverage(detector.getCurrentY()) -
        calcAverage(detector.getStartY());
  }

  @Override
  public float getScale() {
    if (detector.getCount() < 2) {
      return 1;
    } else {
      if (effectiveLength(detector.getStartX()) > 1) {
        float startDeltaX = calcAverage(detector.getStartX()) - detector.getStartX()[0];
        float startDeltaY = calcAverage(detector.getStartY()) - detector.getStartY()[0];
        float currentDeltaX =
            calcAverage(detector.getCurrentX()) - detector.getCurrentX()[0];
        float currentDeltaY =
            calcAverage(detector.getCurrentY()) - detector.getCurrentY()[0];
        float startDist = (float) Math.hypot(startDeltaX, startDeltaY);
        float currentDist = (float) Math.hypot(currentDeltaX, currentDeltaY);
        return currentDist / startDist;
      } else {
        return 1;
      }
    }
  }

  @Override
  public float getRotation() {
    if (detector.getCount() < 2) {
      return 0;
    } else {
      if (effectiveLength(detector.getStartX()) > 1) {
        float startDeltaX = calcAverage(detector.getStartX()) - detector.getStartX()[0];
        float startDeltaY = calcAverage(detector.getStartY()) - detector.getStartY()[0];
        float currentDeltaX =
            calcAverage(detector.getCurrentX()) - detector.getCurrentX()[0];
        float currentDeltaY =
            calcAverage(detector.getCurrentY()) - detector.getCurrentY()[0];
        float startAngle = (float) Math.atan2(startDeltaY, startDeltaX);
        float currentAngle = (float) Math.atan2(currentDeltaY, currentDeltaX);
        return currentAngle - startAngle;
      } else {
        return 0;
      }
    }
  }

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