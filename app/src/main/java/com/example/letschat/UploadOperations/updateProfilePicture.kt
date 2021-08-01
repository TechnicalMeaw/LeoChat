package com.example.letschat.UploadOperations

import android.graphics.Bitmap
import android.util.Log
import com.example.letschat.LoadingScreen.loadingView.Companion.showLoadingSuccess
import com.example.letschat.models.CurrentUserEvents
import com.example.letschat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class updateProfilePicture {
    companion object{

        var currentUser: User? = null
        var uploading = "null"

        private fun refreshUserObject(){
            if(CurrentUserEvents.refreshCurrentUser(currentUser?.uid.toString()) != null) {
                currentUser = CurrentUserEvents.refreshCurrentUser(currentUser?.uid.toString())
            }
        }

        private fun bitmapToByteArray(bitmap: Bitmap, quality: Int): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

            return stream.toByteArray()
        }


        fun updateProfileImage(bitmapDP: Bitmap?, thumbBitmapDP: Bitmap?){
            refreshUserObject()
            UploadImageToFireStore(bitmapDP, thumbBitmapDP)
            uploading = "uploading"
        }

        private fun UploadImageToFireStore(bitmapDP: Bitmap?, thumbBitmapDP: Bitmap?) {
            if (bitmapDP == null){
//                Toast.makeText(this, "Photo Not Selected", Toast.LENGTH_SHORT).show()
                return
            }


            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("*/images/$filename")

            ref.putBytes(bitmapToByteArray(bitmapDP!!, 85))
                .addOnSuccessListener {
                    Log.d("UserInfoActivity", "Photo uploaded successfully")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("UserInfoActivity", "File location: $it")
                        uploadThumbnail(it.toString(), filename, thumbBitmapDP)
                    }

                }


        }

        fun uploadThumbnail(imageDownloadLink: String, filename: String, thumbBitmapDP: Bitmap?){

            val thumbRef = FirebaseStorage.getInstance().getReference("*/Thumbnails/$filename")

            thumbRef.putBytes(bitmapToByteArray(thumbBitmapDP!!, 20)).addOnSuccessListener {
                Log.d("UserInfoActivity", "Thumbnail uploaded successfully")

                thumbRef.downloadUrl.addOnSuccessListener {
                    Log.d("UserInfoActivity", "File location: $it")
                    updateDatabase(imageDownloadLink, it.toString())
                }
            }
        }

        private fun updateDatabase(profileImageUrl: String, thumbnailUrl: String) {
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

            if (currentUser?.profileImageUrl.toString() != ""){
                val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentUser?.profileImageUrl.toString())
                val photoThumbRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentUser?.thumb.toString())

                if (photoRef != null){
                    photoRef.delete().addOnSuccessListener { // File deleted successfully
                        Log.d("UserInfoActivity", "onSuccess: deleted file")
//                        Picasso.get().load(thumbnailUrl).into(userInfoProfileCircleImageView)
                    }.addOnFailureListener { // Uh-oh, an error occurred!
                        Log.d("UserInfoActivity", "onFailure: did not delete file")
                    }

                    photoThumbRef.delete()
                }
            }


            ref.child("profileImageUrl").setValue(profileImageUrl).addOnSuccessListener {
//            currentUser?.profileImageUrl = profileImageUrl
//            refreshUserObject()
//                Picasso.get().load(profileImageUrl).into(userInfoProfileCircleImageView)
                showLoadingSuccess()
                uploading = "completed"
            }
            ref.child("thumb").setValue(thumbnailUrl).addOnSuccessListener {
//            currentUser?.thumb = thumbnailUrl
//                userInfoProfileCircleImageView.visibility = View.VISIBLE
//                deleteProfileImageBtn.visibility = View.VISIBLE
//            refreshUserObject()
            }

        }

    }
}