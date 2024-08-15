package com.kks.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.kks.sample.tangram.Page
import com.kks.sample.tangram.card.LinearLayoutCard

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        Page.newPage(recyclerView)
            .registerCell(ItemView.TYPE, ItemView::class.java)
            .addCardPageLoadSupport(LinearLayoutCard<Any>(), Page.PageLoader { card, page, callback ->
                callback.finish(ItemView.TYPE, listOf(1,2,3),false)
            })
            .build()
    }
}