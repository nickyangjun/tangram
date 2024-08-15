package com.kks.sample;

import android.content.Context;
import android.view.ContextThemeWrapper;

import androidx.fragment.app.FragmentActivity;

public class CommonUtil {

    /**
     * dip转为 px
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static FragmentActivity getBaseActivity(Context context) {
        if (context instanceof FragmentActivity) {
            return (FragmentActivity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return (FragmentActivity) ((ContextThemeWrapper) context).getBaseContext();
        } else {
            return null;
        }
    }
}
