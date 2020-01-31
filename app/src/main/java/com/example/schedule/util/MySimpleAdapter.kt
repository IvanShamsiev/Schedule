package com.example.schedule.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

class MySimpleAdapter<T>(@LayoutRes private val resource: Int, private var items: List<T>,
                         private val onSelect: ((itemView: View, item: T) -> Unit)? = null,
                         private val initViewHolder: ((itemView: View) -> Unit)? = null):
        RecyclerView.Adapter<MySimpleAdapter<T>.SimpleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(resource, parent, false)
        return SimpleViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) = holder.bind(items[position])


    inner class SimpleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(item: T) {
            onSelect?.invoke(itemView, item)
        }
        init {
            initViewHolder?.invoke(itemView)
        }
    }
}