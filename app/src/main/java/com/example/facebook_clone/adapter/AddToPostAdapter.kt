package com.example.facebook_clone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.facebook_clone.R
import com.example.facebook_clone.model.Option
import kotlinx.android.synthetic.main.add_to_post_item_layout.view.*

class AddToPostAdapter(context: Context, resource: Int, objects: MutableList<Option>) :

    ArrayAdapter<Option>(
        context,
        resource,
        objects
    )

{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view =
            LayoutInflater.from(context).inflate(R.layout.add_to_post_item_layout , parent, false)

        val additionItem = getItem(position)

        additionItem?.let { additionItem ->
            view.addToPostIcon.setImageResource(additionItem.imageRes)
            view.additionDescriptionTextView.text = additionItem.imageDescription
        }

        return view
    }




}