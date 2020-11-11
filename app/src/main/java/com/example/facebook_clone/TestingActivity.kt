package com.example.facebook_clone

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_testing.*
import java.io.ByteArrayOutputStream
import java.io.IOException

private const val IMAGE_REQUEST_CODE = 159
private const val TAG = "TestingActivity"

class TestingActivity : AppCompatActivity() {
    private lateinit var path: Uri
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        chooseButton.setOnClickListener {
            val imageIntent = Intent(Intent.ACTION_GET_CONTENT)
            imageIntent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(imageIntent, "Choose an image"),
                IMAGE_REQUEST_CODE
            )
        }

        uploadButton.setOnClickListener {
            val storageReference =
                FirebaseStorage.getInstance().reference.child("DUMMY").child("Dummy1")

            val byteArrayOutputStream = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

            storageReference.putBytes(byteArrayOutputStream.toByteArray()).addOnSuccessListener {
                Toast.makeText(this, "${it.storage.downloadUrl}", Toast.LENGTH_LONG).show()
            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            path = data.data!!
            try {
                 bitmap = MediaStore.Images.Media.getBitmap(contentResolver, path)
                imageView.setImageBitmap(bitmap)
            } catch (ex: IOException) {
                Log.e(TAG, "onActivityResult: ${ex.message}", ex)
            }
        }
    }
}