package com.example.facebook_clone.ui.dialog

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.provider.NameImageProvider
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_creator_dialog.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PostCreatorDialog : DialogFragment(), AdapterView.OnItemSelectedListener, NameImageProvider {
    private val auth: FirebaseAuth by inject()
    private val postCreatorViewModel by viewModel<PostViewModel>()
    private var progressDialog: ProgressDialog? = null
    private var postVisibility = 0
    private lateinit var userName: String
    private lateinit var userProfileImageUrl: String
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


        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.visibility_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            postVisibilitySpinner.adapter = adapter
        }

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
            val post = createPost()
            progressDialog = Utils.showProgressDialog(requireContext(), "Please wait...")
            postCreatorViewModel.createPost(post).addOnCompleteListener { task ->
                progressDialog?.dismiss()
                if (task.isSuccessful) {
                    Utils.toastMessage(requireContext(), "Post is created successfully")
                } else {
                    Utils.toastMessage(requireContext(), task.exception?.message.toString())
                }
            }

        }

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        postVisibility = position
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    private fun createPost(): Post {
        val stringContent = postContentTextView.text.toString()
        return Post(
            publisherId = auth.currentUser?.uid.toString(),
            content = stringContent,
            visibility = postVisibility,
            publisherName = userName,
            publisherImageUrl = userProfileImageUrl,
        )
    }

    override fun setUserNameAndProfileImageUrl(name: String, url: String) {
        userName = name
        userProfileImageUrl = url
    }
}