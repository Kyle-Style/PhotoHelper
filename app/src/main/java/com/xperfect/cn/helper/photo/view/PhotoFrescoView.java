package com.xperfect.cn.helper.photo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.facebook.common.internal.Preconditions;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.xperfect.cn.helper.photo.lib.DefaultTransformHelper;
import com.xperfect.cn.helper.photo.lib.TransformHelper;

public class PhotoFrescoView extends DraweeView<GenericDraweeHierarchy>
    implements DefaultTransformHelper.Listener {

  private static final Class<?> TAG = PhotoFrescoView.class;

  private static final float HUGE_IMAGE_SCALE_FACTOR_THRESHOLD = 1.1f;

  private final RectF mImageBounds = new RectF();
  private final RectF mViewBounds = new RectF();

  private final ControllerListener mControllerListener = new BaseControllerListener<Object>() {
    @Override
    public void onFinalImageSet(
        String id,
        @Nullable Object imageInfo,
        @Nullable Animatable animatable) {
      PhotoFrescoView.this.onFinalImageSet();
    }

    @Override
    public void onRelease(String id) {
      PhotoFrescoView.this.onRelease();
    }
  };

  private DraweeController mHugeImageController;
  private TransformHelper mZoomableController = DefaultTransformHelper.newInstance();

  public PhotoFrescoView(Context context) {
    super(context);
    init();
  }

  public PhotoFrescoView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PhotoFrescoView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    mZoomableController.setListener(this);
  }

  public void setZoomableController(DefaultTransformHelper DefaultTransformHelper) {
    Preconditions.checkNotNull(DefaultTransformHelper);
    mZoomableController.setListener(null);
    mZoomableController = DefaultTransformHelper;
    mZoomableController.setListener(this);
  }

  @Override
  public void setController(@Nullable DraweeController controller) {
    setControllers(controller, null);
  }

  private void setControllersInternal(
      @Nullable DraweeController controller,
      @Nullable DraweeController hugeImageController) {
    removeControllerListener(getController());
    addControllerListener(controller);
    mHugeImageController = hugeImageController;
    super.setController(controller);
  }

  /**
   * Sets the controllers for the normal and huge image.
   * <p>
   * <p> IMPORTANT: in order to avoid a flicker when switching to the huge image, the huge image
   * controller should have the normal-image-uri set as its low-res-uri.
   *
   * @param controller controller to be initially used
   * @param hugeImageController controller to be used after the client starts zooming-in
   */
  public void setControllers(
      @Nullable DraweeController controller,
      @Nullable DraweeController hugeImageController) {
    setControllersInternal(null, null);
    mZoomableController.setEnabled(false);
    setControllersInternal(controller, hugeImageController);
  }

  private void maybeSetHugeImageController() {
    if (mHugeImageController != null &&
        mZoomableController.getScaleFactor() > HUGE_IMAGE_SCALE_FACTOR_THRESHOLD) {
      setControllersInternal(mHugeImageController, null);
    }
  }

  private void removeControllerListener(DraweeController controller) {
    if (controller instanceof AbstractDraweeController) {
      ((AbstractDraweeController) controller)
          .removeControllerListener(mControllerListener);
    }
  }

  private void addControllerListener(DraweeController controller) {
    if (controller instanceof AbstractDraweeController) {
      ((AbstractDraweeController) controller)
          .addControllerListener(mControllerListener);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    int saveCount = canvas.save();
    canvas.concat(mZoomableController.getTransform());
    super.onDraw(canvas);
    canvas.restoreToCount(saveCount);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mZoomableController.onTouchEvent(event)) {
      if (mZoomableController.getScaleFactor() > 1.0f) {
        getParent().requestDisallowInterceptTouchEvent(true);
      }
      return true;
    }
    return super.onTouchEvent(event);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    updateZoomableControllerBounds();
  }

  private void onFinalImageSet() {
    if (!mZoomableController.enabled()) {
      updateZoomableControllerBounds();
      mZoomableController.setEnabled(true);
    }
  }

  private void onRelease() {
    mZoomableController.setEnabled(false);
  }

  @Override
  public void onTransformed(Matrix transform) {
    maybeSetHugeImageController();
    invalidate();
  }

  private void updateZoomableControllerBounds() {
    getHierarchy().getActualImageBounds(mImageBounds);
    mViewBounds.set(0, 0, getWidth(), getHeight());
    mZoomableController.setObjectBounds(mImageBounds);
    mZoomableController.setViewBounds(mViewBounds);
  }
}