package com.yy.comm.tangram.widgets

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
//import androidx.databinding.DataBindingUtil
//import androidx.viewbinding.ViewBinding
import com.tmall.wireless.tangram.structure.BaseCell
import com.tmall.wireless.tangram.structure.view.ITangramViewLifeCycle

open class TBaseItemViewKt(context: Context, layoutId: Int) : FrameLayout(context), ITangramViewLifeCycle {
//    var binding: ViewBinding? = null

    init {
        initView(context, layoutId)
    }

    override fun cellInited(cell: BaseCell<*>?) {
    }

    override fun postBindView(cell: BaseCell<*>?) {
    }

    override fun postUnBindView(cell: BaseCell<*>?) {
    }

    fun initView(context: Context, layoutId: Int) {
//        binding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, this, true)
//        if (binding == null) {
//            LayoutInflater.from(context).inflate(layoutId, this, true)
//        }
    }
}