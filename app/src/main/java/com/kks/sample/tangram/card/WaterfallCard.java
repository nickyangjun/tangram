package com.kks.sample.tangram.card;

import com.alibaba.fastjson.annotation.JSONField;
import com.tmall.wireless.tangram.TangramBuilder;

/**
 * 文件名：WaterfallCard
 * 描述：瀑布流布局的layout
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/24
 * 版权：小水滴
 */
public class WaterfallCard<T> extends BaseCard<T> {
    @JSONField(serialize = false)
    public static final String DEFAULT_ID = "WaterfallCard"; //同一个页面可能有多个相同card, id 必须不同

    public String id = DEFAULT_ID;
    public String type = TangramBuilder.TYPE_CONTAINER_WATERFALL;
    public String load = "com.yy.tangram";
    public int loadType = LOAD_ASYNC;
    public Style style = new Style();

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
        public int hGap = 10;
        public int vGap = 10;
        public int column = 2;
        public String bgColor = "#00000000";
        public int[] margin = {10, 10, 10, 10}; // top , right, bottom, left
        public int[] padding = {0, 0, 0, 0};
    }
}
