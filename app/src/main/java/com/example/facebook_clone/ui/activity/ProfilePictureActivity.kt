package com.example.facebook_clone.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.viewmodel.ProfilePictureActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile_picture.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val REQUEST_CODE = 159
private const val TAG = "ProfilePictureActivity"

class ProfilePictureActivity : AppCompatActivity() {
    private val profilePictureActivityViewModel by viewModel<ProfilePictureActivityViewModel>()
    private val auth: FirebaseAuth by inject()
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_picture)

        skipProfilePictureActivity.setOnClickListener {
//            val intent = Intent(this, ProfileActivity::class.java)
            val intent = Intent(this, OthersProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        takePhotoButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            val bitmap = data?.extras?.get("data") as Bitmap
            profileImagePreView.setImageBitmap(bitmap)
            progressDialog = Utils.showProgressDialog(this, "Please wait...")
            profilePictureActivityViewModel.uploadImageToCloudStorage(bitmap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                            uploadProfileImageToUserCollection(photoUrl.toString())
                            progressDialog?.dismiss()
                        }
                    }
                }
        }
    }


    private fun uploadProfileImageToUserCollection(photoUrl: String) {
        profilePictureActivityViewModel.uploadProfileImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    Utils.toastMessage(this, "Image updated successfully")
                    navigateToProfileActivity()
                } else {
                    Utils.toastMessage(this, task.exception?.message.toString())
                }
            }
    }


    private fun navigateToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }


}