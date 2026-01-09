package com.sanda.truckdoc.client.util

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageLoader @Inject constructor(
    private val context: Context
) {
    private val loader = ImageLoader(context)

    fun loadImage(url: String, imageView: ImageView) {
        imageView.load(url) {
            crossfade(true)
            listener(
                onStart = { /* Handle start */ },
                onSuccess = { _, _ -> /* Handle success */ },
                onError = { _, _ -> /* Handle error */ }
            )
        }
    }

    fun loadImageWithRequest(url: String, imageView: ImageView) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .target(imageView)
            .build()
        loader.enqueue(request)
    }

    fun loadImageWithPlaceholder(url: String, imageView: ImageView, placeholderResId: Int) {
        imageView.load(url) {
            placeholder(placeholderResId)
            error(placeholderResId)
            crossfade(true)
        }
    }
} 