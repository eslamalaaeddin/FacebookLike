package com.example.facebook_clone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.model.post.react.React
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_who_reacted_item.view.*

class ReactsAdapter(private var reacts: List<React>) :
    RecyclerView.Adapter<ReactsAdapter.ReactsHolder>() {
    private val picasso = Picasso.get()
    inner class ReactsHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {


        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }


        fun bind(react: React) {
            picasso.load(react.reactorImageUrl).into(itemView.userWhoReactImageView)
            itemView.whoReactNameTextView.text = react.reactorName
            //React type
            when(react.react){
                1 -> {

                    itemView.userReactPlaceHolder.setImageResource(R.drawable.ic_like_react)
                }
                2 -> {
                    itemView.userReactPlaceHolder.setImageResource(R.drawable.ic_love_react)
                }
                3 -> {
                    itemView.userReactPlaceHolder.setImageResource(R.drawable.ic_care_react)
                }
                4 -> {
                    itemView.userReactPlaceHolder.setImageResource(R.drawable.ic_haha_react)
                }
                5 -> {
                    itemView.userReactPlaceHolder.setImageResource(R.drawable.ic_wow_react)
                }
                6 -> {
                    itemView.userReactPlaceHolder.setImageResource(R.drawable.ic_sad_react)
                }
                7 -> {
                    itemView.userReactPlaceHolder.setImageResource(R.drawable.ic_angry_angry)
                }
            }
        }

        override fun onClick(item: View?) {

        }

        override fun onLongClick(item: View?): Boolean {
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactsHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_who_reacted_item, parent, false)

        return ReactsHolder(view)
    }

    override fun getItemCount(): Int {
        return reacts.size
    }

    override fun onBindViewHolder(holder: ReactsHolder, position: Int) {
        val react = reacts[holder.adapterPosition]
        holder.bind(react)
    }
}