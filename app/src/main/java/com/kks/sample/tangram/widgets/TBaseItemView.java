package com.kks.sample.tangram.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.view.ITangramViewLifeCycle;

/**
 * 文件名：SampleItemView
 * 描述：
 * 版本：1.0
 * 作者：nickyang
 */
public abstract class TBaseItemView extends FrameLayout implements ITangramViewLifeCycle {

    public TBaseItemView(@NonNull Context context) {
        this(context, null);
    }

    public TBaseItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TBaseItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    protected abstract void initView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr);

    @Override
    public void cellInited(BaseCell cell) {

    }

    @Override
    public void postBindView(BaseCell cell) {

    }

    @Override
    public void postUnBindView(BaseCell cell) {

    }
}
