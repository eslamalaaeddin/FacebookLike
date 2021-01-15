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
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.facebook_clone.helper.Utils.POST_FROM_GROUP
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.model.post.react.React
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_post_viewer.view.*
import kotlinx.android.synthetic.main.post_visibility_item_layout.view.*
import kotlinx.android.synthetic.main.profile_post_item.view.addCommentImageView
import kotlinx.android.synthetic.main.profile_post_item.view.addCommentTextView
import kotlinx.android.synthetic.main.profile_post_item.view.addReactTextView
import kotlinx.android.synthetic.main.profile_post_item.view.addShareTextView
import kotlinx.android.synthetic.main.profile_post_item.view.attachmentImageView
import kotlinx.android.synthetic.main.profile_post_item.view.circleImageView
import kotlinx.android.synthetic.main.profile_post_item.view.commentsCountsTextView
import kotlinx.android.synthetic.main.profile_post_item.view.moreOnPost
import kotlinx.android.synthetic.main.profile_post_item.view.playButtonImageView
import kotlinx.android.synthetic.main.profile_post_item.view.postContentTextView
import kotlinx.android.synthetic.main.profile_post_item.view.postTimeTextView
import kotlinx.android.synthetic.main.profile_post_item.view.postVisibilityImageView
import kotlinx.android.synthetic.main.profile_post_item.view.reactImageViewBlue
import kotlinx.android.synthetic.main.profile_post_item.view.reactImageViewGrey
import kotlinx.android.synthetic.main.profile_post_item.view.reactsCountTextView
import kotlinx.android.synthetic.main.profile_post_item.view.reactsLayout
import kotlinx.android.synthetic.main.profile_post_item.view.sharesCountTextView
import kotlinx.android.synthetic.main.profile_post_item.view.userNameTextView
import kotlinx.android.synthetic.main.shared_post_layout.view.*
import kotlinx.android.synthetic.main.shared_post_layout.view.addCommentImageView as addCommentImageView1

private const val TAG = "ProfilePostsAdapter"

