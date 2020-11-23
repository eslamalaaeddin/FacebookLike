package com.example.facebook_clone.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.viewmodel.ProfilePictureActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile_picture.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val REQUEST_CODE = 159
private const val IMAGE_REQUEST_CODE = 123
private const val TAG = "ProfilePictureActivity"

class ProfilePictureActivity : AppCompatActivity() {
    private val profilePictureActivityViewModel by viewModel<ProfilePictureActivityViewModel>()
    private val auth: FirebaseAuth by inject()
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_picture)

        val gender = intent.getStringExtra("gender").toString()

        if (gender == "male"){
            val defaultProfileImageBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.hairstyle
            )
            profileImagePreView.setImageBitmap(defaultProfileImageBitmap)
            handleProfileImage(defaultProfileImageBitmap)
        }

        else{
            val defaultProfileImageBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.niqab
            )
            profileImagePreView.setImageBitmap(defaultProfileImageBitmap)
            handleProfileImage(defaultProfileImageBitmap)

        }

        val defaultCoverImageBitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.cover
        )

        handleCoverImage(defaultCoverImageBitmap)


        skipProfilePictureActivity.setOnClickListener {
            navigateToProfileActivity()
        }

        takePhotoButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_CODE)
            }
        }

        chooseProfilePictureFromGalleryButton.setOnClickListener {
            val imageIntent = Intent(Intent.ACTION_GET_CONTENT)
            imageIntent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(imageIntent, "Choose an image"),
                IMAGE_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //image from camera
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            handleProfileImage(bitmap)
        }
        //image from gallery
        else if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK){
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data?.data!!)
            handleProfileImage(bitmap)
        }
    }

    private fun handleProfileImage(bitmap: Bitmap){
        //profileImagePreView.setImageBitmap(bitmap)
        progressDialog = Utils.showProgressDialog(this, "Please wait...")
        profilePictureActivityViewModel.uploadProfileImageToCloudStorage(bitmap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                        uploadProfileImageToUserCollection(photoUrl.toString())
                        progressDialog?.dismiss()
                    }
                }
            }
    }

    private fun handleCoverImage(bitmap: Bitmap){
       // profileImagePreView.setImageBitmap(bitmap)
        //progressDialog = Utils.showProgressDialog(this, "Please wait...")
        profilePictureActivityViewModel.uploadCoverImageToCloudStorage(bitmap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                        uploadCoverImageToUserCollection(photoUrl.toString())
                      //  progressDialog?.dismiss()
                    }
                }
            }
    }

    private fun uploadProfileImageToUserCollection(photoUrl: String) {
        profilePictureActivityViewModel.uploadProfileImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                   // Utils.toastMessage(this, "Image updated successfully")
                    //navigateToProfileActivity()
                } else {
                    Utils.toastMessage(this, task.exception?.message.toString())
                }
            }
    }

    private fun uploadCoverImageToUserCollection(photoUrl: String) {
        profilePictureActivityViewModel.uploadCoverImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                  //  Utils.toastMessage(this, "Image uploaded successfully")
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