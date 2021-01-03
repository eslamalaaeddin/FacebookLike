package com.example.facebook_clone.ui.dialog

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.PostVisibilityAdapter
import com.example.facebook_clone.helper.provider.NameImageProvider
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.Utils.GROUP_POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.POST_FROM_GROUP
import com.example.facebook_clone.helper.Utils.POST_FROM_PAGE
import com.example.facebook_clone.helper.Utils.POST_FROM_PROFILE
import com.example.facebook_clone.helper.Utils.PROFILE_POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.SPECIFIC_GROUP_POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.toastMessage
import com.example.facebook_clone.helper.listener.PostAttachmentListener
import com.example.facebook_clone.model.Visibility
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.ui.activity.NewsFeedActivity
import com.example.facebook_clone.ui.bottomsheet.AddToPostBottomSheet
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_creator_dialog.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "PostCreatorDialog"

class PostCreatorDialog(private val fromWhere: String,
                        private val groupId: String? = null,
                        private val groupName: String? = null
) : DialogFragment(), AdapterView.OnItemSelectedListener, NameImageProvider,
    PostAttachmentListener {
    private val auth: FirebaseAuth by inject()
    private val postViewModel by viewModel<PostViewModel>()
    private var progressDialog: ProgressDialog? = null
    private var postVisibility = 0
    private var postData: Intent? = null
    private var postDataType: String? = null
    private var bitmapFromCamera: Boolean = false
    private val currentUserId = auth.currentUser?.uid.toString()
    private lateinit var userName: String
    private lateinit var userProfileImageUrl: String
    private var postAttachmentUrl: String? = null
    private var postAttachmentType: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.post_creator_dialog, container, false)
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUpPostCreatorUI()
        addToPostButton.setOnClickListener {showPostAttachmentBottomSheet()}
        postContentTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(content: Editable?) {
                createPostButton.setTextColor(resources.getColor(R.color.gray))
                if (content?.isEmpty()!!) {
                    createPostButton.isEnabled = false
                    createPostButton.setTextColor(resources.getColor(R.color.gray))
                } else if (content.isNotEmpty() || postData != null) {
                    createPostButton.isEnabled = true
                    createPostButton.setTextColor(resources.getColor(R.color.dark_blue))
                }
            }
        })


        /*
            if(from group){
                all the sturr
            }
         */

        createPostButton.setOnClickListener {
            if (postData != null) {
                if (postDataType == "image") {
                    var bitmap: Bitmap? = null
                    if (bitmapFromCamera) {
                        bitmap = postData?.extras?.get("data") as Bitmap
                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(
                            activity?.contentResolver,
                            postData!!.data
                        )
                    }
                    postAtachmentImageView.setImageBitmap(bitmap)
                    progressDialog = Utils.showProgressDialog(requireContext(), "Please wait...")
                    postViewModel.uploadPostImageToCloudStorage(bitmap!!)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                                    postAttachmentUrl = photoUrl.toString()
                                    val post = createPost(postAttachmentUrl!!, postDataType!!)
                                    postViewModel.createPost(post).addOnCompleteListener { task ->
                                        progressDialog?.dismiss()
                                        if (task.isSuccessful){
                                            if (fromWhere == POST_FROM_GROUP){
                                                postViewModel.addGroupPostToPosterCollection(post)
                                            }
                                        }
                                        else{
                                            toastMessage(requireContext(), task.exception?.message.toString())
                                        }
                                    }
                                    dismiss()
                                }
                            }
                        }
                }
                else if (postDataType == "video") {
                    val videoUri = postData!!.data!!
                    progressDialog = Utils.showProgressDialog(requireContext(), "Please wait...")
                    postViewModel.uploadPostVideoToCloudStorage(videoUri)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                task.result?.storage?.downloadUrl?.addOnSuccessListener { videoUrl ->
                                    postAttachmentUrl = videoUrl.toString()
                                    val post = createPost(postAttachmentUrl!!, postDataType!!)
                                    postViewModel.createPost(post).addOnCompleteListener { task ->
                                        progressDialog?.dismiss()
                                        if (task.isSuccessful){
                                            if (fromWhere == POST_FROM_GROUP){
                                                postViewModel.addGroupPostToPosterCollection(post)
                                            }
                                        }
                                        else{
                                            toastMessage(requireContext(), task.exception?.message.toString())
                                        }
                                        dismiss()
                                    }

                                }
                            }
                        }
                }
            }
            //Text post
            else if (postContentTextView.text.toString().isNotEmpty()) {
                val post = createPost()
                progressDialog = Utils.showProgressDialog(requireContext(), "Please wait...")

                postViewModel.createPost(post).addOnCompleteListener { task ->
                    progressDialog?.dismiss()
                    if (task.isSuccessful){
                        if (fromWhere == POST_FROM_GROUP){
                            postViewModel.addGroupPostToPosterCollection(post)
                        }
                    }
                    else{
                        toastMessage(requireContext(), task.exception?.message.toString())
                    }
                    dismiss()
                }
            }
        }

    }

    private fun setUpPostCreatorUI(){
        upButtonImageView.setOnClickListener { dismiss() }

        postVisibilitySpinner.onItemSelectedListener = this

        Picasso.get().load(userProfileImageUrl).into(smallUserImageView)
        userNameTextView.text = userName

        val publicOption = Visibility(R.drawable.ic_public_visibility, "Public")
        val friendsOption = Visibility(R.drawable.ic_friends_visibility, "Friends")
        val privateOption = Visibility(R.drawable.ic_private_visibility, "Private")

        val visibilities = mutableListOf(publicOption, friendsOption, privateOption)

        val adapter = PostVisibilityAdapter(requireContext(), visibilities)
        postVisibilitySpinner.adapter = adapter
    }

    private fun showPostAttachmentBottomSheet(){
        val addToPostBottomSheet = AddToPostBottomSheet(this)
        addToPostBottomSheet.show(activity?.supportFragmentManager!!, addToPostBottomSheet.tag)
    }

    //Visibility Spinner Stuff
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        postVisibility = position
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    private fun createPost(postAttachmentUrl: String? = null, postAttachmentType: String? = null): Post {
        val content = postContentTextView.text.toString()
        val post = Post(
            publisherId = currentUserId,
            content = content,
            visibility = postVisibility,
            publisherName = userName,
            publisherImageUrl = userProfileImageUrl,
            attachmentUrl = postAttachmentUrl,
            attachmentType = postAttachmentType,
            //publisherToken = NewsFeedActivity.getTokenFromSharedPreference(requireContext())
             )

        when(fromWhere){
            POST_FROM_PROFILE -> {
                post.firstCollectionType = POSTS_COLLECTION
                post.creatorReferenceId = currentUserId
                post.secondCollectionType = PROFILE_POSTS_COLLECTION
            }

            POST_FROM_GROUP -> {
                post.groupId = groupId
                post.groupName = groupName
                post.firstCollectionType = GROUP_POSTS_COLLECTION
                post.creatorReferenceId = groupId.orEmpty()
                post.secondCollectionType = SPECIFIC_GROUP_POSTS_COLLECTION
            }

//            POST_FROM_PAGE -> {
//                post.firstCollectionType = POSTS_COLLECTION
//                post.creatorReferenceId = currentUserId
//                post.firstCollectionType = PROFILE_POSTS_COLLECTION
//            }
        }
        return post
    }

    override fun setUserNameAndProfileImageUrl(name: String, url: String) {
        userName = name
        userProfileImageUrl = url
    }

    override fun onAttachmentAdded(data: Intent?, dataType: String, fromCamera: Boolean) {
        if (data != null) {
            postData = data
            postDataType = dataType
            bitmapFromCamera = fromCamera

            if (postDataType == "image") {
                var bitmap: Bitmap? = null
                if (bitmapFromCamera) {
                    bitmap = postData?.extras?.get("data") as Bitmap
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(
                        activity?.contentResolver,
                        postData!!.data
                    )
                }
                postAtachmentImageView.setImageBitmap(bitmap)
            } else if (postDataType == "video") {
                val videoUri = postData!!.data!!
                val interval: Long = 1 * 1000
                val options: RequestOptions = RequestOptions().frame(interval)
                Glide.with(requireContext())
                    .asBitmap().load(videoUri).apply(options)
                    .into(postAtachmentImageView)
            }

        }

    }
}