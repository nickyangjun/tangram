package com.kks.sample.tangram.support;

import android.text.TextUtils;
import android.view.View;

import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.support.CardSupport;

public class CardBackgroundSupport extends CardSupport {
    @Override
    public void onBindBackgroundView(View layoutView, Card card) {
        if(card.style !=null && TextUtils.isDigitsOnly(card.style.bgImgUrl)){ //如果背景都是数字，则是本地drawableId
            try {
                layoutView.setBackgroundResource(Integer.parseInt(card.style.bgImgUrl));
            }catch (Exception e){

            }
        }
    }
}
