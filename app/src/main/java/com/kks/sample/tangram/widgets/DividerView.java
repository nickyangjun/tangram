package com.kks.sample.tangram.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kks.sample.CommonUtil;
import com.kks.sample.tangram.card.TDataHelper;
import com.tmall.wireless.tangram.structure.BaseCell;

public class DividerView extends TBaseItemView {
    public final static String TYPE = "Divider_VIew";

    public DividerView(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void initView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

    }

    @Override
    public void cellInited(BaseCell cell) {
        super.cellInited(cell);

        Divider divider = TDataHelper.getData(cell, Divider.class);
        if (divider == null) return;

        setBackgroundColor(getResources().getColor(divider.bgColor));
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtil.dip2px(getContext(), divider.height));
            setLayoutParams(layoutParams);
        } else if (layoutParams.height != divider.height) {
            layoutParams.height = CommonUtil.dip2px(getContext(), divider.height);
        }
    }

    public static class Divider {
        public int bgColor;
        public int height;

        public static Divider getDivider(int bgColor, int height) {
            Divider divider = new Divider();
            divider.bgColor = bgColor;
            divider.height = height;
            return divider;
        }
    }
}
