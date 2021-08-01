package com.example.letschat.backgroundWallpaper

import android.util.DisplayMetrics
import java.util.*

class backgroundImage {
    companion object{
        val displayMetrics = DisplayMetrics()

        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        var url = "https://picsum.photos/$width/$height/?random&t=" + Date().time.toString()

        fun getImageUrl(): String{
            return url
        }

        fun setImageUrl(Url: String){
            this.url = Url
        }
    }
}