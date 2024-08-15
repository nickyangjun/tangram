package com.kks.sample.tangram.card;

/**
 * 文件名：TData
 * 描述：代表一个组件， 所以需要一个 type 对应一个 View
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/24
 * 版权：小水滴
 */
public class TData<T> {
    public String type;
    public T data;

    public TData(String type, T t) {
        this.type = type;
        this.data = t;
    }

    public static <T> TData<T> getInstance(String type, T t) {
        return new TData<>(type, t);
    }
}
