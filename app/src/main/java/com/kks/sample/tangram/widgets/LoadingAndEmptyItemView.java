package com.kks.sample.tangram.widgets;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.kks.sample.CommonUtil;
import com.kks.sample.tangram.card.LoadingAndEmptyCard;
import com.kks.sample.tangram.card.TDataHelper;
import com.kks.sample.R;
import com.tmall.wireless.tangram.TangramEngine;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.view.ITangramViewLifeCycle;

/**
 * 文件名：LoadingItemView
 * 描述：加载更多 或者 显示空View
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/28
 * 版权：小水滴
 */
public class LoadingAndEmptyItemView extends FrameLayout implements ITangramViewLifeCycle {
    public final static String TYPE = "LoadingAndEmptyItemView";
    private int[] firstVisiblePos;

    private boolean isEmptyViewVisible;
    private boolean isLoadingViewVisible;

    // loading
    private ProgressBar pbLoading;
    private TextView tvLoading;
    private LinearLayout llEnd;
    private TextView tvEndTip;

    // empty
    private ViewGroup emptyContainer;
    private ImageView emptyImage;
    private TextView emptyText;
    private ProgressBar emptyPbLoading;
//    private ViewStub viewStub;
    private View systemView;
    private Button emptyButton;

    private LoadingAndEmptyCard loadingAndEmptyCard;

    public LoadingAndEmptyItemView(@NonNull Context context) {
        this(context, null);
    }

