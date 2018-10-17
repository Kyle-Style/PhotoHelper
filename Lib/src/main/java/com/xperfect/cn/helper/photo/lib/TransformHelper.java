package com.xperfect.cn.helper.photo.lib;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.MotionEvent;

public interface TransformHelper {

  interface Listener {

    void onTransformed(Matrix transform);
  }

  void setEnabled(boolean enabled);

  boolean enabled();

  void setListener(Listener listener);

  float getScaleFactor();

  Matrix getTransform();

  void setTransformBounds(RectF transformBounds);

  void setViewBounds(RectF viewBounds);

  boolean onTouchEvent(MotionEvent motionEvent);
}