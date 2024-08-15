package com.kks.sample.tangram.card;


import com.alibaba.fastjson.annotation.JSONField;
import com.kks.sample.R;
import com.tmall.wireless.tangram.TangramBuilder;


/**
 * 文件名：BannerCard
 * 描述：banner布局的layout
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/24
 * 版权：小水滴
 */
public class BannerCard<T> extends BaseCard<T> {
    @JSONField(serialize=false)
    public static final String DEFAULT_ID = "BannerCard"; //同一个页面可能有多个相同card, id 必须不同

    public String id = DEFAULT_ID;
    public String type = TangramBuilder.TYPE_CONTAINER_BANNER;
    public String load = "com.yy.tangram";
    public int loadType = LOAD_ASYNC;
    public Style style = new Style();

    /**
     * 可以添加一个头部
     **/
    public Header header = null;

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
        /**
         * 内部 View 的宽高比，宽度按满屏幕根据ratio计算高度，建议设置此属性，可预先占位，避免 Banner 有一个高度撑开的过程
         *
         * Double.NaN 代表自适应高度
         */
        public float itemRatio = (float) Double.NaN; // 2.654f;
        public int pageWidth = 200;

        /**
         * banner 的背景
         */
        public int backgroundDrawableId = 0;
        public float elevation = 0f;  //float 阴影效果

        /**
         * 数据加载更新后，可以手动设置这个标记位, 这样又会重 banner 第一个开始轮转
         */
        public boolean updateInitFlag = false;

        //是否无限滚动
        public boolean infinite = false;
        public int infiniteMinCount = 1;  //最小循环滑动个数

        public int autoScroll = 0;  //自动循环时间 单位 ms
        public int autoScrollMinCount = 1;  //最小自动滑动个数

        // 是否有指示器
        public boolean hasIndicator = true;

        /**
         * 指示器
         */
        public String indicatorGravity = "center";
        public int indicatorMargin = 10;

        /**
         * 最高优先级， true 使用数字指示器, 使用 textView 实现
         */
        public boolean indicatorNum = true;
        public int backgroundResId = R.drawable.indicator_bg; //背景
        public String numTextColor = "#fff"; //文字颜色
        public int numTextSize = 12; //文字大小

        /**
         * 点指示器半径，
         * 指示器大小：  width = 2 * indicatorRadius
         *             height = ( indicatorHeight == 0 ) ？2 * indicatorRadius : indicatorHeight
         *
         * 当 indicatorRadius indicatorColor defaultIndicatorColor 都大于0时，优先使用，优先值大于图片指示器
         */
        public float indicatorRadius = 3f;  // 点指示器半径
        public String indicatorColor = "#ffffff";  //选中颜色
        public String defaultIndicatorColor = "#a0a0a0";  // 默认颜色

        /**
         * 图片指示器
         */
        //指示器选中状态的图片，必须带图片宽高比后缀
        public String indicatorImg1 = "https://img.alicdn.com/tps/TB16i4qNXXXXXbBXFXXXXXXXXXX-32-4.png";
        //指示器未被选中状态的图片，必须带图片宽高比后缀
        public String indicatorImg2 = "https://img.alicdn.com/tps/TB1XRNFNXXXXXXKXXXXXXXXXXXX-32-4.png";

        /**
         * 图片指示器
         */
        public int indicatorLocalResId = R.drawable.ic_scoll_short;
        public int indicatorSelectedLocalResId = R.drawable.ic_scoll_long;

        //指示器间距
        public int indicatorGap = 2;
        //指示器高度
        public float indicatorHeight = 0;

        //指示器宽度 px， 只数值指示器有效
        public float indicatorWidth = 110;

        //最左边一帧距离布局左边的间距
        public int scrollMarginLeft = 0;

        //最右边一帧距离布局右边的间距
        public int scrollMarginRight = 0;
        //横向每一帧之间的间距
        public int hGap = 0;
    }
}
