package com.example.letschat.models

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class SaveImage {

    companion object{

        fun saveToGallery(context: Context, bitmap: Bitmap, albumName: String) {
            val filename = "${System.currentTimeMillis()}.jpg"
            val write: (OutputStream) -> Boolean = {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/$albumName"
                    )
                }

                context.contentResolver.let {
                    it.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                        it.openOutputStream(uri)?.let(write)
                        Log.d("URI", uri.toString())
                    }
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + albumName
                val file = File(imagesDir)
                if (!file.exists()) {
                    file.mkdir()
                }
                val image = File(imagesDir, filename)
                write(FileOutputStream(image))
                Log.d("URI", imagesDir.toString())
            }
        }
    }
}