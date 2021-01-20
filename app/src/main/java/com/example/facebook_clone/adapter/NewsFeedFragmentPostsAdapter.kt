package com.example.facebook_clone.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils.POST_FROM_GROUP
import com.example.facebook_clone.helper.Utils.POST_FROM_PAGE
import com.example.facebook_clone.helper.Utils.POST_FROM_PROFILE
import com.example.facebook_clone.helper.listener.NewsFeedPostListener
import com.example.facebook_clone.model.post.Post
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.news_feed_post_item.view.*
import kotlinx.android.synthetic.main.profile_post_item.view.*
import kotlinx.android.synthetic.main.profile_post_item.view.attachmentImageView
import kotlinx.android.synthetic.main.profile_post_item.view.circleImageView
import kotlinx.android.synthetic.main.profile_post_item.view.playButtonImageView
import kotlinx.android.synthetic.main.profile_post_item.view.postContentTextView
import kotlinx.android.synthetic.main.profile_post_item.view.postTimeTextView
import kotlinx.android.synthetic.main.profile_post_item.view.postVisibilityImageView
import kotlinx.android.synthetic.main.profile_post_item.view.userNameTextView

class NewsFeedFragmentPostsAdapter(
    private val posts: List<Post>,
    private val newsFeedLstnr: NewsFeedPostListener
) :
    RecyclerView.Adapter<NewsFeedFragmentPostsAdapter.NewsFeedFragmentPostsViewHolder>() {
    private val picasso = Picasso.get()
    private val newsFeedPostListener = newsFeedLstnr
    inner class NewsFeedFragmentPostsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        init{
            itemView.circleImageView.setOnClickListener {
                val post = posts[adapterPosition]
                newsFeedPostListener.onUserImageClicked(post)
            }
            itemView.userNameTextView.setOnClickListener {
                val post = posts[adapterPosition]
                newsFeedPostListener.onUserNameClicked(post)
            }
            itemView.groupOrPageNameTextView.setOnClickListener {
                val post = posts[adapterPosition]
                newsFeedPostListener.onGroupOrPageNameClicked(post)
            }
            itemView.attachmentImageView.setOnClickListener {
                val post = posts[adapterPosition]
                newsFeedPostListener.onMediaClicked(post.attachmentUrl.orEmpty())
            }
        }
        fun bindPosts(post: Post) {
            picasso.load(post.publisherImageUrl).into(itemView.circleImageView)
            itemView.userNameTextView.text = post.publisherName
            itemView.postTimeTextView.text =
                DateFormat.format("EEEE, MMM d, yyyy h:mm a", post.creationTime.toDate())
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


            if (post.fromWhere == POST_FROM_PROFILE){
                itemView.groupOrPageNameTextView.visibility = View.GONE
                itemView.inGroupPostArrowImageView.visibility = View.GONE
            }
            else if (post.fromWhere == POST_FROM_GROUP){
                itemView.groupOrPageNameTextView.visibility = View.VISIBLE
                itemView.inGroupPostArrowImageView.visibility = View.VISIBLE
                itemView.groupOrPageNameTextView.text = post.groupName
            }
            else if (post.fromWhere == POST_FROM_PAGE){
                itemView.groupOrPageNameTextView.visibility = View.VISIBLE
                itemView.inGroupPostArrowImageView.visibility = View.VISIBLE
            }
            //Logic for visibility
//            val currentUserId = auth.currentUser?.uid.toString()
//            val postVisibility = post.visibility
//            if (currentUserId != post.publisherId) {
//                if (postVisibility == 0) {
//                    itemView.visibility = View.VISIBLE
//                    itemView.layoutParams =
//                        RecyclerView.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT
//                        )
//                } else if (postVisibility == 1) {
//                    if (iAmFriend == true) {
//                        itemView.visibility = View.VISIBLE
//                        itemView.layoutParams = RecyclerView.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT)
//                    } else {
//                        itemView.visibility = View.GONE
//                        itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
//                    }
//                } else {
//                    itemView.visibility = View.GONE
//                    itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
//                }
//            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewsFeedFragmentPostsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.news_feed_post_item, parent, false)
        return NewsFeedFragmentPostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsFeedFragmentPostsViewHolder, position: Int) {
        val post = posts[holder.adapterPosition]
        holder.bindPosts(post)
    }

    override fun getItemCount() = posts.size
}