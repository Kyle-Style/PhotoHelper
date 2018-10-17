package com.xperfect.cn.helper.photo.lib.gestures;

public class BaseGestureDetector {


  protected float calcAverage(float[] arr, int len) {
    float sum = 0;
    for (int i = 0; i < len; i++) {
      sum += arr[i];
    }
    return (len > 0) ? sum / len : 0;
  }
}