package com.kks.sample.tangram.widgets;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class EmptyView extends FrameLayout {
    ViewGroup parent;
    private int mimHeight;  //最小高度

    public EmptyView(Context context, ViewGroup parent) {
        super(context);
        this.parent = parent;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setMimHeight(int mimHeight) {
        this.mimHeight = mimHeight;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //获取空view撑满全屏的高度
        int checkHeight =  this.parent.getHeight() - this.parent.getPaddingBottom() - top;
        if(checkHeight < mimHeight){
            checkHeight = mimHeight;
        }
        if(getLayoutParams().height != checkHeight){
            getLayoutParams().height = checkHeight;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                }
            },10);
            return;
        }

        super.onLayout(changed, left, top, right, bottom);
    }
}
