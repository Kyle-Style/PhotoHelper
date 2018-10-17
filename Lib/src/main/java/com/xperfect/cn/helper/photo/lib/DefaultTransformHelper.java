package com.xperfect.cn.helper.photo.lib;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import com.xperfect.cn.helper.photo.lib.gestures.DefaultGestureDetector;

public class DefaultTransformHelper implements TransformHelper,
    DefaultGestureDetector.GestureListener {

  public static final String TAG = DefaultTransformHelper.class.getSimpleName();

  private boolean enabled = true;
  private float scaleFactor = 0;

  private boolean isRotationEnabled = true;
  private boolean isScaleEnabled = true;
  private boolean isTranslationEnabled = true;

  private float mMinScaleFactor = 1.0f;

  private final RectF viewBounds = new RectF();
  private final RectF imageBounds = new RectF();
  private final RectF transformBounds = new RectF();
  private final Matrix previousTransformMatrix = new Matrix();
  private final Matrix activeTransformMatrix = new Matrix();
  private final Matrix activeTransformInverseMatrix = new Matrix();
  private final float[] tempMatrixArray = new float[9];

  Listener listener;
  DefaultGestureDetector defaultGestureDetector;

  public DefaultTransformHelper() {
    defaultGestureDetector = DefaultGestureDetector.newInstance();
    defaultGestureDetector.setGestureListener(this);
  }

  public boolean isRotationEnabled() {
    return isRotationEnabled;
  }

  public void setRotationEnabled(boolean rotationEnabled) {
    isRotationEnabled = rotationEnabled;
  }

  public boolean isScaleEnabled() {
    return isScaleEnabled;
  }

  public void setScaleEnabled(boolean scaleEnabled) {
    isScaleEnabled = scaleEnabled;
  }

  public boolean isTranslationEnabled() {
    return isTranslationEnabled;
  }

  public void setTranslationEnabled(boolean translationEnabled) {
    isTranslationEnabled = translationEnabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  @Override
  public float getScaleFactor() {
    return defaultGestureDetector.getScale();
  }

  @Override
  public Matrix getTransform() {
    return this.activeTransformMatrix;
  }

  @Override
  public void setTransformBounds(RectF transformBounds) {
    this.imageBounds.set(transformBounds);
  }

  @Override
  public void setViewBounds(RectF viewBounds) {
    this.viewBounds.set(viewBounds);
  }

  @Override
  public boolean onTouchEvent(MotionEvent motionEvent) {
    return defaultGestureDetector.onTouchEvent(motionEvent);
  }

  @Override
  public void onGestureBegin(DefaultGestureDetector detector) {

  }

  @Override
  public void onGestureUpdate(DefaultGestureDetector detector) {
    this.activeTransformMatrix.set(this.previousTransformMatrix);
    if (isTranslationEnabled) {
      this.activeTransformMatrix
          .postTranslate(detector.getTranslationX(), detector.getTranslationY());
    }
    if (isScaleEnabled) {
      float scale = detector.getScale();
      this.activeTransformMatrix
          .postScale(scale, scale, detector.getPivotX(), detector.getPivotY());
    }
    if (isRotationEnabled) {
      float angle = detector.getRotation() * (float) (180 / Math.PI);
      this.activeTransformMatrix.postRotate(angle, detector.getPivotX(), detector.getPivotY());
    }
    limitScale(detector.getPivotX(), detector.getPivotY());
    limitTranslation();
    if (listener != null) {
      listener.onTransformed(this.activeTransformMatrix);
    }
  }

  @Override
  public void onGestureEnd(DefaultGestureDetector detector) {
    this.previousTransformMatrix.set(this.activeTransformMatrix);
  }

  @Override
  public void click(int count, DefaultGestureDetector detector) {
    Log.e(TAG,
        "click count " + count + " TranslationX  " + detector.getTranslationX() + " TranslationY  "
            + detector.getTranslationY());
    if (isScaleEnabled) {
      float scale = detector.getScale();
      switch (count) {
        case 1:
          scale += 1;
        case 2:
          scale -= 1;
        case 3:
          scale = 1;
        default:
      }
      this.activeTransformMatrix
          .postScale(scale, scale, detector.getPivotX(), detector.getPivotY());
    }
  }

  private void limitScale(float pivotX, float pivotY) {
    float currentScale = getScaleFactor();
    if (currentScale < mMinScaleFactor) {
      float scale = mMinScaleFactor / currentScale;
      this.activeTransformMatrix.postScale(mMinScaleFactor, mMinScaleFactor, pivotX, pivotY);
    }
  }

  private void limitTranslation() {
    RectF bounds = this.transformBounds;
    bounds.set(this.imageBounds);
    this.activeTransformMatrix.mapRect(bounds);
    float offsetLeft = getOffset(bounds.left, bounds.width(), this.viewBounds.width());
    float offsetTop = getOffset(bounds.top, bounds.height(), this.viewBounds.height());
    if (offsetLeft != bounds.left || offsetTop != bounds.top) {
      this.activeTransformMatrix.postTranslate(offsetLeft - bounds.left, offsetTop - bounds.top);
      defaultGestureDetector.restartGesture();
    }
  }

  private float getOffset(float offset, float imageDimension, float viewDimension) {
    float diff = viewDimension - imageDimension;
    return (diff > 0) ? diff / 2 : limit(offset, diff, 0);
  }

  private float limit(float value, float min, float max) {
    return Math.min(Math.max(min, value), max);
  }
}