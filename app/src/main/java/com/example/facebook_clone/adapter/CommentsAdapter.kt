package com.example.facebook_clone.adapter

import android.text.format.DateFormat.format
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.CommentClickListener
import com.example.facebook_clone.helper.listener.ReactClickListener
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.comment.ReactionsAndSubComments
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.comment_item_layout.view.*

private const val TAG = "CommentsAdapter"

class CommentsAdapter(
    private val commenterId: String,
    private var comments: List<Comment>,
    private var reacts: List<React>?,
    private val commentClickListener: CommentClickListener,
    private val reactClickListener: ReactClickListener,
    private val postViewModel: PostViewModel,
    private val postPublisherId:String
) :
    RecyclerView.Adapter<CommentsAdapter.CommentHolder>() {
    private val picasso = Picasso.get()
    private lateinit var cClickListener: CommentClickListener
    private lateinit var rClickListener: ReactClickListener
    private var reactsCount = 0


    inner class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnLongClickListener, View.OnClickListener {
        init {
            cClickListener = commentClickListener
            rClickListener = reactClickListener
            itemView.setOnLongClickListener(this)
            itemView.setOnClickListener(this)

            itemView.replyOnCommentTextView.setOnClickListener {
                Toast.makeText(itemView.context, "Reply", Toast.LENGTH_SHORT).show()
            }

            itemView.reactOnCommentTextView.setOnClickListener {
                val comment = comments[adapterPosition]
                var currentReact: React? = null
                var reacted: Boolean = false

                //I HAVE TO GET COMMENT DOCUMENT
                postViewModel.getCommentById(postPublisherId, comment.id.toString())
                    .addOnCompleteListener {
                        val commentDoc = it.result?.toObject(ReactionsAndSubComments::class.java)
                        commentDoc?.reactions?.let { reacts ->
                            if (reacts.isNotEmpty()) {
                                itemView.reactsCountTextView.text = reacts.size.toString()
                            }
                            for (react in reacts) {
                                if (react.reactorId == commenterId) {
                                    itemView.reactOnCommentTextView.text = "Liked"
                                    currentReact = react
                                    reacted = true
                                    break
                                }
                                else{
                                    itemView.reactOnCommentTextView.text = "Like"
                                }
                            }
                        }
                        cClickListener.onReactOnCommentClicked(
                            comment,
                            adapterPosition,
                            reacted,
                            currentReact
                        )
                    }
            }

//
////                comment.reacts?.let { reacts ->
////                    for (react in reacts) {
////                        if (react.reactorId == commenterId ) {
////                            currentReact = react
////                            reacted = true
////                            break
////                        }
////                    }
////                }
//
//            }

            itemView.mediaCommentCardView.setOnClickListener {
                val comment = comments[adapterPosition]
                cClickListener.onMediaCommentClicked(comment.attachmentCommentUrl.toString())
            }
        }

        fun bind(comment: Comment) {
            picasso.load(comment.commenterImageUrl).into(itemView.commenterImageView)
            itemView.commentCreationTimeTextView.text =
                format("EEE, MMM d, h:mm a", comment.commentTime.toDate())
            itemView.commenterNameTextView.text = comment.commenterName

            val interval: Long = 1 * 1000
            val options: RequestOptions = RequestOptions().frame(interval)
            //Media comment
            val commentType = comment.commentType
            if (comment.attachmentCommentUrl != null) {
                itemView.mediaCommentCardView.visibility = View.VISIBLE

                if (commentType == "textWithImage") {
                    itemView.commentTextView.text = comment.textComment
                    picasso.load(comment.attachmentCommentUrl).into(itemView.mediaCommentImageView)
                    itemView.playButtonImgView.visibility = View.GONE
                    itemView.commentTextView.visibility = View.VISIBLE
                }

                else if (commentType == "textWithVideo") {
                    itemView.commentTextView.text = comment.textComment
                    itemView.playButtonImgView.visibility = View.VISIBLE
                    itemView.commentTextView.visibility = View.VISIBLE

                    Glide.with(itemView.context)
                        .asBitmap().load(comment.attachmentCommentUrl).apply(options)
                        .into(itemView.mediaCommentImageView)
                }

                else if (commentType == "image") {
                    itemView.commentTextView.visibility = View.GONE
                    picasso.load(comment.attachmentCommentUrl).into(itemView.mediaCommentImageView)
                    itemView.playButtonImgView.visibility = View.GONE
                }

                else if (commentType == "video") {
                    itemView.commentTextView.visibility = View.GONE
                    itemView.playButtonImgView.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .asBitmap().load(comment.attachmentCommentUrl).apply(options)
                        .into(itemView.mediaCommentImageView)
                }
            }

            else {
                itemView.commentTextView.visibility = View.VISIBLE
                itemView.commentTextView.text = comment.textComment
                itemView.mediaCommentCardView.visibility = View.GONE
            }


        }

        override fun onLongClick(p0: View?): Boolean {
            cClickListener.onCommentLongClicked(comments[adapterPosition])
            return true
        }

        override fun onClick(p0: View?) {

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