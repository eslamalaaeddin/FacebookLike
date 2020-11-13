package com.example.facebook_clone.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.model.post.Post
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_post_item.view.*
import android.text.format.DateFormat.format
import android.util.Log
import com.example.facebook_clone.helper.listener.PostListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.comments_bottom_sheet.view.*

private const val TAG = "ProfilePostsAdapter"

//I passed auth to get to every user id
//I made the adapter implement Callback to get name and image of commenter
class ProfilePostsAdapter(
    private val auth: FirebaseAuth,
    private val postsList: List<Post>,
    private val postListener: PostListener,
    private val interactorName: String,
    private val interactorImageUrl: String
) :
    RecyclerView.Adapter<ProfilePostsAdapter.ProfilePostsViewHolder>() {
    private val picasso = Picasso.get()
    private lateinit var listener: PostListener
    private var reacted = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.profile_post_item, parent, false)
        return ProfilePostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int) {
        val post = postsList[holder.adapterPosition]
        holder.bind(post)
    }

    inner class ProfilePostsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
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
                    interactorImageUrl
                )
            }

            itemView.addReactTextView.setOnClickListener {
                val interactorId = auth.currentUser?.uid.toString()
                val post =  postsList[adapterPosition]
                val postId = post.id.toString()
                val postPublisherId = post.publisherId.toString()
                listener.onReactButtonClicked(
                    postPublisherId,
                    postId,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    reacted
                )
            }

//            itemView.addReactTextView.setOnLongClickListener {
//                val interactorId = auth.currentUser?.uid.toString()
//                val post = getItem(adapterPosition)
//                val postId = post.id.toString()
//                val postPublisherId = post.publisherId.toString()
//                listener.onReactButtonClicked(postPublisherId,postId, interactorId, interactorName, interactorImageUrl, reacted)
//                true
//            }

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
                    interactorImageUrl
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
                    interactorImageUrl
                )
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

            //Shares count
            if (post.shares != null && post.shares!!.isNotEmpty()) {
                itemView.sharesCountTextView.text = "${post.shares?.size.toString()} Shares"
            }
            //Comments count
            if (post.comments != null && post.comments!!.isNotEmpty()) {
                itemView.commentsCountsTextView.text = "${post.comments?.size.toString()} Comments"
            }
            //Reacts
            if (post.reacts != null && post.reacts!!.isNotEmpty()){
                post.reacts?.forEach { react ->
                    when (react.react) {
                        1 -> itemView.likeReactPlaceHolder.visibility = View.VISIBLE
                        2 -> itemView.loveReactPlaceHolder.visibility = View.VISIBLE
                        3 -> itemView.careReactPlaceHolder.visibility = View.VISIBLE
                        4 -> itemView.hahaReactPlaceHolder.visibility = View.VISIBLE
                        5 -> itemView.wowReactPlaceHolder.visibility = View.VISIBLE
                        6 -> itemView.sadReactPlaceHolder.visibility = View.VISIBLE
                        7 -> itemView.angryReactPlaceHolder.visibility = View.VISIBLE

                        else -> {
                            itemView.likeReactPlaceHolder.visibility = View.INVISIBLE
                            itemView.loveReactPlaceHolder.visibility = View.GONE
                            itemView.careReactPlaceHolder.visibility = View.GONE
                            itemView.hahaReactPlaceHolder.visibility = View.GONE
                            itemView.wowReactPlaceHolder.visibility = View.GONE
                            itemView.sadReactPlaceHolder.visibility = View.GONE
                            itemView.angryReactPlaceHolder.visibility = View.GONE}
                    }
                }
            }

            if (post.reacts != null && post.reacts!!.isNotEmpty()) {
                itemView.reactsCountTextView.text = post.reacts?.size.toString()
            }


//            post.reacts?.forEach { react ->
//                if (react.reactorId == auth.currentUser?.uid.toString() || post.reacts?.isEmpty()!!){
//                    reacted = true
//                    itemView.addReactTextView.setTextColor(itemView.context.resources.getColor(R.color.dark_blue))
//                    itemView.reactImageView.setBackgroundResource(R.drawable.ic_thumb_up)
//                }else{
//                    reacted = false
//                }
//            }
        }
    }

    override fun getItemCount(): Int {
        return postsList.size
    }

}