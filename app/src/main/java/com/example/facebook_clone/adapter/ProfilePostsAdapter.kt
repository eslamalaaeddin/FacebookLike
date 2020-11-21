package com.example.facebook_clone.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.model.post.Post
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_post_item.view.*
import android.text.format.DateFormat.format
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.model.post.react.React
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "ProfilePostsAdapter"

//I passed auth to get to every user id
//I made the adapter implement Callback to get name and image of commenter
class ProfilePostsAdapter(
    private val auth: FirebaseAuth,
    private val postsList: List<Post>,
    private val postListener: PostListener,
    private val interactorName: String,
    private val interactorImageUrl: String,
    private val iAmFriend: Boolean
) :
    RecyclerView.Adapter<ProfilePostsAdapter.ProfilePostsViewHolder>() {
    private val picasso = Picasso.get()
    private lateinit var listener: PostListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.profile_post_item, parent, false)
        return ProfilePostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int) {
        val post = postsList[holder.adapterPosition]
        holder.bind(post)
    }

    inner class ProfilePostsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        init {
            itemView.setOnClickListener(this)

            listener = postListener

            itemView.addCommentTextView.setOnClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post = postsList[adapterPosition]
                val postId = post.id.toString()
                val postPublisherId = post.publisherId.toString()
                listener.onCommentButtonClicked(
                    postPublisherId,
                    postId,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    adapterPosition
                )
            }

            itemView.addReactTextView.setOnClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post = postsList[adapterPosition]
                val postId = post.id.toString()
                val postPublisherId = post.publisherId.toString()
                val currentPostReacts = post.reacts.orEmpty()
                var currentReact: React? = null
                var reacted: Boolean = false

                post.reacts?.let { reacts ->
                    for (react in reacts) {
                        if (react.reactorId == interactorId) {
                            currentReact = react
                            reacted = true
                            break
                        }
                    }
                }

                listener.onReactButtonClicked(
                    postPublisherId,
                    postId,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    reacted,
                    currentReact,//null
                    adapterPosition
                )

            }

            itemView.addReactTextView.setOnLongClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post = postsList[adapterPosition]
                val postId = post.id.toString()
                val postPublisherId = post.publisherId.toString()
                val currentPostReacts = post.reacts.orEmpty()
                var currentReact: React? = null
                var reacted: Boolean = false

                post.reacts?.let { reacts ->
                    for (react in reacts) {
                        if (react.reactorId == interactorId) {
                            currentReact = react
                            reacted = true
                            break
                        }
                    }
                }

                listener.onReactButtonLongClicked(
                    postPublisherId,
                    postId,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    reacted,
                    currentReact,//null
                    adapterPosition
                )
                true
            }

            itemView.addShareTextView.setOnClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post = postsList[adapterPosition]
                val postId = post.id.toString()
                val postPublisherId = post.publisherId.toString()
                listener.onShareButtonClicked(
                    postPublisherId,
                    postId,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    adapterPosition
                )
            }

            itemView.reactsLayout.setOnClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post = postsList[adapterPosition]
                val postId = post.id.toString()
                val postPublisherId = post.publisherId.toString()
                listener.onReactLayoutClicked(
                    postPublisherId,
                    postId,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    adapterPosition
                )
            }

            itemView.moreOnPostButton.setOnClickListener {
                val post = postsList[adapterPosition]
                listener.onPostMoreDotsClicked(post)
            }

        }

        @SuppressLint("SetTextI18n")
        fun bind(post: Post) {
            picasso.load(post.publisherImageUrl).into(itemView.circleImageView)
            itemView.userNameTextView.text = post.publisherName
            itemView.postTimeTextView.text =
                format("EEEE, MMM d, yyyy h:mm a", post.creationTime.toDate())
            //visibility
            val visibility = post.visibility
            if (visibility == 0) {
                itemView.postVisibilityImageView.setImageResource(R.drawable.ic_public_visibility)
            } else if (visibility == 1) {
                itemView.postVisibilityImageView.setImageResource(R.drawable.ic_friends_visibility)
            } else {
                itemView.postVisibilityImageView.setImageResource(R.drawable.ic_private_visibility)
            }
            //Content
            itemView.postContentTextView.text = post.content

            if (post.attachmentUrl != null) {
                itemView.attachmentImageView.visibility = View.VISIBLE
                if (post.attachmentType == "image") {
                    picasso.load(post.attachmentUrl).into(itemView.attachmentImageView)
                    itemView.playButtonImageView.visibility = View.GONE
                } else if (post.attachmentType == "video") {
                    itemView.playButtonImageView.visibility = View.VISIBLE
                    val interval: Long = 1 * 1000
                    val options: RequestOptions = RequestOptions().frame(interval)
                    Glide.with(itemView.context)
                        .asBitmap().load(post.attachmentUrl).apply(options)
                        .into(itemView.attachmentImageView)
                }
            } else {
                itemView.attachmentImageView.visibility = View.GONE
                itemView.playButtonImageView.visibility = View.GONE
            }

            //Shares count
            if (post.shares != null && post.shares!!.isNotEmpty()) {
                itemView.sharesCountTextView.text = "${post.shares?.size.toString()} Shares"
            }
            //Comments count
            if (post.comments != null && post.comments!!.isNotEmpty()) {
                itemView.commentsCountsTextView.text = "${post.comments?.size.toString()} Comments"
            }
            //Reacts
//            if (post.reacts != null && post.reacts!!.isNotEmpty()) {
//                post.reacts?.forEach { react ->
//                    when (react.react) {
//                        1 -> itemView.likeReactPlaceHolder.visibility = View.VISIBLE
//                        2 -> itemView.loveReactPlaceHolder.visibility = View.VISIBLE
//                        3 -> itemView.careReactPlaceHolder.visibility = View.VISIBLE
//                        4 -> itemView.hahaReactPlaceHolder.visibility = View.VISIBLE
//                        5 -> itemView.wowReactPlaceHolder.visibility = View.VISIBLE
//                        6 -> itemView.sadReactPlaceHolder.visibility = View.VISIBLE
//                        7 -> itemView.angryReactPlaceHolder.visibility = View.VISIBLE
//
//                        else -> {
//                            itemView.likeReactPlaceHolder.visibility = View.GONE
//                            itemView.loveReactPlaceHolder.visibility = View.GONE
//                            itemView.careReactPlaceHolder.visibility = View.GONE
//                            itemView.hahaReactPlaceHolder.visibility = View.GONE
//                            itemView.wowReactPlaceHolder.visibility = View.GONE
//                            itemView.sadReactPlaceHolder.visibility = View.GONE
//                            itemView.angryReactPlaceHolder.visibility = View.GONE
//                        }
//                    }
//                }
//            }

            if (post.reacts != null && post.reacts!!.isNotEmpty()) {
                itemView.reactsCountTextView.text = post.reacts?.size.toString()
            }

            post.reacts?.let { reacts ->
                for (react in reacts) {
                    // || post.reacts?.isEmpty()!!
                    if (react.reactorId == auth.currentUser?.uid.toString()) {
                        itemView.reactImageViewGrey.visibility = View.INVISIBLE
                        itemView.reactImageViewBlue.visibility = View.VISIBLE
                        when (react.react) {
                            1 -> {
                                itemView.addReactTextView.text = "Like"
                                itemView.addReactTextView.setTextColor(
                                    itemView.context.resources.getColor(
                                        R.color.dark_blue
                                    )
                                )
                                itemView.reactImageViewBlue.setImageResource(R.drawable.ic_thumb_up)
                            }
                            2 -> {
                                itemView.addReactTextView.text = "Love"
                                itemView.addReactTextView.setTextColor(
                                    itemView.context.resources.getColor(
                                        R.color.red
                                    )
                                )
                                itemView.reactImageViewBlue.setImageResource(R.drawable.ic_love_react)
                            }
                            3 -> {
                                itemView.addReactTextView.text = "Care"
                                itemView.addReactTextView.setTextColor(
                                    itemView.context.resources.getColor(
                                        R.color.orange
                                    )
                                )
                                itemView.reactImageViewBlue.setImageResource(R.drawable.ic_care_react)
                            }
                            4 -> {
                                itemView.addReactTextView.text = "Haha"
                                itemView.addReactTextView.setTextColor(
                                    itemView.context.resources.getColor(
                                        R.color.orange
                                    )
                                )
                                itemView.reactImageViewBlue.setImageResource(R.drawable.ic_haha_react)
                            }
                            5 -> {
                                itemView.addReactTextView.text = "Wow"
                                itemView.addReactTextView.setTextColor(
                                    itemView.context.resources.getColor(
                                        R.color.orange
                                    )
                                )
                                itemView.reactImageViewBlue.setImageResource(R.drawable.ic_wow_react)
                            }
                            6 -> {
                                itemView.addReactTextView.text = "Sad"
                                itemView.addReactTextView.setTextColor(
                                    itemView.context.resources.getColor(
                                        R.color.yellow
                                    )
                                )
                                itemView.reactImageViewBlue.setImageResource(R.drawable.ic_sad_react)
                            }
                            7 -> {
                                itemView.addReactTextView.text = "Angry"
                                itemView.addReactTextView.setTextColor(
                                    itemView.context.resources.getColor(
                                        R.color.orange
                                    )
                                )
                                itemView.reactImageViewBlue.setImageResource(R.drawable.ic_angry_angry)
                            }
                        }
                        break
                    }
                    //no react from me
                    else {
                        itemView.reactImageViewGrey.visibility = View.VISIBLE
                        itemView.reactImageViewBlue.visibility = View.INVISIBLE
                        itemView.addReactTextView.text = "Like"
                        //itemView.likeReactPlaceHolder.visibility = View.INVISIBLE
                    }
                }
            }


            val currentUserId = auth.currentUser?.uid.toString()
            val postVisibility = post.visibility
            if (currentUserId != post.publisherId) {
                if (postVisibility == 0) {
                    itemView.visibility = View.VISIBLE
                    itemView.layoutParams =
                        RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                } else if (postVisibility == 1) {
                    if (iAmFriend) {
                        itemView.visibility = View.VISIBLE
                        itemView.layoutParams =
                            RecyclerView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                    } else {
                        itemView.visibility = View.GONE
                        itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                    }
                } else {
                    itemView.visibility = View.GONE
                    itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                }
            }

            if (auth.currentUser?.uid.toString() == post.publisherId){
                itemView.moreOnPostButton.visibility = View.VISIBLE
            }else{
                itemView.moreOnPostButton.visibility = View.GONE
            }
        }

        override fun onClick(p0: View?) {
            val post = postsList[adapterPosition]
            if (post.attachmentType != null) {
                if (post.attachmentType == "video") {
                    listener.onMediaPostClicked(post.attachmentUrl.toString())
                } else {
                    listener.onMediaPostClicked(post.attachmentUrl.toString())
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return postsList.size
    }


}