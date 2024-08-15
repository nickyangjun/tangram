package com.kks.sample.tangram.support;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tmall.wireless.tangram.util.IInnerImageSetter;

/**
 * 文件名：ImageSupport
 * 描述：
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/6/20
 * 版权：小水滴
 */
public class ImageSupport implements IInnerImageSetter {
    @Override
    public <IMAGE extends ImageView> void doLoadImageUrl(@NonNull IMAGE view, @Nullable String url) {

    }
}
