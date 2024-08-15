package com.kks.sample.tangram.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.view.ITangramViewLifeCycle;

import org.json.JSONException;

/**
 * 文件名：SampleItemView
 * 描述：
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/6/12
 * 版权：小水滴
 */
public class SampleItemView extends FrameLayout implements ITangramViewLifeCycle {
    public final static String TYPE = "SampleItemView";

    private int itemLayoutResId;

    public SampleItemView(@NonNull Context context) {
        this(context, null);
    }

    public SampleItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SampleItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void cellInited(BaseCell cell) {
        try {
            int itemLayoutResId = cell.extras.getInt("itemLayoutResId");
            if(this.itemLayoutResId != itemLayoutResId) {
                removeAllViews();
                inflate(getContext(), itemLayoutResId, this);
                this.itemLayoutResId = itemLayoutResId;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postBindView(BaseCell cell) {

    }

    @Override
    public void postUnBindView(BaseCell cell) {

    }
}
