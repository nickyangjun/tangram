package com.kks.sample.tangram.card;

import com.alibaba.fastjson.annotation.JSONField;

public class Header {
    /**
     * 头部view
     */
    public String type = "";    // 必填 注册进tangram 的 item view type
    public Object data = null;  // 额外参数， 从 BaseCell 的 extras 字段获取

    @JSONField(serialize = false)
    public Header setType(String type) {
        this.type = type;
        return this;
    }

    @JSONField(serialize = false)
    public Header setData(Object data) {
        this.data = data;
        return this;
    }
}
