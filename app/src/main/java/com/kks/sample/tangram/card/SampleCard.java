package com.kks.sample.tangram.card;


import com.kks.sample.tangram.widgets.SampleItemView;

/**
 * 文件名：SampleCard
 * 描述：简单的 item card, 用于单独的一行View
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/6/12
 * 版权：小水滴
 */
public class SampleCard implements ICard {
    public String id;
    public String type;
    public Style style = new Style();
    public int itemLayoutResId;

    /**
     * 自己指定显示的 view type
     *
     * @param type
     */
    public SampleCard(String type) {
        this.id = type;
        this.type = type;
    }

    /**
     * 自己指定显示的 view type
     *
     * @param id
     * @param type
     */
    public SampleCard(String id, String type) {
        this.id = id;
        this.type = type;
    }


    /**
     * 默认显示 SampleItemView, SampleItemView 使用 itemLayoutResId 初始化
     *
     * @param layoutResId
     */
    public SampleCard(int layoutResId) {
        this.id = getCardId(layoutResId);
        this.type = SampleItemView.TYPE;
        this.itemLayoutResId = layoutResId;
    }

    @Override
    public String getId() {
        return id;
    }

    public static String getCardId(int layoutResId) {
        return SampleItemView.TYPE + "#" + layoutResId;
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
