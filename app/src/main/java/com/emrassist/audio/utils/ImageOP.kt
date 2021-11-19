package com.emrassist.audio.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.io.ByteArrayOutputStream

object ImageOP {
    fun encodeTobase64(image: Bitmap): String {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 70, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun decodeBase64(input: String?): Bitmap {
        val decodedByte = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }

    /*helping method*/
    fun setImage(
        img: ImageView,
        path: Int,
        context: Context?,
        placeHolder: Int,
        errorHolder: Int
    ) {
        try {
            Glide.with(context!!)
                .load(path)
                .placeholder(placeHolder)
                .error(errorHolder)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        img.setImageDrawable(resource)
                        return false
                    }
                })
                .into(img)
        } catch (error: OutOfMemoryError) {
            error.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setImage(
        img: ImageView,
        path: String?,
        context: Context?,
        placeHolder: Int,
        errorHolder: Int
    ) {
        try {
            Glide.with(context!!)
                .load(path)
                .placeholder(placeHolder)
                .error(errorHolder)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        img.setImageResource(errorHolder)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        img.setImageDrawable(resource)
                        return true
                    }
                })
                .into(img)
        } catch (error: OutOfMemoryError) {
            error.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}