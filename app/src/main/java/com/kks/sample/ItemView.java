package com.kks.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kks.sample.tangram.widgets.TBaseItemView;

public class ItemView extends TBaseItemView {
    public final static String TYPE = "ItemView";

    public ItemView(@NonNull Context context) {
        super(context);
    }

    public ItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TextView textView = new TextView(context);
        addView(textView);

        textView.setText("xxxxxxxxxxxxxxxxxx");
    }
}
