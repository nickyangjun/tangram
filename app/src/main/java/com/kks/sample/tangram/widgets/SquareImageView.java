package com.kks.sample.tangram.widgets;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 文件名：SquareImageView
 * 描述： imageview  宽度 == 高度
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/24
 * 版权：小水滴
 */
public class SquareImageView extends androidx.appcompat.widget.AppCompatImageView{
    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if(width > 0){
            setMeasuredDimension(width, width);
        }else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
