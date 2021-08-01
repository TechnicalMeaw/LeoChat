package com.example.letschat.models

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.letschat.chatLogActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.logging.Handler

public class CurrentUserEvents {



    companion object{
        var currentUser: User? = null
        var flag: Boolean = false

        fun refreshCurrentUser(uid : String): User? {

            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild("profileImageUrl")) {
                        currentUser = snapshot.getValue(User::class.java)
                        flag = true
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    flag = false
                }
            })


            return if (flag) {
                currentUser
            }else
                null

        }




        fun deleteCache(context: Context) {
            try {
                val dir: File = context.cacheDir
                deleteDir(dir)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun deleteDir(dir: File?): Boolean {
            return if (dir != null && dir.isDirectory) {
                val children: Array<String> = dir.list()
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
                dir.delete()
            } else if (dir != null && dir.isFile()) {
                dir.delete()
            } else {
                false
            }
        }

    }




}