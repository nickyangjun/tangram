package com.tmall.wireless.tangram.op;

import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.structure.BaseCell;

import java.util.List;

/**
 * Created by longerian on 2018/3/26.
 *
 * @author longerian
 * @date 2018/03/26
 */

public class LoadGroupOp extends TangramOp2<Card, List<BaseCell>> {
    public LoadGroupOp(Card arg1, List<BaseCell> arg2) {
        super(arg1, arg2);
    }
}
