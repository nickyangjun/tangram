package com.kks.sample.tangram.card;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名：BaseCard
 * 描述：
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/24
 * 版权：小水滴
 */
public abstract class BaseCard<T> implements ICard {
    public static final int LOAD_ASYNC = -1; //异步加载
    public static final int LOAD_PAGE = 1; //分页加载

    private List<TData<T>> items = new ArrayList<>();

    public List<TData<T>> getItems() {
        return items;
    }

    public void setItems(String type, List<T> items) {
        for (T t : items) {
            this.items.add(new TData<>(type, t));
        }
    }

    public abstract String getId();

    public abstract int getLoadType();

    public abstract void setLoadType(int loadType);
}
