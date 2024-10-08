/*
 * MIT License
 *
 * Copyright (c) 2018 Alibaba Group
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tmall.wireless.tangram.support;

import com.tmall.wireless.tangram.support.RxBannerScrolledListener.ScrollEvent;

import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * Created by longerian on 2018/3/14.
 *
 * @author longerian
 * @date 2018/03/14
 */

public class BannerScrolledObservable extends Observable<ScrollEvent> {

    private final RxBannerListener mBannerListener;

    public BannerScrolledObservable(RxBannerScrolledListener bannerListener) {
        mBannerListener = bannerListener;
    }

    @Override
    protected void subscribeActual(Observer<? super ScrollEvent> observer) {
        observer.onSubscribe(mBannerListener);
        mBannerListener.addObserver(observer);
    }

}
