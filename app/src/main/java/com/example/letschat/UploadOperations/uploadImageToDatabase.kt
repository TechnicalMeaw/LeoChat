package com.example.letschat.UploadOperations

import android.graphics.Bitmap
import android.util.Log
import com.example.letschat.NewMessagesActivity
import com.example.letschat.UploadOperations.uploadMessageToDatabase.Companion.sendMessageToFirebaseDatabase
import com.example.letschat.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.toolbar_conversation.*
import java.io.ByteArrayOutputStream
import java.util.*

class uploadImageToDatabase{
    companion object{

        fun UploadImageToFireStore(bitmap: Bitmap?, user: User?) {
            if (bitmap == null) return

            getFromUser(user?.uid)

            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("*/images/$filename")

            ref.putBytes(bitmapToByteArray(bitmap!!))
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Photo uploaded successfully")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("ChatLogActivity", "File location: $it")

                        if (fromUser != null)
                            sendMessageToFirebaseDatabase("IMG>$it", fromUser!!)
                        else
                            sendMessageToFirebaseDatabase("IMG>$it", user!!)
                    }

                }

        }

        private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)

            return stream.toByteArray()
        }


        var fromUser: User? = null
        fun getFromUser(userId: String?) {
            val ref = FirebaseDatabase.getInstance().getReference("/users/$userId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild("profileImageUrl"))
                        fromUser = snapshot.getValue(User::class.java)
                    else
                        Log.e("UploadImageTask", "User Not Found :: Unable To Fetch User From Database")
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

}

