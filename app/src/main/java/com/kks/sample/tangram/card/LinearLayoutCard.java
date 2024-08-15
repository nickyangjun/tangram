package com.kks.sample.tangram.card;

import com.alibaba.fastjson.annotation.JSONField;
import com.tmall.wireless.tangram.TangramBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名：LinearLayoutCard
 * 描述：线性布局的layout
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/24
 * 版权：小水滴
 */
public class LinearLayoutCard<T> extends BaseCard<T> {
    @JSONField(serialize=false)
    public static final String DEFAULT_ID = "LinearLayoutCard"; //同一个页面可能有多个相同card, id 必须不同

    public String id = DEFAULT_ID;
    public String type = TangramBuilder.TYPE_CONTAINER_1C_FLOW;
    public String load = "com.yy.tangram";
    public int loadType = LOAD_ASYNC;  // 1 分页加载, -1 异步加载
    public Style style = new Style();

    /**
     * 可以添加一个头部
     **/
    public Header header = null;

    public List<Object> items = new ArrayList<>();

    public LinearLayoutCard(){}

    public LinearLayoutCard(int loadType){
        this.loadType = loadType;
    }

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

    @JSONField(serialize = false)
    public Header addHeader(){
        this.header = new Header();
        return this.header;
    }

    public class Style {
        public int hGap = 0;
        public int vGap = 0;
        public String bgColor = "#00000000";
        public String bgImgUrl = "";   //如果背景都是数字，则是本地drawableId
        public int[] margin = {0, 0, 0, 0}; // top , right, bottom, left
        public int[] padding = {0, 0, 0, 0}; // top , right, bottom, left
    }
}
