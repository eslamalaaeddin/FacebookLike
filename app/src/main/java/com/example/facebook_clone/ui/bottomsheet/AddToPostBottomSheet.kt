package com.example.facebook_clone.ui.bottomsheet

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.AddToPostAdapter
import com.example.facebook_clone.helper.listener.PostAttachmentListener
import com.example.facebook_clone.model.Option
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_to_post_bottom_sheet.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val IMAGE_REQUEST_CODE = 987
private const val VIDEO_REQUEST_CODE = 654
private const val CAMERA_REQUEST_CODE = 321
private const val TAG = "AddToPostBottomSheet"
class AddToPostBottomSheet(private val postAttachmentListener: PostAttachmentListener) : BottomSheetDialogFragment() {
    private var progressDialog: ProgressDialog? = null
    private val postViewModel by viewModel<PostViewModel>()
    private lateinit var pAttachmentListener: PostAttachmentListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.add_to_post_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageOption = Option(R.drawable.ic_image, "Photo")
        val videoOption = Option(R.drawable.ic_videocam, "Video")
        val cameraOption = Option(R.drawable.ic_camera_alt_24, "Camera")

        val options = mutableListOf(imageOption, videoOption, cameraOption)

        val adapter = AddToPostAdapter(requireContext(), R.layout.add_to_post_item_layout, options)

        view.postAdditionsListView.adapter = adapter

        view.postAdditionsListView.setOnItemClickListener { parent, view, position, id ->
            pAttachmentListener = postAttachmentListener
            when (position) {
                0 -> onClickImage()
                1 -> onClickVideo()
                2 -> onClickCamera()
            }
        }
    }

    private fun onClickCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity?.packageManager!!) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    private fun onClickImage(){
        val imageIntent = Intent(Intent.ACTION_GET_CONTENT)
        imageIntent.type = "image/*"
        startActivityForResult(
            Intent.createChooser(imageIntent, "Choose an image"),
            IMAGE_REQUEST_CODE
        )
    }



    private fun onClickVideo(){
        val videoIntent = Intent(Intent.ACTION_GET_CONTENT)
        videoIntent.type = "video/*"
        startActivityForResult(
            Intent.createChooser(videoIntent, "Choose a video"),
            VIDEO_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //IMAGE
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK &&
                    data != null && data.data != null
        ) {
            pAttachmentListener.onAttachmentAdded(data, "image", false)

//            val pickedImage: Uri = data.data!!
//            // Let's read picked image path using content resolver
//            // Let's read picked image path using content resolver
//            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, pickedImage)
//            //use the bitmap as you like
//            //use the bitmap as you like
//            //imageView.setImageBitmap(bitmap)
//
//            Log.i(TAG, "DATAAA onAttachmentAdded: $data, ")
//            Log.i(TAG, "DATAAA onAttachmentAdded: $resultCode, ")
//            Log.i(TAG, "DATAAA onAttachmentAdded: ${data.data}, ")
//            Log.i(TAG, "DATAAA onAttachmentAdded: ${bitmap}, ")


        }
        //VIDEO
        else if (requestCode == VIDEO_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK ) {
            pAttachmentListener.onAttachmentAdded(data, "video", false)

        }
        //CAMERA
        else if (requestCode == CAMERA_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK ) {
            pAttachmentListener.onAttachmentAdded(data, "image", true)
        }
        //NO ATTACHMENT
        else{
            pAttachmentListener.onAttachmentAdded(null, "NULL", false)
        }
        dismiss()
    }
}