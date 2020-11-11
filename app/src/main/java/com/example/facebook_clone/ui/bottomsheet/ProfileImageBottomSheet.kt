package com.example.facebook_clone.ui.bottomsheet

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_testing.*
import kotlinx.android.synthetic.main.profile_images_bottom_sheet_layout.*
import okhttp3.internal.Util
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

private const val IMAGE_REQUEST_CODE = 123
private const val TAG = "ProfileImageBottomSheet"
class ProfileImageBottomSheet(private val profileImageUrl:String) : BottomSheetDialogFragment(){

    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private var progressDialog: ProgressDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.profile_images_bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectProfilePicture.setOnClickListener {
            //1 get image from gallery
            val imageIntent = Intent(Intent.ACTION_GET_CONTENT)
            imageIntent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(imageIntent, "Choose an image"),
                IMAGE_REQUEST_CODE
            )
        }

        viewProfilePicture.setOnClickListener {
            val imageViewerDialog = ImageViewerDialog()
            imageViewerDialog.show(activity?.supportFragmentManager!!, "signature")
            imageViewerDialog.setImageUrl(profileImageUrl)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            progressDialog = Utils.showProgressDialog(requireContext(), "Please wait...")
            val imagePath = data.data!!
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imagePath)
                profileActivityViewModel.uploadProfileImageToCloudStorage(bitmap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                                progressDialog?.dismiss()
                                uploadProfileImageToUserCollection(photoUrl.toString())
                            }
                        }
                    }
                //imageView.setImageBitmap(bitmap)
            } catch (ex: IOException) {
                Log.e(TAG, "onActivityResult: ${ex.message}", ex)
            }
        }
    }

    private fun uploadProfileImageToUserCollection(photoUrl: String) {
        profileActivityViewModel.uploadProfileImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.toastMessage(requireContext(), "Image uploaded successfully")
                } else {
                    Utils.toastMessage(requireContext(), task.exception?.message.toString())
                }
            }
    }
}