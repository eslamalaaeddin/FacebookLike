package com.example.facebook_clone.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.provider.ImageUrlsProvider
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_viewer_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageViewerDialog() : DialogFragment(), ImageUrlsProvider {

    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var imageUrl: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        return inflater.inflate(R.layout.image_viewer_dialog, container, false)
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val photoView = view.findViewById<PhotoView>(R.id.photoView)

        scope.launch {
            val bitmap = Picasso.get()
                .load(imageUrl)
                .get()
            photoView.setImageBitmap(bitmap)
        }
        upButton.setOnClickListener {
            dismiss()
        }

    }

    override fun setImageUrl(url: String) {
        imageUrl = url
    }
}