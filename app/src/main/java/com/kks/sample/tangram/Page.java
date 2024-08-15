package com.kks.sample.tangram;

import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.kks.sample.CommonUtil;
import com.kks.sample.LogUtil;
import com.kks.sample.tangram.bean.LoaderOp;
import com.kks.sample.tangram.card.BannerCard;
import com.kks.sample.tangram.card.BaseCard;
import com.kks.sample.tangram.card.ICard;
import com.kks.sample.tangram.card.LoadingAndEmptyCard;
import com.kks.sample.tangram.card.SampleCard;
import com.kks.sample.tangram.card.TData;
import com.kks.sample.tangram.card.TDataHelper;
import com.kks.sample.tangram.support.CardBackgroundSupport;
import com.kks.sample.tangram.support.ImageSupport;
import com.kks.sample.tangram.widgets.BannerView;
import com.kks.sample.tangram.widgets.DividerView;
import com.kks.sample.tangram.widgets.LoadingAndEmptyItemView;
import com.kks.sample.tangram.widgets.SampleItemView;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.TangramEngine;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.support.BannerSupport;
import com.tmall.wireless.tangram.support.CardSupport;
import com.tmall.wireless.tangram.support.RxBannerScrolledListener;
import com.tmall.wireless.tangram.support.SimpleClickSupport;
import com.tmall.wireless.tangram.support.async.AsyncPageLoader;
import com.tmall.wireless.tangram.support.async.CardLoadSupport;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;

/**
 * 文件名：Page
 * <p>
 * 描述：
 * <p>
 * JSON.toJSONString(data, SerializerFeature.DisableCircularReferenceDetect))
 * 加上 SerializerFeature.DisableCircularReferenceDetect 避免数组序列号时有相同的对象，第二次被序列化成$ref，造成item显示不出来
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/31
 * 版权：小水滴
 */
public class Page implements LifecycleObserver {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private TangramEngine engine;
    private TangramBuilder.InnerBuilder builder;
    private SimpleClickSupport simpleClickSupport;
    private PageLoader pageLoader;          //分页加载器
    private CardLoadSupport cardLoadSupport;
    private List<ICard> cardList = new ArrayList<>();  // 所有的Card集合，用于最后转成json
    private String pageLoadId;  //分页加载的项，用于下拉刷新, 一个页面只能有一个
    private boolean isRefreshEvent; //下拉刷新
    private Map<String, ViewModel> modelMap = new HashMap<>(); //viewModel
    private Map<String, AsyncLoader> asyncLoaderMap = new HashMap<>();
    private LoadingAndEmptyCard loadingAndEmptyCard = new LoadingAndEmptyCard();
    private boolean hasLoadingCard;  //标志是否已经添加过加载更多 view 了
    private BannerSupport bannerSupport;  //banner 滑动监听事件
    private CardBackgroundSupport backgroundSupport; // 可以定制一个card布局的背景， 但是 card style 里的 bgImgUrl 一定不能为空，为空则不会回调

    private boolean orderAsyncReq;  //是否异步请求按顺序执行
    private FlowableEmitter<LoaderOp> loaderEmitter;
    private Subscription subscription;
    public int firstPosition = 0;

    public static Page newPage(RecyclerView recyclerView, @Nullable SwipeRefreshLayout refreshLayout) {
        return new Page(recyclerView, refreshLayout);
    }

    public static Page newPage(RecyclerView recyclerView) {
        return new Page(recyclerView);
    }

    private Page(RecyclerView recyclerView) {
        this(recyclerView, null);
    }