    public LoadingAndEmptyItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingAndEmptyItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (isEmptyViewVisible) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }

        int firstVisibleItemPos = 0;
        RecyclerView.LayoutManager manager = ((RecyclerView) this.getParent()).getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) manager;
            firstVisibleItemPos = linearLayoutManager.findFirstVisibleItemPosition();
        } else if (manager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) manager;
            firstVisibleItemPos = gridLayoutManager.findFirstVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) manager;
            if (firstVisiblePos == null) {
                firstVisiblePos = new int[staggeredGridLayoutManager.getSpanCount()];
            }
            staggeredGridLayoutManager.findFirstVisibleItemPositions(firstVisiblePos);
            firstVisibleItemPos = firstVisiblePos[0];
        }

        // 当前LoadingView的高度
        int parentHeight = ((RecyclerView) this.getParent()).getHeight();
        //控制不满一屏时，不显示
        if (bottom < parentHeight && firstVisibleItemPos == 0) {
            setVisibility(GONE);
            return;
        } else if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    private void initLoadingView() {
        isLoadingViewVisible = true;
        isEmptyViewVisible = false;
        inflate(getContext(), R.layout.fou_item_loading_more, this);
        pbLoading = findViewById(R.id.pb_loading);
        tvLoading = findViewById(R.id.tv_loading);
        llEnd = findViewById(R.id.ll_end);
        tvEndTip = findViewById(R.id.footer_txt);
    }

    private void initEmptyView(BaseCell cell) {
        isLoadingViewVisible = false;
        isEmptyViewVisible = true;
        EmptyView emptyView = new EmptyView(getContext(), ((TangramEngine) cell.parent.serviceManager).getContentView());
        emptyView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        inflate(getContext(), R.layout.fou_item_loading_empty, emptyView);
        addView(emptyView);
        if (loadingAndEmptyCard != null && loadingAndEmptyCard.emptyViewBackgroundResId > 0) {
            emptyView.setBackgroundResource(loadingAndEmptyCard.emptyViewBackgroundResId);
        }
        emptyContainer = findViewById(R.id.container);
        emptyImage = findViewById(R.id.image);
        emptyText = findViewById(R.id.text);
        if (loadingAndEmptyCard != null && loadingAndEmptyCard.emptyTextColorId != 0) {
            emptyText.setTextColor(getResources().getColor(loadingAndEmptyCard.emptyTextColorId));
        }
        emptyPbLoading = findViewById(R.id.pb_loading);
        emptyButton = findViewById(R.id.btn_ok);
//        viewStub = findViewById(R.id.vs_system_upgrade);
        if (loadingAndEmptyCard.emptyViewMarginTop > 0) {
            LayoutParams lp = (LayoutParams) emptyContainer.getLayoutParams();
            lp.topMargin = loadingAndEmptyCard.emptyViewMarginTop;
            lp.gravity = Gravity.CENTER_HORIZONTAL;
        }
        if (loadingAndEmptyCard.emptyBtnTextColorId > 0) {
            emptyButton.setTextColor(getResources().getColor(loadingAndEmptyCard.emptyBtnTextColorId));
        }
        if (loadingAndEmptyCard.emptyBtnBackgroundResId > 0) {
            emptyButton.setBackgroundResource(loadingAndEmptyCard.emptyBtnBackgroundResId);
        } else if (loadingAndEmptyCard.emptyBtnBackgroundResId == -1) {
            ((LinearLayout.LayoutParams) emptyButton.getLayoutParams()).topMargin = CommonUtil.dip2px(getContext(),10f);
            emptyButton.setBackground(null);
        }
    }

    @Override
    public void cellInited(BaseCell cell) {
        loadingAndEmptyCard = TDataHelper.getLoadingAndEmptyCard(cell);

        setPadding(loadingAndEmptyCard.padding[0], loadingAndEmptyCard.padding[1], loadingAndEmptyCard.padding[2], loadingAndEmptyCard.padding[3]);

        if (loadingAndEmptyCard.emptyViewEnable) { //允许显示empty view
            int itemCount = ((TangramEngine) cell.parent.serviceManager).getContentView().getChildCount();
            if (itemCount == 0) { //显示空view
                if (!isEmptyViewVisible) {  //如果没有显示空view
                    removeAllViews();
                    initEmptyView(cell);
                }
                return;
            }
        }

        if (!isLoadingViewVisible) {
            removeAllViews();
            initLoadingView();
        }
    }

    @Override
    public void postBindView(BaseCell cell) {
        setVisibility(VISIBLE);
        if (isEmptyViewVisible) {  //空view
            if (loadingAndEmptyCard.loadingState == LoadingAndEmptyCard.State.LOADING.getCode()) {
                emptyContainer.setVisibility(GONE);
                emptyPbLoading.setVisibility(VISIBLE);
            } else if (loadingAndEmptyCard.loadingState == LoadingAndEmptyCard.State.LOADING_NETWORK_ERROR.getCode()) {
                emptyPbLoading.setVisibility(View.GONE);
                emptyImage.setImageResource(loadingAndEmptyCard.emptyNetErrImageRId);
                if (!TextUtils.isEmpty(loadingAndEmptyCard.emptyNetErrText)) {
                    emptyText.setText(loadingAndEmptyCard.emptyNetErrText);
                } else {
                    emptyText.setText(loadingAndEmptyCard.emptyNetErrTextRId);
                }

                emptyContainer.setVisibility(View.VISIBLE);

                if (loadingAndEmptyCard.emptyViewNetErrClick != null) {
                    emptyContainer.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadingAndEmptyCard.loadingState = LoadingAndEmptyCard.State.LOADING.getCode();
                            emptyContainer.setVisibility(GONE);
                            emptyPbLoading.setVisibility(VISIBLE);
                            loadingAndEmptyCard.emptyViewNetErrClick.onClick(v);
                        }
                    });
                }

            } else if (loadingAndEmptyCard.loadingState == LoadingAndEmptyCard.State.LOADING_SYSTEM_UPGRADE.getCode()) {
//                emptyPbLoading.setVisibility(View.GONE);
//                emptyContainer.setVisibility(GONE);
//                if (systemView == null) {
//                    systemView = viewStub.inflate();
//                }
//                systemView.setVisibility(View.VISIBLE);
//                if (loadingAndEmptyCard.emptyViewNetErrClick != null) {
//                    systemView.findViewById(R.id.btn_reload).setOnClickListener(new OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            loadingAndEmptyCard.loadingState = LoadingAndEmptyCard.State.LOADING.getCode();
//                            systemView.setVisibility(GONE);
//                            emptyPbLoading.setVisibility(VISIBLE);
//                            loadingAndEmptyCard.emptyViewNetErrClick.onClick(view);
//                        }
//                    });
//                }

            } else {
                emptyPbLoading.setVisibility(View.GONE);
                emptyImage.setImageResource(loadingAndEmptyCard.emptyImageRId);
                emptyText.setText(loadingAndEmptyCard.emptyTextRId);
                emptyContainer.setVisibility(View.VISIBLE);

                if (loadingAndEmptyCard.emptyBtnVisible) {
                    emptyButton.setVisibility(VISIBLE);
                    emptyButton.setText(loadingAndEmptyCard.emptyBtnTextRId);
                    if (loadingAndEmptyCard.emptyViewBtnClick != null) {
                        emptyButton.setOnClickListener(loadingAndEmptyCard.emptyViewBtnClick);
                    }
                } else {
                    emptyButton.setVisibility(GONE);
                }
            }
        } else {
            if (loadingAndEmptyCard.loadingState == LoadingAndEmptyCard.State.LOADING.getCode()) { // 正在加载
                pbLoading.setVisibility(View.VISIBLE);
                tvLoading.setVisibility(View.VISIBLE);
                llEnd.setVisibility(View.GONE);
            } else if (loadingAndEmptyCard.loadingState == LoadingAndEmptyCard.State.LOADING_COMPLETE.getCode()) {  // 本次加载完成
                pbLoading.setVisibility(View.INVISIBLE);
                tvLoading.setVisibility(View.INVISIBLE);
                llEnd.setVisibility(View.GONE);
            } else if (loadingAndEmptyCard.loadingState == LoadingAndEmptyCard.State.LOADING_END.getCode()) {  // 加载到底
                pbLoading.setVisibility(View.GONE);
                if (loadingAndEmptyCard.loadDoneVisible) {
                    tvLoading.setVisibility(View.GONE);
                    llEnd.setVisibility(View.VISIBLE);
                    tvEndTip.setText(loadingAndEmptyCard.loadDoneTextRId);
                } else {
                    tvLoading.setVisibility(View.GONE);
                    llEnd.setVisibility(View.INVISIBLE);
                }
            } else if (loadingAndEmptyCard.loadingState == LoadingAndEmptyCard.State.LOADING_NETWORK_ERROR.getCode()) {  // 网络超时，无返回包
                pbLoading.setVisibility(View.GONE);
                tvLoading.setVisibility(View.GONE);
                llEnd.setVisibility(View.VISIBLE);
                tvEndTip.setText(loadingAndEmptyCard.loadDoneErrTextRId);
            }
        }
    }

    @Override
    public void postUnBindView(BaseCell cell) {

    }
}
