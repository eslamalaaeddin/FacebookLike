package com.example.facebook_clone.adapter

import android.text.format.DateFormat.format
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.CommentClickListener
import com.example.facebook_clone.model.post.Comment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.comment_item_layout.view.*

class CommentsAdapter(private var list: List<Comment>, private val commentClickListener: CommentClickListener) :
    RecyclerView.Adapter<CommentsAdapter.CommentHolder>() {
    private val picasso = Picasso.get()
    private lateinit var cClickListener: CommentClickListener

    inner class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {

        init {
            cClickListener = commentClickListener
            itemView.setOnLongClickListener(this)
        }

        fun bind(comment: Comment) {
            picasso.load(comment.commenterImageUrl).into(itemView.commenterImageView)
            itemView.commenterNameTextView.text = comment.commenterName
            itemView.commentTextView.text = comment.comment
            itemView.commentCreationTimeTextView.text =
                format("EEE, MMM d, h:mm a", comment.commentTime.toDate())
    }

        override fun onLongClick(p0: View?): Boolean {
            cClickListener.onCommentLongClicked(list[adapterPosition])
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.comment_item_layout, parent, false)

        return CommentHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        val comment = list[holder.adapterPosition]
        holder.bind(comment)
    }
}