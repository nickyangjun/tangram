package com.kks.sample.tangram.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.kks.sample.R;


/**
 * 文件名：ResizableImageView
 * 描述： 当图片宽度设置为 MATCH_PARENT 高度设置为 WRAP_CONTENT 时，修正图片高度等于图片宽度充满全屏时所得比值的高度
 * 如果设置了 maxHeight， 当按宽度缩放后最大高度超过 maxHeight 时，此时会自动根据 高度的缩放比例，来缩放宽度
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/24
 * 版权：小水滴
 */
public class ResizableImageView extends androidx.appcompat.widget.AppCompatImageView {
    private int maxHeight;

    public ResizableImageView(Context context) {
        this(context, null);
    }

    public ResizableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResizableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ResizableImageView, defStyleAttr, 0);

        maxHeight = ta.getDimensionPixelSize(R.styleable.ResizableImageView_viewMaxHeight, 0);
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getDrawable();
        if (d != null) {
            // ceil not round - avoid thin vertical gaps along the left/right edges
            int width = MeasureSpec.getSize(widthMeasureSpec);
            //高度根据使得图片的宽度充满屏幕计算而得
            int height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
            if (maxHeight > 0 && height > maxHeight) {
                width = (int) Math.ceil((float) maxHeight * (float) d.getIntrinsicWidth() / (float) d.getIntrinsicHeight());
                height = maxHeight;
            }
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
