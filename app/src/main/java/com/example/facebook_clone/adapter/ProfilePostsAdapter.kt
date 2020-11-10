package com.example.facebook_clone.adapter

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

private const val TAG = "ProfilePostsAdapter"
class ProfilePostsAdapter(
    private val options: FirestoreRecyclerOptions<Post>,
) :
    FirestoreRecyclerAdapter<Post, ProfilePostsAdapter.ProfilePostsViewHolder>(options) {
    private val picasso = Picasso.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.profile_post_item, parent, false)
        return ProfilePostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int, post: Post) {
        holder.bind(post)
    }

    inner class ProfilePostsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(post: Post){
            picasso.load(post.publisherImageUrl).into(itemView.circleImageView)
            itemView.userNameTextView.text = post.publisherName

            itemView.postTimeTextView.text =format("EEEE, MMM d, yyyy h:mm a", post.creationTime.toDate())

            //visibility
            val visibility = post.visibility
            if (visibility == 0){
                itemView.postVisibilityImageView.setImageResource(R.drawable.ic_public_visibility)
            }
            else if(visibility == 1){
                itemView.postVisibilityImageView.setImageResource(R.drawable.ic_friends_visibility)
            }
            else{
                itemView.postVisibilityImageView.setImageResource(R.drawable.ic_private_visibility)
            }

            Log.i(TAG, "ISLAM bind: ${post.content}")
            //Content
            itemView.postContentTextView.text = post.content


        }
    }

    private fun getPublisher(publisherId: String){
        //Instead you should store his name, and image url
    }
}