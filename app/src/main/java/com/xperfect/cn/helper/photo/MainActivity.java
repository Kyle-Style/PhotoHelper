package com.xperfect.cn.helper.photo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.xperfect.cn.helper.photo.view.PhotoFrescoView;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.iv_photo)
  PhotoFrescoView ivPhoto;
  @BindView(R.id.btn_reset)
  Button btnReset;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    Fresco.initialize(this);
    initView("http://upload.wikimedia.org/wikipedia/commons/3/31/Big.Buck.Bunny.-.Frank.Bunny.png");
  }

  public void initView(String url) {
    DraweeController ctrl = Fresco.newDraweeControllerBuilder().setUri(
        url).setTapToRetryEnabled(true).build();
    GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
        .setActualImageScaleType(ScaleType.FIT_CENTER)
        .setProgressBarImage(new ProgressBarDrawable())
        .build();
    ivPhoto.setController(ctrl);
    ivPhoto.setHierarchy(hierarchy);
  }

  @OnClick(R.id.btn_reset)
  public void onViewClicked() {
    initView("http://upload.wikimedia.org/wikipedia/commons/3/31/Big.Buck.Bunny.-.Frank.Bunny.png");
  }
}