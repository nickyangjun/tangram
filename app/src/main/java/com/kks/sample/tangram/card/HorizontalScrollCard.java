package com.kks.sample.tangram.card;

import android.view.ViewGroup;

import com.alibaba.fastjson.annotation.JSONField;
import com.tmall.wireless.tangram.TangramBuilder;

/**
 * 文件名：LinearScrollCard
 * 描述：水平滑动的布局， tangram 内部实现原理是recyclerview + recyclerview
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/24
 * 版权：小水滴
 */
public class HorizontalScrollCard<T> extends BaseCard<T> {
    @JSONField(serialize=false)
    public static final String DEFAULT_ID = "HorizontalScrollCard"; //同一个页面可能有多个相同card, id 必须不同

    public String id = DEFAULT_ID;
    public String type = TangramBuilder.TYPE_CONTAINER_SCROLL;
    public String load = "com.yy.tangram";
    public int loadType = LOAD_ASYNC;  // 1 分页加载, -1 异步加载
    public Style style = new Style();

    /**
     * 可以添加一个头部
     **/
    public Header header = null;

    public HorizontalScrollCard(){}

    public HorizontalScrollCard(int loadType){
        this.loadType = loadType;
    }

    @JSONField(serialize = false)
    public Header addHeader(){
        this.header = new Header();
        return this.header;
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

    public class Style {
        public int pageWidth = 250;
        public double pageHeight = ViewGroup.LayoutParams.WRAP_CONTENT;  //自适应高度

        public boolean hasIndicator = false; // 是否有指示器

        public String bgColor = "#00000000";

        //最左边一帧距离布局左边的间距
        public int scrollMarginLeft =  0;
        //最右边一帧距离布局右边的间距
        public int scrollMarginRight = 0;

        public int[] margin = {0, 0, 0, 0}; // top , right, bottom, left
        public int[] padding = {0, 0, 0, 0}; // top , right, bottom, left

        public String bgImgUrl = "xxxx";
    }
}
