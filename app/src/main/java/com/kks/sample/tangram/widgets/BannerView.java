package com.kks.sample.tangram.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.ultraviewpager.TimerHandler;
import com.tmall.wireless.tangram.core.R;
import com.tmall.wireless.tangram.core.adapter.BinderViewHolder;
import com.tmall.wireless.tangram.core.adapter.GroupBasicAdapter;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import com.tmall.wireless.tangram.eventbus.BusSupport;
import com.tmall.wireless.tangram.eventbus.EventContext;
import com.tmall.wireless.tangram.ext.BannerListener;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.cell.BannerCell;
import com.tmall.wireless.tangram.structure.view.ITangramViewLifeCycle;
import com.tmall.wireless.tangram.support.BannerSupport;
import com.tmall.wireless.tangram.util.ImageUtils;
import com.tmall.wireless.tangram.util.Utils;
import com.tmall.wireless.tangram.view.BannerViewPager;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名：BannerView
 * 描述： 仿 tangram bannerView 修改了indicator
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/6/29
 * 版权：小水滴
 */
public class BannerView extends ViewGroup implements ViewPager.OnPageChangeListener, TimerHandler.TimerHandlerListener,
        ITangramViewLifeCycle {

    private static final String CURRENT_POS = "__current_pos__";

    public static final int GRAVITY_LEFT = 0;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_RIGHT = 2;

    private boolean isIndicatorOutside;
    private BannerViewPager mUltraViewPager;
    private BannerIndicator mIndicator;

    private boolean isIndicatorNum;
    private int mIndicatorBackgroundResId = 0;
    private int mIndicatorNumColor = 0xf0f0f0;
    private int mIndicatorNumSize = Style.dp2px(3);
    private int mIndicatorHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
    private int mIndicatorWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
    private int mIndicatorGap = Style.dp2px(6);
    private int mIndicatorMargin = Style.dp2px(10);
    private int mIndicatorLocalResId;
    private int mIndicatorSelectedLocalResId;
    private int autoScrollMinCount; //最小自动滑动到下一个个数
    private boolean updateInitFlag;  //数据更新后表示
    private int backgroundDrawableId;
    private boolean hasIndicator;
    private float elevation;

    private float xDown;
    private float yDown;

    private float ratio = Float.NaN;

    private int height = VirtualLayoutManager.LayoutParams.WRAP_CONTENT;

    protected BaseCell cell;

    private BannerSupport bannerSupport;

    private List<BinderViewHolder> mHeaderViewHolders = new ArrayList<>();

    private List<BinderViewHolder> mFooterViewHolders = new ArrayList<>();

    private int currentItemPos;

    private boolean init;

    private int direction; // > 0 for right, < 0 for left

    private TimerHandler timer;

    private ScreenBroadcastReceiver mScreenBroadcastReceiver;

    private IntentFilter filter = new IntentFilter();

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mUltraViewPager = new BannerViewPager(getContext());
        mUltraViewPager.setId(R.id.TANGRAM_BANNER_ID);
        mIndicator = createIndicator();
        addView(mUltraViewPager);
        addView(mIndicator);
        mIndicator.setPadding(mIndicatorGap, 0, 0, 0);
        mScreenBroadcastReceiver = new ScreenBroadcastReceiver(this);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
    }

    protected BannerIndicator createIndicator() {
        return new BannerIndicator(getContext());
    }

    public void setAdapter(PagerAdapter adapter) {
        mUltraViewPager.setAdapter(adapter);
        disableAutoScroll();//reset timer when reuse
        mUltraViewPager.removeOnPageChangeListener(this);
        mUltraViewPager.addOnPageChangeListener(this);
    }

    public void updateIndicators(String focusUrl, String norUrl, int radius, int focusColor, int norColor) {
        if (mIndicator != null) {
            mIndicator.updateIndicators(focusUrl, norUrl, radius, focusColor, norColor);
        }
    }

    public void setIndicatorGravity(int gravity) {
        switch (gravity) {
            case GRAVITY_LEFT:
                if (mIndicator != null) {
                    mIndicator.setGravity(Gravity.LEFT);
                }
                break;
            case GRAVITY_CENTER:
                if (mIndicator != null) {
                    mIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
                }
                break;
            case GRAVITY_RIGHT:
                if (mIndicator != null) {
                    mIndicator.setGravity(Gravity.RIGHT);
                }
                break;
            default:
                break;
        }
    }

    public void setIndicatorPos(String isInside) {
        if ("inside".equals(isInside)) {
            isIndicatorOutside = false;
        } else if ("outside".equals(isInside)) {
            isIndicatorOutside = true;
        } else {
            isIndicatorOutside = false;
        }
    }

    public void setIndicatorGap(int gap) {
        if (gap > 0) {
            this.mIndicatorGap = gap;
        }
    }

    public void setIndicatorMargin(int indicatorMargin) {
        if (indicatorMargin > 0) {
            this.mIndicatorMargin = indicatorMargin;
        }
    }

    public void setIndicatorHeight(int indicatorHeight) {
        if (indicatorHeight > 0) {
            mIndicatorHeight = indicatorHeight;
        } else {
            mIndicatorHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }


    public BannerViewPager getUltraViewPager() {
        return mUltraViewPager;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (bannerSupport != null) {
            for (int j = 0; j < bannerSupport.getListeners().size(); j++) {
                BannerListener listener = bannerSupport.getListeners().get(j);
                listener.onPageScrolled(currentItemPos, positionOffset, positionOffsetPixels, direction);
            }
        }
        if (bannerSupport != null) {
            List<BannerListener> listeners = bannerSupport.getScrolledListenerById(cell.id);
            if (listeners != null) {
                for (int i = 0; i < listeners.size(); i++) {
                    BannerListener listener = listeners.get(i);
                    listener.onPageScrolled(currentItemPos, positionOffset, positionOffsetPixels, direction);
                }
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        currentItemPos = mUltraViewPager.getCurrentItem();
        mIndicator.setCurrItem(currentItemPos);

        if (cell != null && cell.extras != null) {
            try {
                cell.extras.put(CURRENT_POS, currentItemPos);
            } catch (JSONException e) {
            }
        }

        if (bannerSupport != null) {
            for (int j = 0; j < bannerSupport.getListeners().size(); j++) {
                BannerListener listener = bannerSupport.getListeners().get(j);
                listener.onPageSelected(currentItemPos);
            }
        }
        if (cell != null && cell.serviceManager != null) {
            BusSupport busSupport = cell.serviceManager.getService(BusSupport.class);
            if (busSupport != null) {
                EventContext eventContext = new EventContext();
                if (((BannerCell)cell).mCells != null && currentItemPos >= 0
                        && currentItemPos < ((BannerCell)cell).mCells.size()) {
                    eventContext.producer = ((BannerCell)cell).mCells.get(currentItemPos);
                }
                busSupport.post(BusSupport.obtainEvent(BusSupport.EVENT_ON_EXPOSURE, cell.id, null, eventContext));
            }
        }
        if (bannerSupport != null) {
            List<BannerListener> listeners = bannerSupport.getSelectedListenerById(cell.id);
            if (listeners != null) {
                for (int i = 0; i < listeners.size(); i ++) {
                    BannerListener listener = listeners.get(i);
                    listener.onPageSelected(currentItemPos);
                }
            }
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (bannerSupport != null) {
            for (int j = 0; j < bannerSupport.getListeners().size(); j++) {
                BannerListener listener = bannerSupport.getListeners().get(j);
                listener.onPageScrollStateChanged(state);
            }
        }
        if (bannerSupport != null) {
            List<BannerListener> listeners = bannerSupport.getScrollStateChangedListenerById(cell.id);
            if (listeners != null) {
                for (int i = 0; i < listeners.size(); i ++) {
                    BannerListener listener = listeners.get(i);
                    listener.onPageScrollStateChanged(state);
                }
            }
        }
    }

    @Override
    public void cellInited(BaseCell cell) {
        this.init = (this.cell != cell);
        this.cell = cell;
        if(cell.parent.style != null && cell.parent.style.extras !=null) {
            try {
                updateInitFlag = cell.parent.style.extras.optBoolean("updateInitFlag", false);
                isIndicatorNum = cell.parent.style.extras.optBoolean("indicatorNum", false);
                mIndicatorWidth = cell.parent.style.extras.getInt("indicatorWidth");
                mIndicatorBackgroundResId = cell.parent.style.extras.getInt("backgroundResId");
                mIndicatorNumColor = Style.parseColor(cell.parent.style.extras.optString("numTextColor", "#fff"));
                mIndicatorNumSize = cell.parent.style.extras.getInt("numTextSize");
                mIndicatorLocalResId = cell.parent.style.extras.getInt("indicatorLocalResId");
                mIndicatorSelectedLocalResId = cell.parent.style.extras.getInt("indicatorSelectedLocalResId");
                autoScrollMinCount = cell.parent.style.extras.getInt("autoScrollMinCount");
                backgroundDrawableId = cell.parent.style.extras.getInt("backgroundDrawableId");
                hasIndicator = cell.parent.style.extras.optBoolean("hasIndicator", true);
                elevation = (float) cell.parent.style.extras.optDouble("elevation");
                //从置标志位
                cell.parent.style.extras.putOpt("updateInitFlag", false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void postBindView(BaseCell cell) {
        getContext().registerReceiver(mScreenBroadcastReceiver, filter);
        BannerCell bannerCell = (BannerCell) cell;
        bannerCell.initAdapter();
        if (cell.style != null) {
            setPadding(cell.style.padding[3], cell.style.padding[0], cell.style.padding[1], cell.style.padding[2]);
        }
        if(backgroundDrawableId > 0) {
            setBackground(getResources().getDrawable(backgroundDrawableId));
        }else {
            setBackgroundColor(bannerCell.mBgColor);
        }

        if(elevation > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setElevation(elevation);
            }
        }
        setAdapter(bannerCell.mBannerWrapper);
        mUltraViewPager.setAutoMeasureHeight(true);
        this.ratio = bannerCell.mRatio;
        this.height = bannerCell.height;
        mUltraViewPager.setRatio(this.ratio);
        if(bannerCell.mCells.size() <= autoScrollMinCount){
            disableAutoScroll();
        }else {
            setAutoScroll(bannerCell.mAutoScrollInternal, bannerCell.mSpecialInterval);
        }
        mUltraViewPager.setPageMargin(bannerCell.hGap);
        if (bannerCell.mCells.size() <= bannerCell.mInfiniteMinCount) {
            setInfiniteLoop(false);
        } else {
            setInfiniteLoop(bannerCell.mInfinite);
        }
        setIndicatorGravity(getIndicatorGravity(bannerCell.mIndicatorGravity));
        setIndicatorPos(bannerCell.mIndicatorPos);
        int indicatorGap = bannerCell.mIndicatorGap;
        if (indicatorGap <= 0) {
            indicatorGap = mIndicatorGap;
        }
        setIndicatorGap(indicatorGap);
        int indicatorMargin = bannerCell.mIndicatorMargin;
        if (indicatorMargin <= 0) {
            indicatorMargin = mIndicatorMargin;
        }
        setIndicatorMargin(indicatorMargin);
        int indicatorHeight = bannerCell.mIndicatorHeight;
        setIndicatorHeight(indicatorHeight);
        if (bannerCell.itemMargin[0] > 0 || bannerCell.itemMargin[1] > 0) {
            setScrollMargin(bannerCell.itemMargin[0], bannerCell.itemMargin[1]);
            mUltraViewPager.setClipToPadding(false);
            mUltraViewPager.setClipChildren(false);
        } else {
            setScrollMargin(0, 0);
            mUltraViewPager.setClipToPadding(true);
            mUltraViewPager.setClipChildren(true);
        }
        VirtualLayoutManager.LayoutParams layoutParams = (VirtualLayoutManager.LayoutParams) getLayoutParams();
        layoutParams.setMargins(bannerCell.margin[3], bannerCell.margin[0], bannerCell.margin[1], bannerCell.margin[2]);
        mUltraViewPager.setItemRatio(bannerCell.itemRatio);
        if(updateInitFlag){
            updateInitFlag = false;
            currentItemPos = 0;
            if (cell.extras != null) {
                try {
                    cell.extras.put(CURRENT_POS, currentItemPos);
                } catch (JSONException e) {
                }
            }
        }else {
            currentItemPos = bannerCell.optIntParam(CURRENT_POS);
        }
        mUltraViewPager.setCurrentItem(currentItemPos);
        updateIndicators(bannerCell.mIndicatorFocus, bannerCell.mIndicatorNor,
                bannerCell.mIndicatorRadius, bannerCell.mIndicatorColor,
                bannerCell.mIndicatorDefaultColor);
        recycleView();
        bindHeaderView(bannerCell.mHeader);
        bindFooterView(bannerCell.mFooter);
        if (cell.serviceManager != null) {
            bannerSupport = cell.serviceManager.getService(BannerSupport.class);
        }
    }

    @Override
    public void postUnBindView(BaseCell cell) {
        recycleView();
        getContext().unregisterReceiver(mScreenBroadcastReceiver);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new LayoutParams(lp);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!Float.isNaN(ratio)) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (widthSize / ratio), MeasureSpec.EXACTLY);
        } else if (height > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        mUltraViewPager.measure(widthMeasureSpec, heightMeasureSpec);
        mIndicator.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int headerHeight = 0;
        if (!mHeaderViewHolders.isEmpty()) {
            for (int i = 0, count = mHeaderViewHolders.size(); i < count; i++) {
                View header = mHeaderViewHolders.get(i).itemView;
                LayoutParams lp = (LayoutParams) header.getLayoutParams();
                header.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                headerHeight += header.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            }
        }
        int footerHeight = 0;
        if (!mFooterViewHolders.isEmpty()) {
            for (int i = 0, count = mFooterViewHolders.size(); i < count; i++) {
                View footer = mFooterViewHolders.get(i).itemView;
                LayoutParams lp = (LayoutParams) footer.getLayoutParams();
                footer.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                footerHeight += footer.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            }
        }

        int measureWidth = mUltraViewPager.getMeasuredWidth();
        int measureHeight = mUltraViewPager.getMeasuredHeight();
        if (isIndicatorOutside) {
            int indicatorHeight = mIndicator.getMeasuredHeight();
            setMeasuredDimension(measureWidth, measureHeight + indicatorHeight + headerHeight + footerHeight);
        } else {
            setMeasuredDimension(measureWidth, measureHeight + headerHeight + footerHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int measureWidth = mUltraViewPager.getMeasuredWidth();
        int measureHeight = mUltraViewPager.getMeasuredHeight();
        int indicatorHeight = mIndicator.getMeasuredHeight();
        int top = getPaddingTop();
        int left = getPaddingLeft();
        if (!mHeaderViewHolders.isEmpty()) {
            for (int i = 0, count = mHeaderViewHolders.size(); i < count; i++) {
                View header = mHeaderViewHolders.get(i).itemView;
                LayoutParams lp = (LayoutParams) header.getLayoutParams();
                header.layout(left + lp.leftMargin, top + lp.topMargin, header.getMeasuredWidth(),
                        top + lp.topMargin + header.getMeasuredHeight());
                top += lp.topMargin + header.getMeasuredHeight() + lp.bottomMargin;
            }
        }
        mUltraViewPager.layout(left, top, measureWidth, top + measureHeight);
        top += measureHeight;
        if (isIndicatorOutside) {
            mIndicator.layout(left, top, measureWidth, top + measureHeight + indicatorHeight);
            top += indicatorHeight;
        } else {
            mIndicator.layout(left, top - indicatorHeight, measureWidth, top);
        }
        if (!mFooterViewHolders.isEmpty()) {
            for (int i = 0, count = mFooterViewHolders.size(); i < count; i++) {
                View footer = mFooterViewHolders.get(i).itemView;
                LayoutParams lp = (LayoutParams) footer.getLayoutParams();
                footer.layout(left + lp.leftMargin, top + lp.topMargin, footer.getMeasuredWidth(),
                        top + lp.topMargin + footer.getMeasuredHeight());
                top +=  + lp.topMargin + footer.getMeasuredHeight() + lp.bottomMargin;
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startTimer();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTimer();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            startTimer();
        } else {
            stopTimer();
        }
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        stopTimer();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        startTimer();
    }

    private int getIndicatorGravity(String gravity) {
        if ("left".equals(gravity)) {
            return GRAVITY_LEFT;
        }
        if ("right".equals(gravity)) {
            return GRAVITY_RIGHT;
        }
        return GRAVITY_CENTER;
    }

    public class BannerIndicator extends LinearLayout {

        private final int STYLE_NONE = 0;
        private final int STYLE_DOT = 1;
        private final int STYLE_IMG = 2;
        private final int STYLE_IMG_LOCAL = 3;

        private ImageView[] mImageViews;
        protected TextView mTextView;
        protected int pageCount;
        private String focusUrl;
        private String norUrl;
        private int norColor;
        private int focusColor;
        private float radius;
        private int style;

        public BannerIndicator(Context context) {
            super(context);
        }

        public void updateIndicators(String focusUrl, String norUrl, int radius, int focusColor, int norColor) {
            if (mUltraViewPager.getWrapperAdapter() == null) {
                return;
            }

            if(!hasIndicator){ //没有指示器
                setVisibility(INVISIBLE);
                return;
            }

            if(isIndicatorNum){  //用数字 indicator
                if(mTextView == null) {
                    mTextView = new TextView(getContext());
                    mTextView.setGravity(Gravity.CENTER);
                    addView(mTextView);
                }
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
                layoutParams.setMargins(0, mIndicatorMargin, mIndicatorGap, mIndicatorMargin);
                mTextView.setLayoutParams(layoutParams);
                mTextView.setTextColor(mIndicatorNumColor);
                mTextView.setTextSize(mIndicatorNumSize);
                mTextView.setBackgroundResource(mIndicatorBackgroundResId);
                pageCount = mUltraViewPager.getWrapperAdapter().getCount();
                mTextView.setText("1/"+pageCount);
                return;
            }

            this.focusUrl = focusUrl;
            this.norUrl = norUrl;

            this.norColor = norColor;
            this.focusColor = focusColor;
            this.radius = radius;

            if (norColor != 0 && focusColor != 0 && radius > 0) {
                style = STYLE_DOT;
            } else if(mIndicatorLocalResId > 0 && mIndicatorSelectedLocalResId > 0){
                style = STYLE_IMG_LOCAL;
            }else if (!TextUtils.isEmpty(focusUrl) && !TextUtils.isEmpty(norUrl)) {
                style = STYLE_IMG;
            } else {
                style = STYLE_NONE;
            }
            if (style == STYLE_NONE) {
                setVisibility(INVISIBLE);
                return;
            } else {
                setVisibility(VISIBLE);
            }
            int width = 0;
            int height = 0;

            if (style == STYLE_IMG) {
                Pair<Integer, Integer> norSize = Utils.getImageSize(norUrl);
                Pair<Integer, Integer> focSize = Utils.getImageSize(focusUrl);
                if (norSize != null && focSize != null) {
                    width = Math.max(norSize.first, focSize.first);
                    height = Math.max(norSize.second, focSize.second);
                } else {
                    if (focSize != null) {
                        width = focSize.first;
                        height = focSize.second;
                    }
                    if (norSize != null) {
                        width = norSize.first;
                        height = norSize.second;
                    }
                }
            } else if (style == STYLE_DOT) {
                width = 2 * radius;
                height = 2 * radius;
            }
            if (mIndicatorHeight != ViewGroup.LayoutParams.WRAP_CONTENT && mIndicatorHeight > 0) {
                height = mIndicatorHeight;
            }

            int count = mUltraViewPager.getWrapperAdapter().getCount();
            if(count < 2){ //只有一个不显示指示
                if(mImageViews != null && mImageViews.length > 0){
                    removeAllViews();
                    mImageViews = null;
                }
                return;
            }
            if (mImageViews == null) {
                mImageViews = new ImageView[count];
                for (int i = 0; i < mImageViews.length; i++) {
                    mImageViews[i] = ImageUtils.createImageInstance(getContext());
                    mImageViews[i].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    addView(mImageViews[i]);
                }
            } else if (mImageViews.length != count) {
                for (int i = 0; i < mImageViews.length; i++) {
                    removeView(mImageViews[i]);
                }
                ImageView[] old = mImageViews;
                mImageViews = new ImageView[count];
                System.arraycopy(old, 0,mImageViews, 0, Math.min(old.length, count));
                for (int i = 0; i < mImageViews.length; i++) {
                    if (mImageViews[i] == null) {
                        mImageViews[i] = ImageUtils.createImageInstance(getContext());
                        mImageViews[i].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                    addView(mImageViews[i]);
                }
            }
            int position = mUltraViewPager.getCurrentItem();
            for (int i = 0; i < mImageViews.length; i++) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mImageViews[i].getLayoutParams();
                if (style == STYLE_IMG || style == STYLE_DOT) {
                    layoutParams.setMargins(0, mIndicatorMargin, mIndicatorGap, mIndicatorMargin);
                    if (width > 0) {
                        layoutParams.width = width;
                    }
                    if (height > 0) {
                        layoutParams.height = height;
                    }
                } else {
                    layoutParams.setMargins(0, 0, 0, 0);
                }
                if (style == STYLE_DOT) {
                    mImageViews[i].setImageDrawable(getGradientDrawable(position == i ? focusColor : norColor, radius));
                } else if(style == STYLE_IMG_LOCAL){
                    layoutParams.setMargins(0, mIndicatorMargin, mIndicatorGap, mIndicatorMargin);
                    mImageViews[i].setImageResource(position == i ? mIndicatorSelectedLocalResId : mIndicatorLocalResId);
                } else if (style == STYLE_IMG){
                }
            }
            if (style == STYLE_IMG) {
                if (init) {
                    for (int i = 0; i < mImageViews.length; i++) {
                        ImageUtils.doLoadImageUrl(mImageViews[i], position == i ? focusUrl : norUrl);
                        if (i == currentItemPos) {
                            mImageViews[i].setTag(R.id.TANGRAM_BANNER_INDICATOR_POS, currentItemPos);
                        }
                    }
                } else {
                    for (int i = 0; i < mImageViews.length; i++) {
                        ImageView imageView = mImageViews[i];
                        if (imageView.getTag(R.id.TANGRAM_BANNER_INDICATOR_POS) == null) {
                            continue;
                        } else {
                            imageView.setTag(R.id.TANGRAM_BANNER_INDICATOR_POS, null);
                            ImageUtils.doLoadImageUrl(imageView, norUrl);
                        }
                    }
                    mImageViews[currentItemPos].setTag(R.id.TANGRAM_BANNER_INDICATOR_POS, currentItemPos);
                    ImageUtils.doLoadImageUrl(mImageViews[currentItemPos], focusUrl);
                }
            }
        }

        public void setCurrItem(int position) {
            if(mTextView != null){
                mTextView.setText((position+1)+"/"+pageCount);
            }else if (mImageViews != null) {
                for (int i = 0; i < mImageViews.length; i++) {
                    if (style == STYLE_DOT) {
                        mImageViews[i].setImageDrawable(getGradientDrawable(position == i ? focusColor : norColor, radius));
                    } else if (style == STYLE_IMG_LOCAL){
                        mImageViews[i].setImageResource(position == i ? mIndicatorSelectedLocalResId : mIndicatorLocalResId);
                    }else if (style == STYLE_IMG){
                        ImageView imageView = mImageViews[i];
                        if (imageView.getTag(R.id.TANGRAM_BANNER_INDICATOR_POS) == null) {
                            continue;
                        } else {
                            imageView.setTag(R.id.TANGRAM_BANNER_INDICATOR_POS, null);
                            ImageUtils.doLoadImageUrl(imageView, norUrl);
                        }
                    }
                }
                mImageViews[currentItemPos].setTag(R.id.TANGRAM_BANNER_INDICATOR_POS, currentItemPos);
                if (style == STYLE_IMG) {
                    ImageUtils.doLoadImageUrl(mImageViews[currentItemPos], focusUrl);
                }
            }
        }

        private GradientDrawable getGradientDrawable(int color, float radius) {
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{color, color});
            gradientDrawable.setShape(GradientDrawable.OVAL);
            gradientDrawable.setCornerRadius(radius);
            return gradientDrawable;
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (timer != null) {
            final int action = ev.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                stopTimer();
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                startTimer();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        float x = ev.getRawX();
        float y = ev.getRawY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                xDown = x;
                yDown = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int xDiff = (int) (x - xDown);
                int yDiff = (int) (y - yDown);

                direction = -xDiff;

                if (Math.abs(xDiff) >= Math.abs(yDiff)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                direction = 1;
                break;
            default:
                break;
        }

        return false;
    }

    @Override
    public int getNextItem() {
        return getNextItemIndex();
    }

    @Override
    public void callBack() {
        scrollNextPage();
    }

    private void bindHeaderView(BaseCell cell) {
        if (cell.isValid()) {
            View header = getHeaderViewFromRecycler(cell);
            if (header != null) {
                ViewGroup.LayoutParams lp = header.getLayoutParams();
                if (lp == null || !(lp instanceof LayoutParams)) {
                    lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                ((LayoutParams) lp).topMargin = cell.style.margin[Style.MARGIN_TOP_INDEX];

                ((LayoutParams) lp).leftMargin = cell.style.margin[Style.MARGIN_LEFT_INDEX];
                ((LayoutParams) lp).bottomMargin = cell.style.margin[Style.MARGIN_BOTTOM_INDEX];
                ((LayoutParams) lp).rightMargin = cell.style.margin[Style.MARGIN_RIGHT_INDEX];
                addView(header, lp);
            }
        }
    }

    private void bindFooterView(BaseCell cell) {
        if (cell.isValid()) {
            View footer = getFooterViewFromRecycler(cell);
            if (footer != null) {
                ViewGroup.LayoutParams lp = footer.getLayoutParams();
                if (lp == null || !(lp instanceof LayoutParams)) {
                    lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                ((LayoutParams) lp).topMargin = cell.style.margin[Style.MARGIN_TOP_INDEX];
                ((LayoutParams) lp).leftMargin = cell.style.margin[Style.MARGIN_LEFT_INDEX];
                ((LayoutParams) lp).bottomMargin = cell.style.margin[Style.MARGIN_BOTTOM_INDEX];
                ((LayoutParams) lp).rightMargin = cell.style.margin[Style.MARGIN_RIGHT_INDEX];
                addView(footer, lp);
            }
        }
    }

    private View getHeaderViewFromRecycler(@NonNull BaseCell cell) {
        GroupBasicAdapter adapter = cell.serviceManager.getService(GroupBasicAdapter.class);
        RecyclerView.RecycledViewPool pool = cell.serviceManager.getService(RecyclerView.RecycledViewPool.class);
        int itemViewType = adapter.getItemType(cell);
        BinderViewHolder holder = (BinderViewHolder) pool.getRecycledView(itemViewType);
        if (holder == null) {
            holder = (BinderViewHolder) adapter.createViewHolder(this, itemViewType);
        }
        holder.bind(cell);
        mHeaderViewHolders.add(holder);
        return holder.itemView;
    }

    private View getFooterViewFromRecycler(@NonNull BaseCell cell) {
        GroupBasicAdapter adapter = cell.serviceManager.getService(GroupBasicAdapter.class);
        RecyclerView.RecycledViewPool pool = cell.serviceManager.getService(RecyclerView.RecycledViewPool.class);
        int itemViewType = adapter.getItemType(cell);
        BinderViewHolder holder = (BinderViewHolder) pool.getRecycledView(itemViewType);
        if (holder == null) {
            holder = (BinderViewHolder) adapter.createViewHolder(this, itemViewType);
        }
        holder.bind(cell);
        mFooterViewHolders.add(holder);
        return holder.itemView;
    }

    private void recycleView() {
        recyclerView(mHeaderViewHolders);
        recyclerView(mFooterViewHolders);
    }

    private void recyclerView(List<BinderViewHolder> cache) {
        if (!cache.isEmpty()) {
            RecyclerView.RecycledViewPool pool = cell.serviceManager.getService(RecyclerView.RecycledViewPool.class);
            for (int i = 0, size = cache.size(); i < size; i++) {
                BinderViewHolder viewHolder = cache.get(i);
                viewHolder.unbind();
                removeView(viewHolder.itemView);
                pool.putRecycledView(viewHolder);
            }
            cache.clear();
        }
    }

    public void setScrollMargin(int left, int right) {
        mUltraViewPager.setPadding(left, 0, right, 0);
    }

    public void setInfiniteLoop(boolean enableLoop) {
        mUltraViewPager.setEnableLoop(enableLoop);
    }

    public void setAutoScroll(int intervalInMillis, SparseIntArray intervalArray) {
        if (0 == intervalInMillis) {
            return;
        }
        if (timer != null) {
            disableAutoScroll();
        }
        timer = new TimerHandler(this, intervalInMillis);
        timer.setSpecialInterval(intervalArray);
        startTimer();
    }

    public void disableAutoScroll() {
        stopTimer();
        timer = null;
    }

    private void startTimer() {
        if (timer == null || mUltraViewPager == null || !timer.isStopped()) {
            return;
        }
        timer.setListener(this);
        timer.removeCallbacksAndMessages(null);
        timer.tick(0);
        timer.setStopped(false);
    }

    private void stopTimer() {
        if (timer == null || mUltraViewPager == null || timer.isStopped()) {
            return;
        }
        timer.removeCallbacksAndMessages(null);
        timer.setListener(null);
        timer.setStopped(true);
    }

    private int getNextItemIndex() {
        int nextIndex = mUltraViewPager.getNextItem();
        return nextIndex;
    }

    private boolean scrollNextPage() {
        direction = 1;
        boolean isChange = false;
        if (mUltraViewPager != null && mUltraViewPager.getAdapter() != null && mUltraViewPager.getAdapter().getCount() > 0) {
            final int curr = mUltraViewPager.getCurrentItemFake();
            int nextPage = 0;
            if (curr < mUltraViewPager.getAdapter().getCount() - 1) {
                nextPage = curr + 1;
                isChange = true;
            }
            mUltraViewPager.setCurrentItemFake(nextPage, true);
        }
        return isChange;
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
            super(source);
        }

    }

    private static class ScreenBroadcastReceiver extends BroadcastReceiver {

        private String action = null;
        private BannerView mBannerView = null;

        public ScreenBroadcastReceiver(BannerView bannerView) {
            mBannerView = bannerView;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mBannerView.startTimer();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mBannerView.stopTimer();
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mBannerView.startTimer();
            }
        }
    }


}

