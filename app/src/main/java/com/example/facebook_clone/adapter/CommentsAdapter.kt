package com.example.facebook_clone.adapter

import android.text.format.DateFormat.format
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.CommentClickListener
import com.example.facebook_clone.helper.listener.ReactClickListener
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.react.React
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.comment_item_layout.view.*

private const val TAG = "CommentsAdapter"
class CommentsAdapter(private val auth: FirebaseAuth,
                      private val userName: String,
                      private val userImageUrl: String,
                      private var comments: List<Comment>,
                      private var reacts: List<React>,
                      private val commentClickListener: CommentClickListener,
                      private val reactClickListener: ReactClickListener
) :
    RecyclerView.Adapter<CommentsAdapter.CommentHolder>() {
    private val picasso = Picasso.get()
    private lateinit var cClickListener: CommentClickListener
    private lateinit var rClickListener: ReactClickListener


    inner class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {
        init {
            cClickListener = commentClickListener
            rClickListener = reactClickListener
            itemView.setOnLongClickListener(this)
            
            itemView.replyTextView.setOnClickListener {
                Toast.makeText(itemView.context, "Reply", Toast.LENGTH_SHORT).show()
            }
        }

        fun bind(comment: Comment) {
            picasso.load(comment.commenterImageUrl).into(itemView.commenterImageView)
            itemView.commenterNameTextView.text = comment.commenterName
            itemView.commentTextView.text = comment.comment
            itemView.commentCreationTimeTextView.text =
                format("EEE, MMM d, h:mm a", comment.commentTime.toDate())
    }

        override fun onLongClick(p0: View?): Boolean {
            cClickListener.onCommentLongClicked(comments[adapterPosition])
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.comment_item_layout, parent, false)

        return CommentHolder(view)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        val comment = comments[holder.adapterPosition]
//        val react = reacts[holder.adapterPosition]
        holder.bind(comment)
    }
}