package com.xperfect.cn.helper.photo.lib;

import android.graphics.Matrix;
import android.graphics.PointF;
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
  private final RectF objectBounds = new RectF();
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

  public static DefaultTransformHelper newInstance() {
    return new DefaultTransformHelper();
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
    activeTransformMatrix.getValues(tempMatrixArray);
    return tempMatrixArray[Matrix.MSCALE_X];
  }

  @Override
  public Matrix getTransform() {
    return this.activeTransformMatrix;
  }

  @Override
  public void setObjectBounds(RectF objectBounds) {
    this.objectBounds.set(objectBounds);
  }

  @Override
  public void setViewBounds(RectF viewBounds) {
    this.viewBounds.set(viewBounds);
  }

  @Override
  public boolean onTouchEvent(MotionEvent motionEvent) {
    if (enabled) {
      return defaultGestureDetector.onTouchEvent(motionEvent);
    }
    return false;
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
      if (listener != null) {
        listener.onTransformed(this.activeTransformMatrix);
      }
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
    bounds.set(this.objectBounds);
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

  /**
   * Maps array of 2D points from absolute to the image's relative coordinate system, and writes the
   * transformed points back into the array. Points are represented by float array of [x0, y0, x1,
   * y1, ...].
   *
   * @param destPoints destination array (may be the same as source array)
   * @param srcPoints source array
   * @param numPoints number of points to map
   */
  private void mapAbsoluteToRelative(float[] destPoints, float[] srcPoints, int numPoints) {
    for (int i = 0; i < numPoints; i++) {
      destPoints[i * 2 + 0] =
          (srcPoints[i * 2 + 0] - transformBounds.left) / transformBounds.width();
      destPoints[i * 2 + 1] =
          (srcPoints[i * 2 + 1] - transformBounds.top) / transformBounds.height();
    }
  }

  /**
   * Maps array of 2D points from relative to the image's absolute coordinate system, and writes the
   * transformed points back into the array Points are represented by float array of [x0, y0, x1,
   * y1, ...].
   *
   * @param destPoints destination array (may be the same as source array)
   * @param srcPoints source array
   * @param numPoints number of points to map
   */
  private void mapRelativeToAbsolute(float[] destPoints, float[] srcPoints, int numPoints) {
    for (int i = 0; i < numPoints; i++) {
      destPoints[i * 2 + 0] = srcPoints[i * 2 + 0] * transformBounds.width() + transformBounds.left;
      destPoints[i * 2 + 1] = srcPoints[i * 2 + 1] * transformBounds.height() + transformBounds.top;
    }
  }

  /**
   * Maps point from the view's to the object's relative coordinate system. This takes into account
   * the zoomable transformation.
   */
  public PointF mapViewToObject(PointF viewPoint) {
    float[] points = tempMatrixArray;
    points[0] = viewPoint.x;
    points[1] = viewPoint.y;
    activeTransformMatrix.invert(activeTransformMatrix);
    activeTransformInverseMatrix.mapPoints(points, 0, points, 0, 1);
    mapAbsoluteToRelative(points, points, 1);
    return new PointF(points[0], points[1]);
  }

  /**
   * Maps point from the object's relative to the view's coordinate system. This takes into account
   * the zoomable transformation.
   */
  public PointF mapObjectToView(PointF objectPointF) {
    float[] points = tempMatrixArray;
    points[0] = objectPointF.x;
    points[1] = objectPointF.y;
    mapRelativeToAbsolute(points, points, 1);
    activeTransformMatrix.mapPoints(points, 0, points, 0, 1);
    return new PointF(points[0], points[1]);
  }
}