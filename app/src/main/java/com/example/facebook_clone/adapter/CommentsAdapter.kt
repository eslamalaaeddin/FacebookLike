package com.example.facebook_clone.adapter

import android.text.format.DateFormat.format
import android.util.Log
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
import kotlinx.android.synthetic.main.comment_item_layout.view.reactsCountTextView

private const val TAG = "CommentsAdapter"

class CommentsAdapter(
    private val commenterId: String,
    private var comments: List<Comment>,
    private var reacts: List<React>?,
    private val commentClickListener: CommentClickListener,
    private val reactClickListener: ReactClickListener,
    private val postViewModel: PostViewModel,
    private val postPublisherId: String
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
                val comment = comments[adapterPosition]
                var currentReact: React? = null
                var reacted: Boolean = false

                postViewModel.getCommentById(postPublisherId, comment.id.toString())
                    .addOnCompleteListener {
                        val commentDoc = it.result?.toObject(ReactionsAndSubComments::class.java)
                        commentDoc?.reactions?.let { reacts ->
                            for (react in reacts) {
                                if (react.reactorId == commenterId) {
                                    currentReact = react
                                    reacted = true
                                    break
                                }
                            }
                        }

                        cClickListener.onReplyToCommentClicked(
                            comment,
                            adapterPosition,
                            reacted,
                            currentReact
                        )
                    }
            }

            itemView.viewPreviousComments.setOnClickListener {
                val comment = comments[adapterPosition]
                var currentReact: React? = null
                var reacted: Boolean = false

                postViewModel.getCommentById(postPublisherId, comment.id.toString())
                    .addOnCompleteListener {
                        val commentDoc = it.result?.toObject(ReactionsAndSubComments::class.java)
                        commentDoc?.reactions?.let { reacts ->
                            for (react in reacts) {
                                if (react.reactorId == commenterId) {
                                    currentReact = react
                                    reacted = true
                                    break
                                }
                            }
                        }

                        cClickListener.onReplyToCommentClicked(
                            comment,
                            adapterPosition,
                            reacted,
                            currentReact
                        )
                    }
            }

            itemView.reactOnCommentTextView.setOnClickListener {
                val comment = comments[adapterPosition]
                var currentReact: React? = null
                var reacted: Boolean = false

                //I HAVE TO GET COMMENT DOCUMENT
                postViewModel.getCommentById(postPublisherId, comment.id.toString())
                    .addOnCompleteListener {
                        val commentDoc =
                            it.result?.toObject(ReactionsAndSubComments::class.java)
                        commentDoc?.reactions?.let { reacts ->
                            for (react in reacts) {
                                if (react.reactorId == commenterId) {
                                    currentReact = react
                                    reacted = true
                                    break
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

            itemView.reactOnCommentTextView.setOnLongClickListener {
                val comment = comments[adapterPosition]
                var currentReact: React? = null
                var reacted: Boolean = false

                //I HAVE TO GET COMMENT DOCUMENT
                postViewModel.getCommentById(postPublisherId, comment.id.toString())
                    .addOnCompleteListener {
                        val commentDoc =
                            it.result?.toObject(ReactionsAndSubComments::class.java)
                        commentDoc?.reactions?.let { reacts ->
                            for (react in reacts) {
                                if (react.reactorId == commenterId) {
                                    currentReact = react
                                    reacted = true
                                    break
                                }
                            }
                        }
                        cClickListener.onReactOnCommentLongClicked(
                            comment,
                            adapterPosition,
                            reacted,
                            currentReact
                        )
                    }
                true
            }

            itemView.mediaCommentCardView.setOnClickListener {
                val comment = comments[adapterPosition]
                cClickListener.onMediaCommentClicked(comment.attachmentCommentUrl.toString())
            }

            itemView.whoReactedOnCommentLayout.setOnClickListener {
                val commentId = comments[adapterPosition].id.toString()
                cClickListener.onCommentReactionsLayoutClicked(commentId)
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
                    picasso.load(comment.attachmentCommentUrl)
                        .into(itemView.mediaCommentImageView)
                    itemView.playButtonImgView.visibility = View.GONE
                    itemView.commentTextView.visibility = View.VISIBLE
                } else if (commentType == "textWithVideo") {
                    itemView.commentTextView.text = comment.textComment
                    itemView.playButtonImgView.visibility = View.VISIBLE
                    itemView.commentTextView.visibility = View.VISIBLE

                    Glide.with(itemView.context)
                        .asBitmap().load(comment.attachmentCommentUrl).apply(options)
                        .into(itemView.mediaCommentImageView)
                } else if (commentType == "image") {
                    itemView.commentTextView.visibility = View.GONE
                    picasso.load(comment.attachmentCommentUrl)
                        .into(itemView.mediaCommentImageView)
                    itemView.playButtonImgView.visibility = View.GONE
                } else if (commentType == "video") {
                    itemView.commentTextView.visibility = View.GONE
                    itemView.playButtonImgView.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .asBitmap().load(comment.attachmentCommentUrl).apply(options)
                        .into(itemView.mediaCommentImageView)
                }
            } else {
                itemView.commentTextView.visibility = View.VISIBLE
                itemView.commentTextView.text = comment.textComment
                itemView.mediaCommentCardView.visibility = View.GONE
            }

            postViewModel.getCommentById(postPublisherId, comment.id.toString())
                .addOnCompleteListener {
                    val commentDoc = it.result?.toObject(ReactionsAndSubComments::class.java)
                    //Comments
//                    if (commentDoc?.subComments != null){
//                        if (commentDoc.subComments!!.isNotEmpty()){
//                            itemView.viewPreviousComments.visibility = View.VISIBLE
//                        }else{
//                            itemView.viewPreviousComments.visibility = View.GONE
//                        }
//                    }
                    //Reacts
                    commentDoc?.reactions?.let { reacts ->
                        if (reacts.isNotEmpty()) {
                            itemView.reactsCountTextView.text = reacts.size.toString()
                        }
                        for (react in reacts) {
                            if (react.reactorId == commenterId) {
                                itemView.myReactPlaceHolder.visibility = View.VISIBLE
//                                itemView.likeReactPlaceHolder.visibility = View.GONE
//                                itemView.loveReactPlaceHolder.visibility = View.GONE
//                                itemView.careReactPlaceHolder.visibility = View.GONE
//                                itemView.hahaReactPlaceHolder.visibility = View.GONE
//                                itemView.wowReactPlaceHolder.visibility = View.GONE
//                                itemView.sadReactPlaceHolder.visibility = View.GONE
//                                itemView.angryReactPlaceHolder.visibility = View.GONE
                                when (react.react) {
                                    1 -> {
                                        itemView.reactOnCommentTextView.text = "Like"
                                        itemView.reactOnCommentTextView.setTextColor(
                                            itemView.context.resources.getColor(
                                                R.color.dark_blue
                                            )
                                        )
                                        itemView.myReactPlaceHolder.setImageResource(R.drawable.ic_like_react)
                                    }
                                    2 -> {
                                        itemView.reactOnCommentTextView.text = "Love"
                                        itemView.reactOnCommentTextView.setTextColor(
                                            itemView.context.resources.getColor(
                                                R.color.red
                                            )
                                        )
                                        itemView.myReactPlaceHolder.setImageResource(R.drawable.ic_love_react)
                                    }
                                    3 -> {
                                        itemView.reactOnCommentTextView.text = "Care"
                                        itemView.reactOnCommentTextView.setTextColor(
                                            itemView.context.resources.getColor(
                                                R.color.orange
                                            )
                                        )
                                        itemView.myReactPlaceHolder.setImageResource(R.drawable.ic_care_react)
                                    }
                                    4 -> {
                                        itemView.reactOnCommentTextView.text = "Haha"
                                        itemView.reactOnCommentTextView.setTextColor(
                                            itemView.context.resources.getColor(
                                                R.color.orange
                                            )
                                        )
                                        itemView.myReactPlaceHolder.setImageResource(R.drawable.ic_haha_react)
                                    }
                                    5 -> {
                                        itemView.reactOnCommentTextView.text = "Wow"
                                        itemView.reactOnCommentTextView.setTextColor(
                                            itemView.context.resources.getColor(
                                                R.color.orange
                                            )
                                        )
                                        itemView.myReactPlaceHolder.setImageResource(R.drawable.ic_wow_react)
                                    }
                                    6 -> {
                                        itemView.reactOnCommentTextView.text = "Sad"
                                        itemView.reactOnCommentTextView.setTextColor(
                                            itemView.context.resources.getColor(
                                                R.color.yellow
                                            )
                                        )
                                        itemView.myReactPlaceHolder.setImageResource(R.drawable.ic_sad_react)
                                    }
                                    7 -> {
                                        itemView.reactOnCommentTextView.text = "Angry"
                                        itemView.reactOnCommentTextView.setTextColor(
                                            itemView.context.resources.getColor(
                                                R.color.orange
                                            )
                                        )
                                        itemView.myReactPlaceHolder.setImageResource(R.drawable.ic_angry_angry)
                                    }
                                }
//                                itemView.reactOnCommentTextView.text = "Like"
//                                itemView.reactOnCommentTextView.setTextColor(
//                                    itemView.context.resources.getColor(
//                                        R.color.dark_blue
//                                    )
//                                )
                                break
                            }
                            else {
                                //Visible to me
                                itemView.myReactPlaceHolder.visibility = View.INVISIBLE
                                itemView.reactOnCommentTextView.text = "Like"
                                when (react.react) {
                                    1 -> {
                                        itemView.likeReactPlaceHolder.visibility = View.VISIBLE
                                        itemView.loveReactPlaceHolder.visibility = View.GONE
                                        itemView.careReactPlaceHolder.visibility = View.GONE
                                        itemView.hahaReactPlaceHolder.visibility = View.GONE
                                        itemView.wowReactPlaceHolder.visibility = View.GONE
                                        itemView.sadReactPlaceHolder.visibility = View.GONE
                                        itemView.angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    2 -> {
                                        itemView.likeReactPlaceHolder.visibility = View.GONE
                                        itemView.loveReactPlaceHolder.visibility = View.VISIBLE
                                        itemView.careReactPlaceHolder.visibility = View.GONE
                                        itemView.hahaReactPlaceHolder.visibility = View.GONE
                                        itemView.wowReactPlaceHolder.visibility = View.GONE
                                        itemView.sadReactPlaceHolder.visibility = View.GONE
                                        itemView.angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    3 -> {
                                        itemView.likeReactPlaceHolder.visibility = View.GONE
                                        itemView.loveReactPlaceHolder.visibility = View.GONE
                                        itemView.careReactPlaceHolder.visibility = View.VISIBLE
                                        itemView.hahaReactPlaceHolder.visibility = View.GONE
                                        itemView.wowReactPlaceHolder.visibility = View.GONE
                                        itemView.sadReactPlaceHolder.visibility = View.GONE
                                        itemView.angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    4 -> {
                                        itemView.likeReactPlaceHolder.visibility = View.GONE
                                        itemView.loveReactPlaceHolder.visibility = View.GONE
                                        itemView.careReactPlaceHolder.visibility = View.GONE
                                        itemView.hahaReactPlaceHolder.visibility = View.VISIBLE
                                        itemView.wowReactPlaceHolder.visibility = View.GONE
                                        itemView.sadReactPlaceHolder.visibility = View.GONE
                                        itemView.angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    5 -> {
                                        itemView.likeReactPlaceHolder.visibility = View.GONE
                                        itemView.loveReactPlaceHolder.visibility = View.GONE
                                        itemView.careReactPlaceHolder.visibility = View.GONE
                                        itemView.hahaReactPlaceHolder.visibility = View.GONE
                                        itemView.wowReactPlaceHolder.visibility = View.VISIBLE
                                        itemView.sadReactPlaceHolder.visibility = View.GONE
                                        itemView.angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    6 -> {
                                        itemView.likeReactPlaceHolder.visibility = View.GONE
                                        itemView.loveReactPlaceHolder.visibility = View.GONE
                                        itemView.careReactPlaceHolder.visibility = View.GONE
                                        itemView.hahaReactPlaceHolder.visibility = View.GONE
                                        itemView.wowReactPlaceHolder.visibility = View.GONE
                                        itemView.sadReactPlaceHolder.visibility = View.VISIBLE
                                        itemView.angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    7 -> {
                                        itemView.likeReactPlaceHolder.visibility = View.GONE
                                        itemView.loveReactPlaceHolder.visibility = View.GONE
                                        itemView.careReactPlaceHolder.visibility = View.GONE
                                        itemView.hahaReactPlaceHolder.visibility = View.GONE
                                        itemView.wowReactPlaceHolder.visibility = View.GONE
                                        itemView.sadReactPlaceHolder.visibility = View.GONE
                                        itemView.angryReactPlaceHolder.visibility = View.VISIBLE
                                    }
                                }
                            }

                        }
                    }
                }


        }

        override fun onLongClick(p0: View?): Boolean {
            val comment = comments[adapterPosition]
            //commenter id == interactor id
            Log.i(TAG, "UUUU onLongClick: $comment")
            if (commenterId == comment.commenterId) {
                cClickListener.onCommentLongClicked(comment)
            }
                return true

        }

        override fun onClick(p0: View?) {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.comment_item_layout, parent, false)

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