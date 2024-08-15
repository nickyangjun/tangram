package com.kks.sample.tangram.card;

import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSON;
import com.tmall.wireless.tangram.TangramEngine;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.structure.BaseCell;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件名：TDataHelper
 * 描述：
 * 版本：1.0
 * 作者：nickyang
 * 创建日期：2019/5/28
 * 版权：小水滴
 */
public class TDataHelper {
    private final static int DATA_KEY = 11111;
    private final static String MODEL_KEY = "ViewModel";
    private final static String EMPTY_CARD_KEY = "EMPTY_CARD_KEY";

    /**
     * tangram 是一个数据bean 对应一个view, 所以都必须有一个唯一的 type
     *
     * @param type
     * @param data
     * @param <T>
     * @return
     */
    public static <T> List<TData<?>> convert(String type, List<T> data) {
        if (data == null) return null;
        List<TData<?>> result = new ArrayList<>(data.size());
        for (T t : data) {
            result.add(new TData<>(type, t));
        }
        return result;
    }

    /**
     * 数据和 baseCell 绑定
     *
     * @param baseCells
     * @param datas
     * @param <T>
     */
    public static <T> void bindData(List<BaseCell> baseCells, List<T> datas) {
        if (baseCells.size() != datas.size()) return;
        for (int i = 0; i < baseCells.size(); i++) {
            BaseCell cell = baseCells.get(i);
            cell.setTag(TDataHelper.DATA_KEY, datas.get(i));  //缓存数据，避免反序列化
        }
    }

    /**
     * 数据和 baseCell 绑定
     *
     * @param cell
     * @param data
     * @param <T>
     */
    public static <T> void bindData(BaseCell cell, T data) {
        cell.setTag(TDataHelper.DATA_KEY, data);  //缓存数据，避免反序列化
    }

    /**
     * 数据和 baseCell 绑定
     *
     * @param baseCells
     * @param datas
     */
    public static void bindTData(List<BaseCell> baseCells, List<TData<?>> datas) {
        if (baseCells.size() != datas.size()) return;
        for (int i = 0; i < baseCells.size(); i++) {
            BaseCell cell = baseCells.get(i);
            cell.setTag(TDataHelper.DATA_KEY, datas.get(i).data);  //缓存数据，避免反序列化
        }
    }

    /**
     * 从 BaseCell 里面取得数据
     *
     * @param cell
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T getData(BaseCell cell, Class<T> cls) {
        if (cell.getTag(DATA_KEY) != null) {
            return (T) cell.getTag(DATA_KEY);
        } else {
            try {
                if (cell.extras.has("data")) {
                    T t = JSON.parseObject(cell.extras.getString("data"), cls);
                    cell.setTag(DATA_KEY, t);
                    return t;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 从 BaseCell 里面取得List数据
     *
     * @param cell
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> getArrayData(BaseCell cell, Class<T> cls) {
        if (cell.getTag(DATA_KEY) != null) {
            return (List<T>) cell.getTag(DATA_KEY);
        } else {
            try {
                if (cell.extras.has("data")) {
                    List<T> t = JSON.parseArray(cell.extras.getString("data"), cls);
                    cell.setTag(DATA_KEY, t);
                    return t;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // 绑定 ViewModel
    public static void bindViewModel(TangramEngine engine, Map<String, ViewModel> modelMap) {
        if (modelMap.size() > 0) {
            for (Map.Entry<String, ViewModel> entry : modelMap.entrySet()) {
                Card card = engine.getCardById(entry.getKey());
                bindViewModel(card, entry.getValue());
            }
        }
    }

    // 绑定 ViewModel
    public static void bindViewModel(Card card, ViewModel model) {
        if (card == null || model == null) return;
        Map<String, Object> map = new HashMap<>();
        map.put(MODEL_KEY, model);
        card.setParams(map);
    }

    public static <T extends ViewModel> T getViewModel(BaseCell cell) {
        if (cell.parent != null && cell.parent.getParams() != null) {
            Object obj = cell.parent.getParams().get(MODEL_KEY);
            return obj != null ? (T) obj : null;
        }
        return null;
    }

    public static void bindLoadingAndEmptyCard(TangramEngine engine, LoadingAndEmptyCard loadingAndEmptyCard) {
        if (loadingAndEmptyCard == null) return;
        Card card = engine.findCardById(loadingAndEmptyCard.getId());
        bindLoadingAndEmptyCard(card, loadingAndEmptyCard);
    }

    public static void bindLoadingAndEmptyCard(Card card, LoadingAndEmptyCard loadingAndEmptyCard) {
        if (loadingAndEmptyCard == null || card == null) return;
        Map<String, Object> map = new HashMap<>();
        map.put(EMPTY_CARD_KEY, loadingAndEmptyCard);
        card.setParams(map);
    }

    public static LoadingAndEmptyCard getLoadingAndEmptyCard(BaseCell cell) {
        Card card = cell.parent;
        if (card != null && card.getParams() != null) {
            Object obj = card.getParams().get(EMPTY_CARD_KEY);
            return obj != null ? (LoadingAndEmptyCard) obj : null;
        }
        return null;
    }
}
