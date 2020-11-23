package com.example.facebook_clone.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.provider.MediaUrlProvider
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_viewer_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageViewerDialog() : DialogFragment(), MediaUrlProvider {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var imageUrl: String? = null
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
        //Image
        //When i check if it is null, it crashes
        scope.launch {
            val bitmap = Picasso.get()
                .load(imageUrl.toString())
                .get()
            if (bitmap != null) {
                //Only the original thread that created a view hierarchy can touch its views.
                CoroutineScope(Dispatchers.Main).launch {
                    photoView.setImageBitmap(bitmap)
                }
            }
        }

        upButton.setOnClickListener {
            dismiss()
        }

    }

    override fun setMediaUrl(url: String?) {
        if (url != null) {
            imageUrl = url
        }
    }
}