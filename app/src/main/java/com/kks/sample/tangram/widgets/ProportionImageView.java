package com.kks.sample.tangram.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.kks.sample.R;


/**
 * 文件名：ProportionImageView
 * 描述：根据宽度，按比例来缩放高度
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/8/5
 * 版权：小水滴
 */
public class ProportionImageView extends androidx.appcompat.widget.AppCompatImageView {
    private float proportion = 1f;

    public ProportionImageView(Context context) {
        this(context, null);
    }

    public ProportionImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProportionImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProportionImageView, defStyleAttr, 0);

        proportion = ta.getFloat(R.styleable.ProportionImageView_proportion, 1f);
    }

    public void setProportion(float proportion) {
        if (this.proportion != proportion) {
            this.proportion = proportion;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width > 0) {
            setMeasuredDimension(width, (int) (width * proportion));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}