//I passed auth to get to every user id
//I made the adapter implement Callback to get name and image of commenter
class ProfilePostsAdapter(
    private val auth: FirebaseAuth,
    private var posts: List<Post>,
    private val postListener: PostListener,
    private val interactorName: String,
    private val interactorImageUrl: String,
    private val iAmFriend: Boolean?,
    private val userId: String,
    private var fromWhere: String = ""
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val picasso = Picasso.get()
    private lateinit var listener: PostListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View

        if (viewType == 0) {
            view = layoutInflater.inflate(R.layout.profile_post_item, parent, false)
            return ProfilePostsViewHolder(view)
        }

        view = layoutInflater.inflate(R.layout.shared_post_layout, parent, false)
        return SharedPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sharedPostPublisherId = posts[position].shares?.lastOrNull()?.sharedPost?.publisherId
        if ( sharedPostPublisherId != null && sharedPostPublisherId != userId ) {
            val sharedPostViewHolder = holder as SharedPostViewHolder
            val post = posts[sharedPostViewHolder.adapterPosition]
            sharedPostViewHolder.bindSharedPost(post)

        } else {
            val postViewHolder = holder as ProfilePostsViewHolder
            val post = posts[postViewHolder.adapterPosition]
            postViewHolder.bind(post)
        }


    }

    inner class ProfilePostsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)

            listener = postListener

            itemView.addCommentTextView.setOnClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post = posts[adapterPosition]
                val postId = post.id.toString()
                val postPublisherId = post.publisherId.toString()
                listener.onCommentButtonClicked(
                    post,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    adapterPosition
                )
            }

            itemView.addReactTextView.setOnClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post = posts[adapterPosition]
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
                    post,
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
                val post = posts[adapterPosition]
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
                    post,
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
                val post = posts[adapterPosition]
                listener.onShareButtonClicked(
                    post,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    adapterPosition
                )
            }

            itemView.reactsLayout.setOnClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post = posts[adapterPosition]
                listener.onReactLayoutClicked(
                    post,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    adapterPosition
                )
            }

            itemView.moreOnPost.setOnClickListener {
                val post = posts[adapterPosition]
                val interactorId = auth.currentUser?.uid.toString()
                listener.onPostMoreDotsClicked(interactorId, post, null)
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
                            1 -> {handleReactsPositioning(itemView,"Like", R.color.dark_blue, R.drawable.ic_thumb_up)}
                            2 -> {handleReactsPositioning(itemView,"Love", R.color.red, R.drawable.ic_love_react)}
                            3 -> {handleReactsPositioning(itemView,"Care", R.color.orange, R.drawable.ic_care_react)}
                            4 -> {handleReactsPositioning(itemView,"Haha", R.color.orange, R.drawable.ic_haha_react)}
                            5 -> {handleReactsPositioning(itemView,"Wow", R.color.orange, R.drawable.ic_wow_react)}
                            6 -> {handleReactsPositioning(itemView,"Sad", R.color.yellow, R.drawable.ic_sad_react)}
                            7 -> {handleReactsPositioning(itemView,"Angry", R.color.orange, R.drawable.ic_angry_angry)}
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
                    if (iAmFriend == true) {
                        itemView.visibility = View.VISIBLE
                        itemView.layoutParams = RecyclerView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT)
                    } else {
                        itemView.visibility = View.GONE
                        itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                    }
                } else {
                    itemView.visibility = View.GONE
                    itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                }
            }

            if (auth.currentUser?.uid.toString() == post.publisherId || fromWhere == POST_FROM_GROUP) {
                itemView.moreOnPost.visibility = View.VISIBLE
            } else {
                itemView.moreOnPost.visibility = View.GONE
            }

            //To remove visibility icon in group
            if (post.fromWhere == POST_FROM_GROUP){
                itemView.postVisibilityImageView?.let {
                    it.setImageResource(R.drawable.ic_group_top)
                }
            }else{
                itemView.postVisibilityImageView?.let {
                    it.visibility = View.VISIBLE
                }
            }

            if (post.commentsAvailable){
                itemView.addCommentTextView.visibility = View.VISIBLE
                itemView.addCommentImageView.visibility = View.VISIBLE
                itemView.reactsLayout.visibility = View.VISIBLE
            }
            else{
                itemView.addCommentTextView.visibility = View.GONE
                itemView.addCommentImageView.visibility = View.GONE
                itemView.reactsLayout.visibility = View.GONE
            }
        }

        override fun onClick(p0: View?) {
            val post = posts[adapterPosition]
            if (post.attachmentType != null) {
                if (post.attachmentType == "video") {
                    listener.onMediaPostClicked(post.attachmentUrl.toString())
                } else {
                    listener.onMediaPostClicked(post.attachmentUrl.toString())
                }
            }
        }
    }

    inner class SharedPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) , View.OnClickListener{

        init {
            itemView.setOnClickListener(this)
            listener = postListener

            /////////////////////////////////////////////////////////////


            itemView.addCommentTextView.setOnClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post = posts[adapterPosition]
                listener.onCommentButtonClicked(
                    post,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    adapterPosition
                )
            }

            itemView.addReactTextView.setOnClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post = posts[adapterPosition]
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
                    post,
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
                val post = posts[adapterPosition]
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
                    post,
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
                Toast.makeText(itemView.context, "Cyclic share is not available till now", Toast.LENGTH_SHORT).show()
            }

            itemView.reactsLayout.setOnClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post = posts[adapterPosition]
                listener.onReactLayoutClicked(
                    post,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    adapterPosition
                )
            }

            //////////////////////////////////////////////////////////////////////
            itemView.moreOnPost.setOnClickListener {
                val post = posts[adapterPosition]
                val interactorId = auth.currentUser?.uid.toString()
                listener.onPostMoreDotsClicked(interactorId, post, true)
            }
        }

        override fun onClick(p0: View?) {
            val post = posts[adapterPosition]
            val postPublisherId = post.shares?.lastOrNull()?.sharedPost?.publisherId.toString()
            val postId = post.shares?.lastOrNull()?.sharedPost?.id.toString()

            listener.onSharedPostClicked(postPublisherId, postId)
        }

        @SuppressLint("SetTextI18n")
        fun bindSharedPost(post: Post) {
            val currentShare = post.shares?.last()
            val sharedPost = post.shares?.last()?.sharedPost
            Log.i(TAG, "ISLAM bindSharedPost: $sharedPost")
            currentShare?.let { share ->
                picasso.load(share.sharerImageUrl).into(itemView.circleImageView)
                itemView.userNameTextView.text = share.sharerName
                itemView.postTimeTextView.text =
                    format("EEEE, MMM d, yyyy h:mm a", share.shareTime.toDate())
            }


            picasso.load(sharedPost?.publisherImageUrl).into(itemView.postOriginalCreatorImageView)
            itemView.postOriginalCreatorNameTextView.text = sharedPost?.publisherName
            itemView.sharedPostCreationTimeTextView.text =
                format("EEEE, MMM d, yyyy h:mm a", sharedPost?.creationTime?.toDate())

            //visibility
            val visibility = post?.visibility
            if (visibility == -1){
                itemView.visibility = View.GONE
                itemView.layoutParams = RecyclerView.LayoutParams(0, 0)

            }
            else {
                itemView.visibility = View.VISIBLE
                itemView.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
                if (visibility == 0) {
                    itemView.sharedPostVisibilityImageView.setImageResource(R.drawable.ic_public_visibility)
                } else if (visibility == 1) {
                    itemView.sharedPostVisibilityImageView.setImageResource(R.drawable.ic_friends_visibility)
                } else if (visibility == 2) {
                    itemView.sharedPostVisibilityImageView.setImageResource(R.drawable.ic_private_visibility)
                }
            }


            itemView.postContentTextView.text = sharedPost?.content
            if (sharedPost?.attachmentUrl != null) {
                itemView.sharedPostAttachmentImageView.visibility = View.VISIBLE
                if (sharedPost?.attachmentType == "image") {
                    picasso.load(sharedPost?.attachmentUrl).into(itemView.sharedPostAttachmentImageView)
                    itemView.sharedPostPlayButtonImageView.visibility = View.GONE
                } else if (sharedPost?.attachmentType == "video") {
                    itemView.sharedPostPlayButtonImageView.visibility = View.VISIBLE
                    val interval: Long = 1 * 1000
                    val options: RequestOptions = RequestOptions().frame(interval)
                    Glide.with(itemView.context)
                        .asBitmap().load(sharedPost?.attachmentUrl).apply(options)
                        .into(itemView.sharedPostAttachmentImageView)
                }
            } else {
                itemView.sharedPostAttachmentImageView.visibility = View.GONE
                itemView.sharedPostPlayButtonImageView.visibility = View.GONE
            }
            //Shares count
            if (post.shares != null && post.shares!!.isNotEmpty()) {
                itemView.sharesCountTextView.text = "${post.shares?.size.toString()} Shares"
            }
            //Comments count
            if (post.comments != null && post.comments!!.isNotEmpty()) {
                itemView.commentsCountsTextView.text = "${post.comments?.size.toString()} Comments"
            }
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
                            1 -> {handleReactsPositioning(itemView,"Like", R.color.dark_blue, R.drawable.ic_thumb_up)}
                            2 -> {handleReactsPositioning(itemView,"Love", R.color.red, R.drawable.ic_love_react)}
                            3 -> {handleReactsPositioning(itemView,"Care", R.color.orange, R.drawable.ic_care_react)}
                            4 -> {handleReactsPositioning(itemView,"Haha", R.color.orange, R.drawable.ic_haha_react)}
                            5 -> {handleReactsPositioning(itemView,"Wow", R.color.orange, R.drawable.ic_wow_react)}
                            6 -> {handleReactsPositioning(itemView,"Sad", R.color.yellow, R.drawable.ic_sad_react)}
                            7 -> {handleReactsPositioning(itemView,"Angry", R.color.orange, R.drawable.ic_angry_angry)}
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
                    if (iAmFriend == true) {
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
            if (postVisibility == 0) {
                itemView.postVisibilityImageView.setImageResource(R.drawable.ic_public_visibility)
            } else if (postVisibility == 1) {
                itemView.postVisibilityImageView.setImageResource(R.drawable.ic_friends_visibility)
            } else {
                itemView.postVisibilityImageView.setImageResource(R.drawable.ic_private_visibility)
            }

            if (auth.currentUser?.uid.toString() == post.shares?.last()?.sharerId) {
                itemView.moreOnPost.visibility = View.VISIBLE
            } else {
                itemView.moreOnPost.visibility = View.GONE
            }

        }


    }

    override fun getItemViewType(position: Int): Int {
        val sharedPostPublisherId = posts[position].shares?.lastOrNull()?.sharedPost?.publisherId
        if ( sharedPostPublisherId != null && sharedPostPublisherId != userId ) {
            return 1//Shared
        } else {
            return 0
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }


    fun setPosts(newPosts: List<Post>) {
        posts = newPosts
    }

    private fun handleReactsPositioning(itemView: View, reactText: String, colorId: Int, imageId: Int) {
        itemView.addReactTextView.text = reactText
        itemView.addReactTextView.setTextColor(itemView.resources.getColor(colorId))
        itemView.reactImageViewBlue.setImageResource(imageId)
    }


}