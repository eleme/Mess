package me.ele.mess;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

public class MyLinearLayoutManager extends LinearLayoutManager {
  public MyLinearLayoutManager(Context context) {
    super(context);
  }

  public MyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
    super(context, orientation, reverseLayout);
  }

  public MyLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }
}
