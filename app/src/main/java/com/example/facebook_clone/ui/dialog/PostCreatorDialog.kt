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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.AddToPostAdapter
import com.example.facebook_clone.adapter.PostVisibilityAdapter
import com.example.facebook_clone.helper.provider.NameImageProvider
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.PostAttachmentListener
import com.example.facebook_clone.model.Option
import com.example.facebook_clone.model.Visibility
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.ui.bottomsheet.AddToPostBottomSheet
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.add_to_post_bottom_sheet.view.*
import kotlinx.android.synthetic.main.post_creator_dialog.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "PostCreatorDialog"
class PostCreatorDialog : DialogFragment(), AdapterView.OnItemSelectedListener, NameImageProvider, PostAttachmentListener {
    private val auth: FirebaseAuth by inject()
    private val postViewModel by viewModel<PostViewModel>()
    private var progressDialog: ProgressDialog? = null
    private var postVisibility = 0
    private var postData: Intent? = null
    private var postDataType: String? = null
    private var bitmapFromCamera: Boolean = false
    private lateinit var userName: String
    private lateinit var userProfileImageUrl: String
    private var postAttachmentUrl:String? = null
    private var postAttachmentType:String? = null
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
        upButtonImageView.setOnClickListener { dismiss() }

        postVisibilitySpinner.onItemSelectedListener = this

        Picasso.get().load(userProfileImageUrl).into(smallProfileImageView)
        userNameTextView.text = userName

        val publicOption = Visibility(R.drawable.ic_public_visibility, "Public")
        val friendsOption = Visibility(R.drawable.ic_friends_visibility, "Friends")
        val privateOption = Visibility(R.drawable.ic_private_visibility, "Private")

        val visibilities = mutableListOf(publicOption, friendsOption, privateOption)

        val adapter = PostVisibilityAdapter(requireContext(), visibilities)
        postVisibilitySpinner.adapter = adapter

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
                } else {
                    createPostButton.isEnabled = true
                    createPostButton.setTextColor(resources.getColor(R.color.dark_blue))
                }
            }
        })

        createPostButton.setOnClickListener {
            if (postData != null){
                if (postDataType == "image") {
                    var bitmap : Bitmap? = null
                    if (bitmapFromCamera){
                         bitmap = postData?.extras?.get("data") as Bitmap
                    }else{
                         bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, postData!!.data)
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
                                        if (!task.isSuccessful) {
                                           Utils.toastMessage(requireContext(), task.exception?.message.toString())
                                        }
                                    }
                                    dismiss()
                                }
                            }
                        }
                } else if (postDataType == "video") {
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
                                        if (!task.isSuccessful) {
                                            Utils.toastMessage(requireContext(), task.exception?.message.toString())
                                        }
                                    }
                                    dismiss()
                                }
                            }
                        }
                }
            }
            //Text post
            else{
                val post = createPost(null, null)
                progressDialog = Utils.showProgressDialog(requireContext(), "Please wait...")
                postViewModel.createPost(post).addOnCompleteListener { task ->
                    progressDialog?.dismiss()
                    if (!task.isSuccessful) {
                        Utils.toastMessage(requireContext(), task.exception?.message.toString())
                    }
                    dismiss()
                }
            }
        }

        addToPostButton.setOnClickListener {
            val addToPostBottomSheet = AddToPostBottomSheet(this)
            addToPostBottomSheet.show(activity?.supportFragmentManager!!, addToPostBottomSheet.tag)
        }

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        postVisibility = position
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    private fun createPost(postAttachmentUrl: String?, postAttachmentType: String?): Post {
        val content = postContentTextView.text.toString()

        return Post(
            publisherId = auth.currentUser?.uid.toString(),
            content = content,
            visibility = postVisibility,
            publisherName = userName,
            publisherImageUrl = userProfileImageUrl,
            attachmentUrl = postAttachmentUrl,
            attachmentType = postAttachmentType
        )
    }

    override fun setUserNameAndProfileImageUrl(name: String, url: String) {
        userName = name
        userProfileImageUrl = url
    }

    override fun onAttachmentAdded(data: Intent?, dataType: String, fromCamera:Boolean) {
        if (data != null){
            postData = data
            postDataType = dataType
            bitmapFromCamera = fromCamera
        }

    }
}