    private Page(RecyclerView recyclerView, @Nullable SwipeRefreshLayout refreshLayout) {
        this.recyclerView = recyclerView;
        this.refreshLayout = refreshLayout;
        if (recyclerView.getContext() instanceof FragmentActivity) {
            ((FragmentActivity) recyclerView.getContext()).getLifecycle().addObserver(this);
        } else if (recyclerView.getContext() instanceof ContextThemeWrapper) {
            ((FragmentActivity) ((ContextThemeWrapper) recyclerView.getContext()).getBaseContext()).getLifecycle().addObserver(this);
        }

        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    isRefreshEvent = true;
//                    if(asyncLoaderMap.size() == 1){
                    for (String key : asyncLoaderMap.keySet()) {
                        refreshAsyncData(key);
                    }
//                    }
                    if (cardLoadSupport != null) {
                        refreshPageData();
                    }
                }
            });
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (manager instanceof LinearLayoutManager) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) manager;
                    firstPosition = layoutManager.findFirstVisibleItemPosition();
                }
            }
        });

        //Step 1: init tangram
        TangramBuilder.init(recyclerView.getContext().getApplicationContext(), new ImageSupport(), ImageView.class);

        //Step 2: register build=in cells and cards
        builder = TangramBuilder.newInnerBuilder(recyclerView.getContext());

        builder.registerCell(SampleItemView.TYPE, SampleItemView.class);
        // 覆盖tangram 本身提供的 BannerView , 修改了 indicator
        builder.registerCell("-2", BannerView.class); // TangramBuilder.TYPE_CAROUSEL_CELL_COMPACT
        builder.registerCell(TangramBuilder.TYPE_CONTAINER_BANNER, BannerView.class);
        //注册默认分割
        builder.registerCell(DividerView.TYPE, DividerView.class);
    }

    private void initLoaderEmitter(final LoaderOp loaderOp) {
        Flowable.create(new FlowableOnSubscribe<LoaderOp>() {
            @Override
            public void subscribe(FlowableEmitter<LoaderOp> e) throws Exception {
                loaderEmitter = e;
                e.onNext(loaderOp);
            }
        }, BackpressureStrategy.BUFFER)
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap(new Function<LoaderOp, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(final LoaderOp loaderOp) throws Exception {
                        return Flowable.create(new FlowableOnSubscribe<Object>() {
                            @Override
                            public void subscribe(FlowableEmitter<Object> emitter) throws Exception {
                                if (loaderOp.loader instanceof AsyncLoader) {
                                    handleAsyncLoader(loaderOp.card, (AsyncLoader) loaderOp.loader, (com.tmall.wireless.tangram.support.async.AsyncLoader.LoadedCallback) loaderOp.tangramCallback, emitter);
                                } else {
                                    handleAsyncPageLoader(loaderOp.pageNo, loaderOp.card, (PageLoader) loaderOp.loader, (AsyncPageLoader.LoadedCallback) loaderOp.tangramCallback, emitter);
                                }
                            }
                        }, BackpressureStrategy.BUFFER);
                    }
                })
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onSubscribe(Subscription sub) {
                        subscription = sub;
                        sub.request(Integer.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Object o) {
                        subscription.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void sendLoaderOp(LoaderOp loaderOp) {
        if (loaderEmitter == null) {
            initLoaderEmitter(loaderOp);
            return;
        }
        loaderEmitter.onNext(loaderOp);
    }

    /**
     * 异步请求按页面布局顺序回调
     *
     * @return
     */
    public Page enableOrderAsyncReq() {
        orderAsyncReq = true;
        return this;
    }

    private void handleAsyncLoader(final Card card, AsyncLoader asyncLoader,
                                   final com.tmall.wireless.tangram.support.async.AsyncLoader.LoadedCallback callback,
                                   @Nullable final FlowableEmitter<Object> emitter) {
        asyncLoader.loadData(card, new AsyncLoader.LoadedCallback() {
            @Override
            public <T> void finish(String itemViewType, List<T> data) {
                if (data == null) {
                    data = new ArrayList<>();
                }

                if (isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                List<BaseCell> baseCells = null;
                try {
                    baseCells = createBaseCellList(card, itemViewType, data);
                    callback.finish(baseCells);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (emitter != null) {
                    emitter.onNext(card);
                    emitter.onComplete();
                }

                //没有分页加载时
                if (pageLoader == null && loadingAndEmptyCard != null) {
                    loadingAndEmptyCard.loadingState = LoadingAndEmptyCard.State.LOADING_END.getCode();
                    Card loadingCard = engine.getCardById(loadingAndEmptyCard.getId());
                    if (loadingCard != null) {
                        loadingCard.notifyDataChange();
                    }
                }

            }

            @Override
            public void finish(List<TData<?>> data) {
                if (isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                List<BaseCell> baseCells = null;
                try {
                    baseCells = createBaseCellList(card, data);
                    callback.finish(baseCells);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (emitter != null) {
                    emitter.onNext(card);
                    emitter.onComplete();
                }

                //没有分页加载时
                if (pageLoader == null && loadingAndEmptyCard != null) {
                    Page.this.finishNoMore();
                }
            }

            @Override
            public void finish() {
                if (isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                callback.finish();

                if (emitter != null) {
                    emitter.onNext(card);
                    emitter.onComplete();
                }

                //没有分页加载时
                if (pageLoader == null && loadingAndEmptyCard != null) {
                    Page.this.finishNoMore();
                }
            }

            @Override
            public void finishWithNetworkError() {
                finishWithNetworkError(0, "");
            }

            @Override
            public void finishWithNetworkError(int imageResId, String errorTxt) {
                if (isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                callback.fail(false);

                Page.this.finishWithNetworkError(imageResId, errorTxt);

                if (emitter != null) {
                    emitter.onNext(card);
                    emitter.onComplete();
                }
            }

            @Override
            public void finishWithSystemUpgrade() {
                if (isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                callback.fail(false);

                if (loadingAndEmptyCard != null) {
                    loadingAndEmptyCard.loadingState = LoadingAndEmptyCard.State.LOADING_SYSTEM_UPGRADE.getCode();
                    Card loadingCard = engine.getCardById(loadingAndEmptyCard.getId());
                    if (loadingCard != null) {
                        loadingCard.notifyDataChange();
                    }
                }

                if (emitter != null) {
                    emitter.onNext(card);
                    emitter.onComplete();
                }
            }
        });
    }

    private void handleAsyncPageLoader(final int page, final Card card, PageLoader pageLoader,
                                       final AsyncPageLoader.LoadedCallback callback, @Nullable final FlowableEmitter<Object> emitter) {
        pageLoader.loadData(card, page, new PageLoader.LoadedCallback() {
            @Override
            public <T> void finish(String cellType, List<T> data, boolean hasMore) {
                try {
                    if (page == 1 && isRefreshEvent && refreshLayout != null) {
                        refreshLayout.setRefreshing(false);
                    }

                    if (data == null) data = new ArrayList();

                    List<BaseCell> baseCells = createBaseCellList(card, cellType, data);
                    if (page == 1) {
                        card.page = 1;  //修正一下page， 商城首页缓存数据后，造成拉取两次首屏数据
                    }
                    callback.finish(baseCells, hasMore);

                    if (!hasMore && loadingAndEmptyCard != null) {
                        loadingAndEmptyCard.loadingState = LoadingAndEmptyCard.State.LOADING_END.getCode();
                        //整个page没有数据不显示加载完了
                        if (page == 1 && (data == null || data.size() == 0)) {
                            loadingAndEmptyCard.loadDoneVisible = false;
                        }
                        Card loadingCard = engine.getCardById(loadingAndEmptyCard.getId());
                        if (loadingCard != null) {
                            loadingCard.notifyDataChange();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (emitter != null) {
                    emitter.onNext(card);
                    emitter.onComplete();
                }
            }

            @Override
            public void finish(List<TData<?>> data, boolean hasMore) {
                try {
                    if (page == 1 && isRefreshEvent && refreshLayout != null) {
                        refreshLayout.setRefreshing(false);
                    }

                    if (data == null || data.size() == 0) {
                        callback.finish(hasMore);
                    } else {
                        List<BaseCell> baseCells = createBaseCellList(card, data);
                        if (page == 1) {
                            card.page = 1;  //修正一下page, 商城首页缓存数据后，造成拉取两次首屏数据
                        }
                        callback.finish(baseCells, hasMore);
                    }

                    if (!hasMore && loadingAndEmptyCard != null) {
                        loadingAndEmptyCard.loadingState = LoadingAndEmptyCard.State.LOADING_END.getCode();
                        //整个page没有数据不显示加载完了
                        if (page == 1 && (data == null || data.size() == 0)) {
                            loadingAndEmptyCard.loadDoneVisible = false;
                        }
                        Card loadingCard = engine.getCardById(loadingAndEmptyCard.getId());
                        if (loadingCard != null) {
                            loadingCard.notifyDataChange();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (emitter != null) {
                    emitter.onNext(card);
                    emitter.onComplete();
                }
            }

            @Override
            public void finishWithNetworkError() {
                finishWithNetworkError(0, "");
            }

            @Override
            public void finishWithNetworkError(int imageResId, String errorTxt) {
                if (page == 1 && isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }

                Page.this.finishWithNetworkError(imageResId, errorTxt);

                callback.fail(true);

                if (emitter != null) {
                    emitter.onNext(card);
                    emitter.onComplete();
                }
            }

            @Override
            public void finishWithSystemUpgrade() {
                if (page == 1 && isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }

                if (loadingAndEmptyCard != null) {
                    loadingAndEmptyCard.loadingState = LoadingAndEmptyCard.State.LOADING_SYSTEM_UPGRADE.getCode();
                    Card loadingCard = engine.getCardById(loadingAndEmptyCard.getId());
                    if (loadingCard != null) {
                        loadingCard.notifyDataChange();
                    }
                }
                callback.fail(true);

                if (emitter != null) {
                    emitter.onNext(card);
                    emitter.onComplete();
                }
            }
        });
    }

    //所有数据加载完成
    public void finishNoMore() {
        if (loadingAndEmptyCard != null) {
            loadingAndEmptyCard.loadingState = LoadingAndEmptyCard.State.LOADING_END.getCode();
            Card loadingCard = engine.getCardById(loadingAndEmptyCard.getId());
            if (loadingCard != null) {
                loadingCard.notifyDataChange();
            }
        }
    }

    public void finishWithSystemUpgrade() {
        if (loadingAndEmptyCard != null) {
            loadingAndEmptyCard.loadingState = LoadingAndEmptyCard.State.LOADING_SYSTEM_UPGRADE.getCode();
            Card loadingCard = engine.getCardById(loadingAndEmptyCard.getId());
            if (loadingCard != null) {
                loadingCard.notifyDataChange();
            }
        }
    }

    public void finishWithNetworkError() {
        finishWithNetworkError(0, "");
    }

    public void finishWithNetworkError(int imageResId, @Nullable String errorTxt) {
        if (loadingAndEmptyCard != null) {
            loadingAndEmptyCard.loadingState = LoadingAndEmptyCard.State.LOADING_NETWORK_ERROR.getCode();
            if (imageResId > 0) {
                loadingAndEmptyCard.emptyNetErrImageRId = imageResId;
            }
            if (!TextUtils.isEmpty(errorTxt)) {
                loadingAndEmptyCard.emptyNetErrText = errorTxt;
            }
            Card loadingCard = engine.getCardById(loadingAndEmptyCard.getId());
            if (loadingCard != null) {
                loadingCard.notifyDataChange();
            }
        }
    }

    public <V extends View> Page registerCell(String type, @NonNull Class<V> viewClz) {
        builder.registerCell(type, viewClz);
        return this;
    }

    public Page addSimpleClickSupport(@NonNull final SimpleClickSupport support) {
        simpleClickSupport = support;
        return this;
    }

    /**
     * 可以定制一个card布局的背景
     *
     * @param cardSupport
     * @return
     */
    public Page addCardBackgroundSupport(CardBackgroundSupport cardSupport) {
        this.backgroundSupport = cardSupport;
        if (engine != null) {
            engine.register(CardSupport.class, this.backgroundSupport);
        }
        return this;
    }

    /**
     * 只能一个页面添加一个PageLoader
     *
     * @param pageLoader
     * @return
     */
    public Page addCardPageLoadSupport(@NonNull final PageLoader pageLoader) {
        this.pageLoader = pageLoader;
        return this;
    }

    /**
     * 只能一个页面添加一个PageLoader
     *
     * @param pageLoader
     * @return
     */
    public Page addCardPageLoadSupport(ICard card, @NonNull final PageLoader pageLoader) {
        this.pageLoader = pageLoader;
        if (card instanceof BaseCard) {
            ((BaseCard) card).setLoadType(BaseCard.LOAD_PAGE);
        }
        return addCard(card, null, null);
    }

    /**
     * 只能一个页面添加一个PageLoader
     *
     * @param pageLoader
     * @return
     */
    public Page addCardPageLoadSupport(ICard card, @Nullable ViewModel viewModel, @NonNull final PageLoader pageLoader) {
        this.pageLoader = pageLoader;
        if (card instanceof BaseCard) {
            ((BaseCard) card).setLoadType(BaseCard.LOAD_PAGE);
        }
        return addCard(card, viewModel, null);
    }

    public Page addCard(ICard card) {
        return addCard(card, null);
    }

    public Page addCard(ICard card, @Nullable ViewModel viewModel) {
        return addCard(card, viewModel, null);
    }

    public Page addCard(ICard card, @Nullable ViewModel viewModel, @Nullable AsyncLoader loader) {
        return addCard(cardList.size(), card, viewModel, loader);
    }

    public Page addCard(int index, ICard card) {
        return addCard(index, card, null, null);
    }

    public Page addCard(int index, ICard card, @Nullable ViewModel viewModel) {
        return addCard(index, card, viewModel, null);
    }

    public Page addCard(int index, ICard card, @Nullable ViewModel viewModel, @Nullable AsyncLoader loader) {
        if (index >= cardList.size()) {
            if (hasLoadingCard) {  // 最后一项已经有加载更多了
                index = cardList.size() - 1;
                cardList.add(index, card);
            } else {
                cardList.add(card);
            }
        } else {
            cardList.add(index, card);
        }
        if (card instanceof BaseCard) {
            BaseCard baseCard = (BaseCard) card;
            if (baseCard.getLoadType() == BaseCard.LOAD_PAGE) { //有分页加载的项
                pageLoadId = ((BaseCard) card).getId(); //有分页加载
            }
            if (viewModel != null) {
                modelMap.put(baseCard.getId(), viewModel);
            }
            if (loader != null && baseCard.getLoadType() == BaseCard.LOAD_ASYNC) { // 异步加载
                asyncLoaderMap.put(baseCard.getId(), loader);
            }
            if (card instanceof BannerCard) {  //如果有banner, 增加 banner 滑动监听
                bannerSupport = new BannerSupport();
            }
        }

        if (card instanceof SampleCard) {
            SampleCard sampleCard = (SampleCard) card;
            if (viewModel != null) {
                modelMap.put(sampleCard.getId(), viewModel);
            }
        }

        if (engine != null) { // 已经初始化了，即后面动态加入的布局
            try {
                JSONObject data = new JSONObject(JSON.toJSONString(card, SerializerFeature.DisableCircularReferenceDetect));
                Card tangramCard = engine.parseSingleData(data);
                TDataHelper.bindViewModel(tangramCard, viewModel);
                engine.insertBatchWith(index, tangramCard);  // 只加在最后
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public int getCardIndex(ICard card) {
        return cardList.indexOf(card);
    }

    /**
     * 只能添加在最后一项
     *
     * @return
     */
    public Page addLoadingAndEmptyCard(LoadingAndEmptyCard loadingAndEmptyCard) {
        this.loadingAndEmptyCard = loadingAndEmptyCard;
        hasLoadingCard = true;
        if (engine != null) { // 已经初始化了，即后面动态加入加载更多
            try {
                JSONObject data = new JSONObject(JSON.toJSONString(loadingAndEmptyCard, SerializerFeature.DisableCircularReferenceDetect));
                Card card = engine.parseSingleData(data);
                // 绑定 空View 点击事件
                TDataHelper.bindLoadingAndEmptyCard(card, loadingAndEmptyCard);
                engine.appendWith(card);  // 只加在最后
                cardList.add(loadingAndEmptyCard);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            registerCell(LoadingAndEmptyItemView.TYPE, LoadingAndEmptyItemView.class);
            cardList.add(loadingAndEmptyCard);
        }
        return this;
    }

    /**
     * 只能添加在最后一项
     *
     * @return
     */
    public Page addLoadingAndEmptyCard() {
        if (loadingAndEmptyCard == null) {
            loadingAndEmptyCard = new LoadingAndEmptyCard();
        }
        loadingAndEmptyCard.emptyViewNetErrClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(pageLoadId)) {
                    Card card = engine.findCardById(pageLoadId);
                    card.loaded = false; //重置标志位，才能重新加载数据
                }
                loadFirstPageCard();
            }
        };
        return addLoadingAndEmptyCard(loadingAndEmptyCard);
    }

    public LoadingAndEmptyCard getLoadingAndEmptyCard() {
        return loadingAndEmptyCard;
    }

    public Page build(boolean autoLoad) {
        try {
            engine = builder.build();

            if (!autoLoad) { // 不自动加载数据
                engine.enableAutoLoadMore(false);
            }

            if (simpleClickSupport != null) {
                engine.addSimpleClickSupport(simpleClickSupport);
            }

            if (bannerSupport != null) {
                engine.register(BannerSupport.class, bannerSupport);
            }

            cardLoadSupport = new CardLoadSupport(new com.tmall.wireless.tangram.support.async.AsyncLoader() {
                @Override
                public void loadData(final Card card, @NonNull final LoadedCallback callback) {
                    if (asyncLoaderMap.size() == 0) {
                        callback.finish();
                        return;
                    }
                    AsyncLoader asyncLoader = asyncLoaderMap.get(card.id);
                    if (asyncLoader == null) {
                        callback.finish();
                        return;
                    }

                    if (orderAsyncReq) {
                        LoaderOp loaderOp = new LoaderOp<>(card, asyncLoader, callback);
                        sendLoaderOp(loaderOp);
                        return;
                    }
                    handleAsyncLoader(card, asyncLoader, callback, null);
                }
            }, new AsyncPageLoader() {
                @Override
                public void loadData(final int page, @NonNull final Card card, final @NonNull LoadedCallback callback) {
                    if (pageLoader == null) {
                        callback.finish(false);
                        return;
                    }
                    //设置正在加载
                    if (loadingAndEmptyCard != null) {
                        loadingAndEmptyCard.loadingState = LoadingAndEmptyCard.State.LOADING.getCode();
                    }

                    if (orderAsyncReq) {
                        LoaderOp loaderOp = new LoaderOp<>(page, card, pageLoader, callback);
                        sendLoaderOp(loaderOp);
                        return;
                    }
                    handleAsyncPageLoader(page, card, pageLoader, callback, null);
                }
            });

            engine.addCardLoadSupport(cardLoadSupport);

            engine.bindView(recyclerView);

            if (this.backgroundSupport != null) {
                engine.register(CardSupport.class, this.backgroundSupport);
            }

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    engine.onScrolled();
                }
            });
            JSONArray data = new JSONArray(JSON.toJSONString(cardList, SerializerFeature.DisableCircularReferenceDetect));
            LogUtil.i(JSON.toJSONString(cardList, SerializerFeature.DisableCircularReferenceDetect));

            //防止马上拉取数据，page 对象还没有返回
            engine.setEnableLoadFirstPageCard(false);
            engine.setData(data);
            // 绑定 ViewModel
            TDataHelper.bindViewModel(engine, modelMap);
            // 绑定 空View 点击事件
            TDataHelper.bindLoadingAndEmptyCard(engine, loadingAndEmptyCard);
            if (autoLoad) { //不自动加载数据
                this.recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        loadFirstPageCard();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Page build() {
        return build(true);
    }

    /**
     * parseComponent 会把数据自动加入到 card 里面
     */
    private <T> List<BaseCell> createBaseCellList(Card card, String cellType, List<T> data) throws JSONException {
        if (data == null) return new ArrayList();
        JSONArray dataJson = new JSONArray(JSON.toJSONString(TDataHelper.convert(cellType, data), SerializerFeature.DisableCircularReferenceDetect));
        List<BaseCell> baseCells = engine.parseComponent(card, dataJson, false);
        TDataHelper.bindData(baseCells, data);
        return baseCells;
    }

    /**
     * parseComponent 会把数据自动加入到 card 里面
     */
    private List<BaseCell> createBaseCellList(Card card, List<TData<?>> data) throws JSONException {
        if (data == null) return new ArrayList();
        JSONArray dataJson = new JSONArray(JSON.toJSONString(data, SerializerFeature.DisableCircularReferenceDetect));
        List<BaseCell> baseCells = engine.parseComponent(card, dataJson, false);
        TDataHelper.bindTData(baseCells, data);
        return baseCells;
    }

    /**
     * 重新加载数据， 仅支持分页加载的数据重新第一页开始加载
     */
    public void refreshPageData() {
        if (cardLoadSupport != null && !TextUtils.isEmpty(pageLoadId)) {
            Card card = engine.getCardById(pageLoadId);
            if (card == null) return;
            card.page = 1;
            card.loaded = true;
            card.loading = false;
            card.hasMore = true;
            cardLoadSupport.loadMore(card);
        }
    }

    /**
     * 重新加载数据， 仅支持分页加载的数据重新第一页开始加载
     */
    public void refreshPageDataWithSwipeRefresh() {
        if (refreshLayout != null) {
            isRefreshEvent = true;
            refreshLayout.setRefreshing(true);
        }
        refreshPageData();
    }


    /**
     * 重新加载数据
     * <p>
     * todo  AsyncLoader card 还无效，因为 card.loaded = true  未置位，目前没有用的，后面解决
     *
     * @deprecated
     */
    public void refreshData() {
        if (cardLoadSupport != null && !TextUtils.isEmpty(pageLoadId)) {
            Card card = engine.getCardById(pageLoadId);
            if (card != null) {
                card.page = 1;
                card.loaded = false;
                card.loading = false;
                card.hasMore = true;
            }
        }
        loadFirstPageCard();
    }

    /**
     * 只能刷新 AsyncLoader 数据
     *
     * @param cardId
     */
    public void refreshAsyncData(String cardId) {
        final Card card = engine.getCardById(cardId);
        if (card == null) return;
        AsyncLoader asyncLoader = asyncLoaderMap.get(card.id);
        if (asyncLoader == null) return;

        card.loading = true;
        asyncLoader.loadData(card, new AsyncLoader.LoadedCallback() {
            @Override
            public <T> void finish(String itemViewType, List<T> data) {
                if (isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }

                if (data == null) {
                    data = new ArrayList<>();
                }

                List<BaseCell> baseCells = null;
                try {
                    baseCells = createBaseCellList(card, itemViewType, data);
                    card.removeAllCells();
                    card.addCells(baseCells);
                    card.notifyDataChange();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //没有分页加载时
                if (pageLoader == null && loadingAndEmptyCard != null) {
                    Page.this.finishNoMore();
                }
            }

            @Override
            public void finish(List<TData<?>> data) {
                if (isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                List<BaseCell> baseCells = null;
                try {
                    baseCells = createBaseCellList(card, data);
                    card.removeAllCells();
                    card.addCells(baseCells);
                    card.notifyDataChange();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //没有分页加载时
                if (pageLoader == null && loadingAndEmptyCard != null) {
                    Page.this.finishNoMore();
                }
            }

            @Override
            public void finish() {
                if (isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                card.loading = false;
                card.loaded = true;

                //没有分页加载时
                if (pageLoader == null && loadingAndEmptyCard != null) {
                    Page.this.finishNoMore();
                }
            }

            @Override
            public void finishWithNetworkError() {
                if (isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                card.loading = false;
                card.loaded = false;

                Page.this.finishWithNetworkError();
            }

            @Override
            public void finishWithNetworkError(int imageResId, String errorTxt) {
                if (isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                card.loading = false;
                card.loaded = false;

                Page.this.finishWithNetworkError(imageResId, errorTxt);
            }

            @Override
            public void finishWithSystemUpgrade() {
                if (isRefreshEvent && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                card.loading = false;
                card.loaded = false;
                Page.this.finishWithSystemUpgrade();
            }
        });
    }

    public void removeCard(String cardId) {
        if (TextUtils.isEmpty(cardId)) return;
        Iterator<ICard> iterator = cardList.iterator();
        while (iterator.hasNext()) {
            ICard card = iterator.next();
            if (card.getId().equals(cardId)) {
                iterator.remove();
                break;
            }
        }

        Card card = engine.getCardById(cardId);
        if (card != null) {
            engine.removeData(card);
        }
    }

    public void clearCardData(String cardId) {
        Card card = engine.getCardById(cardId);
        if (card != null) {
            card.removeAllCells();
        }
    }

    public @Nullable
    Card getCardById(String cardId) {
        if (engine == null) return null;
        return engine.getCardById(cardId);
    }

    /**
     * 更新某个组件(父容器)数据
     *
     * @param cardId 组件Id, 必须唯一
     * @param data   必须是 ICard 集合, 即容器嵌套， 内部实现是把 子容器的 item 抽出来直接加到父容器里面，
     *               有一个mChildren保存了各子容器的item， 而返回的 BaseCell 已经无效了
     *               <p>
     *               todo 嵌套时子item会自动加入，如果子容器和非容器item一起混合使用，会造成子容器mChildren里顺序不对，因为非容器item默认不加入
     */
    public <T extends ICard> void updateCard2Card(String cardId, List<T> data) {
        if (engine == null) return;
        Card card = engine.getCardById(cardId);
        if (card == null) return;
        try {
            List<BaseCell> baseCells = createCardBaseCellList(card, data);
//            card.setCells(baseCells);
            card.notifyDataChange();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * parseComponent 会把数据自动加入到 card 里面
     */
    private <T extends ICard> List<BaseCell> createCardBaseCellList(Card card, List<T> data) throws JSONException {
        JSONArray dataJson = new JSONArray(JSON.toJSONString(data, SerializerFeature.DisableCircularReferenceDetect));
        List<BaseCell> baseCells = engine.parseComponent(card, dataJson, false);
//        TDataHelper.bindTData(baseCells, data);
        return baseCells;
    }

    public <T> void addCell(String cardId, int index, String cellType, T data){
        if (engine == null) return;
        Card card = engine.getCardById(cardId);
        if (card == null) return;

        try {
            if (data == null) {
                return;
            }
            ArrayList<T> dataList = new ArrayList<>();
            dataList.add(data);

            List<BaseCell> baseCells = createBaseCellList(card, cellType, dataList);
            card.addCell(index, baseCells.get(0));
            card.notifyDataChange();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 更新某个组件(父容器)数据
     *
     * @param cardId   组件Id, 必须唯一
     * @param cellType 组件单元cell的类型，必须是先注册过的类型
     * @param data
     * @param <T>
     */
    public <T> void updateCardCell(String cardId, String cellType, List<T> data) {
        if (engine == null) return;
        Card card = engine.getCardById(cardId);
        if (card == null) return;
        try {
            if (data == null) {
                data = new ArrayList<>();
//                data.add((T) new Object());
            }
            List<BaseCell> baseCells = createBaseCellList(card, cellType, data);
            card.setCells(baseCells);
            card.notifyDataChange();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //更新组件头部
    public <T> void updateCardHeader(String cardId, String cellType, T data) {
        if (engine == null) return;
        Card card = engine.getCardById(cardId);
        if (card == null) return;
        BaseCell header = card.getHeader();
        if (header == null) {
            return;
        }
        try {
            header.typeKey = cellType;
            TDataHelper.bindData(header, data);
            card.notifyDataChange();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新某个组件(父容器)数据
     *
     * @param cardId   组件Id, 必须唯一
     * @param cellType 组件单元cell的类型，必须是先注册过的类型
     * @param data
     * @param <T>
     */
    public <T> void updateCardCell(String cardId, String cellType, T data) {
        if (engine == null) return;
        List<T> datas = new ArrayList<>(1);
        if (data == null) data = (T) new Object();
        datas.add(data);
        updateCardCell(cardId, cellType, datas);
    }

    /**
     * 更新某个组件(父容器)数据
     *
     * @param cardId 组件Id, 必须唯一
     * @param data
     */
    public void updateCardCell(String cardId, List<TData<?>> data) {
        if (engine == null) return;
        Card card = engine.getCardById(cardId);
        if (card == null) return;
        try {
            List<BaseCell> baseCells = createBaseCellList(card, data);
            card.setCells(baseCells);
            card.notifyDataChange();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加一个分割
     *
     * @param index
     * @param dividerId 即cardId,  可以根据这个删除此分割线
     * @param colorId
     * @param height
     */
    public void addDivider(int index, String dividerId, int colorId, int height) {
        ICard iCard = new SampleCard(dividerId, DividerView.TYPE);
        if (!hasCard(iCard.getId())) {
            addCard(index, iCard);
        }
        updateCardCell(iCard.getId(), DividerView.TYPE, DividerView.Divider.getDivider(colorId, height));
    }


    /**
     * 添加某个组件, 会默认一个 SampleCard
     *
     * @param cellType 组件单元cell的类型，必须是先注册过的类型
     * @param data     可以为空, 只能是单个数据
     * @param <T>
     */
    public <T> Page addSingleCell(String cellType, @Nullable T data) {
        addSingleCell(cardList.size(), cellType, data);
        return this;
    }

    /**
     * 添加某个组件, 会默认一个 SampleCard
     *
     * @param cellType  组件单元cell的类型，必须是先注册过的类型
     * @param viewModel 可以为空, 只能是单个数据
     */
    public Page addSingleCell(String cellType, ViewModel viewModel) {
        addSingleCell(cardList.size(), cellType, null, viewModel);
        return this;
    }

    /**
     * 添加某个组件, 会默认一个 SampleCard
     *
     * @param cellType  组件单元cell的类型，必须是先注册过的类型
     * @param viewModel 可以为空, 只能是单个数据
     */
    public <T> Page addSingleCell(String cellType, ViewModel viewModel, @Nullable T data) {
        addSingleCell(cardList.size(), cellType, data, viewModel);
        return this;
    }


    /**
     * 添加某个组件, 会默认一个SampleCard
     *
     * @param index    card 位置
     * @param cellType 组件单元cell的类型，必须是先注册过的类型
     * @param data     可以为空, 只能是单个数据
     * @param <T>
     */
    public <T> Page addSingleCell(int index, String cellType, @Nullable T data) {
        addSingleCell(index, cellType, data, null);
        return this;
    }

    public <T> void updateSingleCell(String cellType, @Nullable T data) {
        if (engine == null) return;
        Card card = engine.getCardById(cellType); //SingleCell 的 cellType == cardId
        if (card == null) return;
        updateCardCell(cellType, cellType, data);
    }

    /**
     * 添加某个组件, 会默认一个SampleCard
     *
     * @param index    card 位置
     * @param cellType 组件单元cell的类型，必须是先注册过的类型
     * @param data     可以为空, 只能是单个数据
     * @param <T>
     */
    public <T> Page addSingleCell(int index, String cellType, @Nullable T data, @Nullable ViewModel viewModel) {
        ICard iCard = new SampleCard(cellType);
        addCard(index, iCard, viewModel);
        if (data != null) {
            updateCardCell(iCard.getId(), cellType, data);
        }
        return this;
    }

    public <T> Page addSingleCell(SampleCard iCard, @Nullable T data, @Nullable ViewModel viewModel) {
        addCard(cardList.size(), iCard, viewModel);
        if (data != null) {
            updateCardCell(iCard.getId(), iCard.type, data);
        }
        return this;
    }

    /**
     * 添加某个组件的(父容器)数据
     *
     * @param cardId   组件Id, 必须唯一
     * @param cellType 组件单元cell的类型，必须是先注册过的类型
     * @param data     可以为空, 只能是单个数据
     * @param <T>
     */
    public <T> void addCardCell(String cardId, String cellType, @Nullable T data) {
        if (engine == null) return;
        Card card = engine.getCardById(cardId);
        if (card == null) return;
        try {
            TData<T> tData = new TData<>(cellType, data);
            JSONObject jsonObject = new JSONObject(JSON.toJSONString(tData, SerializerFeature.DisableCircularReferenceDetect));
            BaseCell cell = engine.parseSingleComponent(card, jsonObject);
            TDataHelper.bindData(cell, data);
            //card.notifyDataChange();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加某个组件的(父容器)数据
     *
     * @param cardId   组件Id, 必须唯一
     * @param cellType 组件单元cell的类型，必须是先注册过的类型
     * @param data     可以为空
     * @param <T>
     */
    public <T> void addCardCells(String cardId, String cellType, @Nullable List<T> data) {
        if (engine == null) return;
        Card card = engine.getCardById(cardId);
        if (card == null) return;
        try {
            List<TData<?>> tData = TDataHelper.convert(cellType, data);
            JSONArray jsonArray = new JSONArray(JSON.toJSONString(tData, SerializerFeature.DisableCircularReferenceDetect));
            List<BaseCell> cells = engine.parseComponent(card, jsonArray);
            TDataHelper.bindData(cells, data);
            card.notifyDataChange();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public <T> void addCardCells(String cardId, @Nullable List<TData<T>> data) {
        if (engine == null) return;
        Card card = engine.getCardById(cardId);
        if (card == null) return;
        try {
            JSONArray jsonArray = new JSONArray(JSON.toJSONString(data, SerializerFeature.DisableCircularReferenceDetect));
            List<BaseCell> cells = engine.parseComponent(card, jsonArray);
            TDataHelper.bindData(cells, data);
            card.notifyDataChange();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public <T> void addCardCellsFirst(String cardId, @Nullable List<TData<T>> data) {
        if (engine == null) return;
        Card card = engine.getCardById(cardId);
        if (card == null) return;
        try {
            JSONArray jsonArray = new JSONArray(JSON.toJSONString(data, SerializerFeature.DisableCircularReferenceDetect));
            List<BaseCell> cells = engine.parseComponent(card, jsonArray, false);
            TDataHelper.bindData(cells, data);
            card.addCells(card, 0, cells);
            card.notifyDataChange();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<BaseCell> getCardCells(String cardId) {
        if (engine == null) return null;
        Card card = engine.getCardById(cardId);
        if (card == null) return null;
        return card.getCells();
    }

    /**
     * 更新相关 card 数据
     *
     * @param cardId
     */
    public void notifyDataChange(String cardId) {
        if (engine == null) return;
        Card card = engine.getCardById(cardId);
        if (card == null) return;
        card.notifyDataChange();
    }

    public void notifyDataChange() {
        if (engine == null) return;
        engine.refresh();
    }

    public void loadFirstPageCard() {
        engine.setEnableLoadFirstPageCard(true);
        engine.loadFirstPageCard();
    }

    public boolean hasCard(String cardId) {
        if (engine == null) return false;
        return engine.getCardById(cardId) != null;
    }

    public int getCardCount() {
        return cardList.size();
    }

    // TODO: 2019/7/31 清除所有数据界面会闪一下
    public void cleanAllCard() {
        if (engine == null) return;
        engine.getGroupBasicAdapter().setData(null);
    }

    public void scrollToPosition(String cardId) {
        Card card = engine.getCardById(cardId);
        if (card == null) return;
        scrollToPosition(card);
    }

    public void scrollToPosition(Card card) {
        List<BaseCell> cells = card.getCells();
        if (cells.size() > 0) {
            BaseCell cell = cells.get(0);
            int pos = engine.findFirstPositionOfCell(cell.stringType);
            if (pos == 0) {
                RecyclerView recyclerView = engine.getContentView();
                if (recyclerView != null) {
                    recyclerView.scrollToPosition(pos);
                }
            } else {
                engine.scrollToPosition(card);
            }
        }
    }

    public void topPosition(String cardId) {
        Card card = engine.getCardById(cardId);
        if (card == null) return;
        topPosition(card);
    }

    public void topPosition(Card card) {
        List<BaseCell> cells = card.getCells();
        if (cells.size() > 0) {
            BaseCell cell = cells.get(0);
            int pos = engine.findFirstPositionOfCell(cell.stringType);
            if (pos == 0) {
                RecyclerView recyclerView = engine.getContentView();
                if (recyclerView != null) {
                    recyclerView.scrollToPosition(pos);
                }
            } else {
                engine.topPosition(card);
            }
        }
    }

    /**
     * 获取 banner 滑动事件
     *
     * @param bannerCardId
     * @return
     */
    public Observable<RxBannerScrolledListener.ScrollEvent> observeScrolled(String bannerCardId) {
        return bannerSupport.observeScrolled(bannerCardId);
    }

    /**
     * 获取 banner 滑动事件，选中第几页
     *
     * @param bannerCardId
     * @return Observable<position>
     */
    public Observable<Integer> observeSelected(String bannerCardId) {
        return bannerSupport.observeSelected(bannerCardId);
    }

    /**
     * 获取 banner 滑动状态变化
     *
     * @param bannerCardId
     * @return Observable<state>
     */
    public Observable<Integer> observeScrollStateChanged(String bannerCardId) {
        return bannerSupport.observeScrollStateChanged(bannerCardId);
    }

    /**
     * onDestroy 时清除数据
     *
     * @param owner
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy(@NonNull LifecycleOwner owner) {
        if (engine != null) {
            engine.destroy();
        }
        if (subscription != null) {
            subscription.cancel();
        }

        FragmentActivity activity = CommonUtil.getBaseActivity(recyclerView.getContext());
        if (activity != null) {
            activity.getLifecycle().removeObserver(this);
        }
    }

    public interface Loader {

    }

    /**
     * 分页加载回调
     */
    public interface PageLoader extends Loader {
        void loadData(Card card, int page, LoadedCallback callback);

        interface LoadedCallback {
            <T> void finish(String itemViewType, List<T> data, boolean hasMore);

            void finish(@Nullable List<TData<?>> data, boolean hasMore);

            /**
             * 使用默认网络错误图标和文案
             */
            void finishWithNetworkError();

            /**
             * 错误的图标 和 文案
             *
             * @param imageResId
             * @param errorTxt
             */
            void finishWithNetworkError(int imageResId, String errorTxt);

            /**
             * 系统升级图标 和 文案
             */
            void finishWithSystemUpgrade();
        }
    }

    /**
     * 异步加载回调,
     */
    public interface AsyncLoader extends Loader {
        void loadData(Card card, LoadedCallback callback);

        interface LoadedCallback {
            <T> void finish(String itemViewType, List<T> data);

            void finish(List<TData<?>> data);

            void finish();

            /**
             * 使用默认网络错误图标和文案
             */
            void finishWithNetworkError();

            /**
             * 错误的图标 和 文案
             *
             * @param imageResId
             * @param errorTxt
             */
            void finishWithNetworkError(int imageResId, String errorTxt);

            /**
             * 系统升级图标 和 文案
             */
            void finishWithSystemUpgrade();
        }
    }

}
