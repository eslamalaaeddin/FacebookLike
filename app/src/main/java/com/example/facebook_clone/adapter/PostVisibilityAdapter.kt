package com.example.facebook_clone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.facebook_clone.R
import com.example.facebook_clone.model.Visibility


class PostVisibilityAdapter(val context: Context, var dataSource: List<Visibility>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val itemHolder: ItemHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.post_visibility_item_layout, parent, false)
            itemHolder = ItemHolder(view)
            view?.tag = itemHolder
        } else {
            view = convertView
            itemHolder = view.tag as ItemHolder
        }
        itemHolder.label.text = dataSource[position].visibilityDescription
        itemHolder.img.setImageResource(dataSource[position].visibilityIcon)

        return view
    }

    override fun getItem(position: Int): Any? {
        return dataSource[position];
    }

    override fun getCount(): Int {
        return dataSource.size;
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    private class ItemHolder(row: View?) {
        val label: TextView
        val img: ImageView

        init {
            label = row?.findViewById(R.id.visibilityDescriptionTextView) as TextView
            img = row?.findViewById(R.id.postVisibilityIcon) as ImageView
        }
    }

}