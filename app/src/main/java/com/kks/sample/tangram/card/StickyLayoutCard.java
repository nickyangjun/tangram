package com.kks.sample.tangram.card;

import com.alibaba.fastjson.annotation.JSONField;
import com.tmall.wireless.tangram.TangramBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认第一个item 为 sticky view
 * @param <T>
 */
public class StickyLayoutCard<T> extends BaseCard<T> {
    @JSONField(serialize = false)
    public static final String DEFAULT_ID = "StickyLayoutCard"; //同一个页面可能有多个相同card, id 必须不同

    public String id = DEFAULT_ID;
    public String type = TangramBuilder.TYPE_CONTAINER_STICKY;
    public String load = "com.yy.tangram";
    public int loadType = LOAD_ASYNC;  // 1 分页加载, -1 异步加载
    public Style style = new Style();

    public List<Object> items = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getLoadType() {
        return loadType;
    }

    @Override
    public void setLoadType(int loadType) {
        this.loadType = loadType;
    }


    public class Style {
        public String sticky = "start";  // start , end
        public String bgColor = "#00000000";
        public String bgImgUrl = "";   //如果背景都是数字，则是本地drawableId
        public int[] margin = {0, 0, 0, 0}; // top , right, bottom, left
        public int[] padding = {0, 0, 0, 0}; // top , right, bottom, left
        public int offset = 0;
    }

}
