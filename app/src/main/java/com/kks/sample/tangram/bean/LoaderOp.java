package com.kks.sample.tangram.bean;

import com.kks.sample.tangram.Page;
import com.tmall.wireless.tangram.dataparser.concrete.Card;

/**
 * 文件名：LoaderOp
 * 描述：用于 rx 传递参数
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/6/13
 * 版权：小水滴
 */
public class LoaderOp<V> {
    public Card card;
    public Page.Loader loader;
    public V tangramCallback;
    public int pageNo;

    public LoaderOp(Card card, Page.Loader loader, V v){
        this.card = card;
        this.loader = loader;
        this.tangramCallback = v;
    }

    public LoaderOp(int pageNo, Card card, Page.Loader loader, V v){
        this.pageNo = pageNo;
        this.card = card;
        this.loader = loader;
        this.tangramCallback = v;
    }
}
