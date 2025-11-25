package com.sanda.truckdoc.client.util.image

import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation

/**
 * Helper class to consolidate image loading and migrate from Picasso to Coil.
 * Use this instead of direct Picasso or Coil calls.
 */
object ImageLoaderHelper {

    /**
     * Load an image from a URL into an ImageView.
     */
    @JvmStatic
    @JvmOverloads
    fun load(
        imageView: ImageView,
        url: String?,
        @DrawableRes placeholder: Int? = null,
        @DrawableRes error: Int? = null,
        circleCrop: Boolean = false,
        cornerRadiusDp: Float? = null
    ) {
        imageView.load(url) {
            crossfade(true)
            
            placeholder?.let { placeholder(it) }
            error?.let { error(it) }
            
            if (circleCrop) {
                transformations(CircleCropTransformation())
            } else if (cornerRadiusDp != null && cornerRadiusDp > 0) {
                // Convert dp to px roughly or assume px if caller handles it. 
                // For simplicity in helper, let's assume caller might pass raw value or we handle density.
                // Coil takes pixels.
                val density = imageView.context.resources.displayMetrics.density
                transformations(RoundedCornersTransformation(cornerRadiusDp * density))
            }
        }
    }

    /**
     * Load an image from a resource ID.
     */
    @JvmStatic
    fun load(
        imageView: ImageView,
        @DrawableRes resourceId: Int
    ) {
        imageView.load(resourceId)
    }
}

