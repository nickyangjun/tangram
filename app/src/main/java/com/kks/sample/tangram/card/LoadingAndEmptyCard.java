package com.kks.sample.tangram.card;

import android.view.View;

import com.alibaba.fastjson.annotation.JSONField;
import com.kks.sample.R;
import com.kks.sample.tangram.widgets.LoadingAndEmptyItemView;


/**
 * 文件名：LoadingAndEmptyCard
 * 描述：
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/28
 * 版权：小水滴
 */
public class LoadingAndEmptyCard implements ICard {
    @JSONField(serialize = false)
    public static final String DEFAULT_ID = "LoadingMore"; //同一个页面可能有多个相同card, id 必须不同

    // 内容的padding
    public int[] padding = {0, 0, 0, 0}; // left, top , right, bottom

    public String id = DEFAULT_ID;
    public final String type = LoadingAndEmptyItemView.TYPE;
    public int loadingState = State.LOADING.getCode();
    public boolean loadDoneVisible = true;  // 数据加载完成后，是否显示没有数据了
    public int loadDoneTextRId = R.string.no_more;  //没有更多了文案
    public int loadDoneErrTextRId = R.string.unstable_network;  //加载网络错误文案

    /**
     * 空 view
     */
    public boolean emptyViewEnable = true; //是否显示空view
    public int emptyViewMarginTop = 0;  //默认居中，如果 emptyViewMarginTop > 0 则取消居中，由 emptyViewMarginTop 控制
    public int emptyViewBackgroundResId = R.color.white; //空view背景颜色
    public int emptyTextRId = R.string.no_data;
    public int emptyTextColorId = 0;
    public int emptyImageRId = R.drawable.ic_default_empty;
    public int emptyNetErrTextRId = R.string.unstable_network;  //网络错误文字 R id
    public String emptyNetErrText = "";  //网络错误文字
    public int emptyNetErrImageRId = R.drawable.ic_net_failed;    //网络错误图标

    public boolean emptyBtnVisible = false;  //是否线索空view中的按钮
    public int emptyBtnTextRId = 0;  //按钮文字
    public int emptyBtnTextColorId = 0;  //按钮文字颜色
    public int emptyBtnBackgroundResId = 0;  //按钮文字背景  0 默认主题按钮色， -1 置为空
    @JSONField(serialize = false)
    public View.OnClickListener emptyViewBtnClick;  //按钮点击事件

    @JSONField(serialize = false)
    public View.OnClickListener emptyViewNetErrClick;

    @Override
    public String getId() {
        return id;
    }

    public LoadingAndEmptyCard() {
    }

    public LoadingAndEmptyCard(int emptyImageRId, int emptyTextRId) {
        this.emptyImageRId = emptyImageRId;
        this.emptyTextRId = emptyTextRId;
    }

    public LoadingAndEmptyCard(int emptyImageRId, int emptyTextRId, int emptyViewBackgroundResId) {
        this.emptyImageRId = emptyImageRId;
        this.emptyTextRId = emptyTextRId;
        this.emptyViewBackgroundResId = emptyViewBackgroundResId;
    }

    public enum State {

        /**
         * 正在加载
         */
        LOADING(0),

        /**
         * 本次加载完成
         */
        LOADING_COMPLETE(1),

        /**
         * 加载到底，没有更多了
         */
        LOADING_END(2),

        /**
         * 网络超时
         */
        LOADING_NETWORK_ERROR(3),

        /**
         * 系统维护中
         */
        LOADING_SYSTEM_UPGRADE(4);

        int code;

        State(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
