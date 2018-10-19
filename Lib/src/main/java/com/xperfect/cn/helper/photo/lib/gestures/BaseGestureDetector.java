package com.xperfect.cn.helper.photo.lib.gestures;

public class BaseGestureDetector {

  protected float calcAverage(float[] arr) {
    float sum = 0;
    int count = 0;
    for (int i = 0; i < arr.length; i++) {
      if (arr[i] > 0) {
        count++;
        sum += arr[i];
      }
    }
    return (count > 0) ? sum / count : 0;
  }

  protected float effectiveLength(float[] arr) {
    int count = 0;
    for (int i = 0; i < arr.length; i++) {
      if (arr[i] > 0) {
        count++;
      }
    }
    return count;
  }
}