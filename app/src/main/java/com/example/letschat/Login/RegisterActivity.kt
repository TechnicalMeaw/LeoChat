package com.example.letschat.Login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.letschat.LatestMessagesActivity
import com.example.letschat.LoadingScreen.loadingView
import com.example.letschat.LoadingScreen.loadingView.Companion.hideLoadingScreen
import com.example.letschat.LoadingScreen.loadingView.Companion.showLoadingSuccess
import com.example.letschat.Notifications.MyFirebaseMessagingService

import com.example.letschat.R
import com.example.letschat.models.ImageResizer
import com.example.letschat.models.User
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.util.*

class RegisterActivity : AppCompatActivity() {


    //val currentUser = FirebaseAuth.getInstance().currentUser
//    private var userName: String? = null
//    var storedVerificationId: String? = null
    var phoneNum: String? = null
    var notificationToken: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Get Phone Number
        phoneNum = intent.getStringExtra("PHONE_NUMBER")


        // Circle photo selector
        photoSelector.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }


        // Setting Up Notification Token
        MyFirebaseMessagingService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            MyFirebaseMessagingService.token = it.token
            notificationToken = it.token
        }


        // On Button Click
        confirmBtn.setOnClickListener{
            if (fullNameEditText.text!!.isEmpty()){
                Toast.makeText(this, "Please type your full name.", Toast.LENGTH_SHORT).show()
            }else{
                UploadImageToFireStore()
            }

            closeKeyboard(fullNameEditText)
        }
    }






    private var Imageuri : Uri? = null
    private var bitmap : Bitmap? = null
    private var thumbBitmap : Bitmap?= null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Imageuri = data.data
//            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Imageuri)
//
//            CircleImageView.setImageBitmap(bitmap)
//            bitmap = ImageResizer.reduceBitmapSize(bitmap, 630000)
//            thumbBitmap = ImageResizer.reduceBitmapSize(bitmap, 50000)
//
//            photoSelector.alpha = 0f
            showImage(Imageuri)
            Log.d("RegisterActivity", "Photo was selected")
        }else
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                val result = CropImage.getActivityResult(data)
                if (resultCode == RESULT_OK) {
                    val resultUri = result.uri

                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, resultUri)

                    CircleImageView.setImageURI(resultUri)

                    bitmap = ImageResizer.reduceBitmapSize(bitmap, 630000)
                    thumbBitmap = ImageResizer.reduceBitmapSize(bitmap, 50000)
                    photoSelector.alpha = 0f
                    Log.d("ChatLogActivity", "Photo cropped successfully")
                }
            }
    }


    private fun showImage(uri: Uri?) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setBorderCornerColor(getColor(R.color.colorAccent))
            .setCropMenuCropButtonIcon(R.drawable.done)
            .setAspectRatio(1,1)
            .setFixAspectRatio(true)
            .start(this);
    }


    private fun bitmapToByteArray(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        return stream.toByteArray()
    }




    private fun UploadImageToFireStore() {
        if (bitmap == null){
            Toast.makeText(this, "Please select a photo.", Toast.LENGTH_SHORT).show()
            return
        }

        loadingView.showLoadingScreen(this)

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("*/images/$filename")

        ref.putBytes(bitmapToByteArray(bitmap!!, 85))
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Photo uploaded successfully")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File location: $it")
                    uploadThumbnail(it.toString(), filename)
                }

            }


    }

    fun uploadThumbnail(imageDownloadLink: String, filename: String){

        val thumbRef = FirebaseStorage.getInstance().getReference("*/Thumbnails/$filename")

        thumbRef.putBytes(bitmapToByteArray(thumbBitmap!!, 20)).addOnSuccessListener {
            Log.d("RegisterActivity", "Thumbnail uploaded successfully")

            thumbRef.downloadUrl.addOnSuccessListener {
                Log.d("RegisterActivity", "File location: $it")
                SaveUserToFirebaseDatabase(imageDownloadLink, it.toString())
            }
        }
    }

    private fun SaveUserToFirebaseDatabase(profileImageUrl: String, thumbnailUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, fullNameEditText.text.toString(), profileImageUrl, "online", phoneNum!!, thumbnailUrl, notificationToken!!)

        ref.setValue(user)
            .addOnSuccessListener {
                showLoadingSuccess()
                Log.d("RegisterActivity", "User Data Has Been Stored to Database")
                intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

                val redirect = Runnable { kotlin.run { startActivity(intent)
                    finish() } }

                Handler().postDelayed(redirect, 3151)
            }
            .addOnFailureListener{
                hideLoadingScreen()
                Log.d("RegisterActivity", "Failed to store user data to database:: ${it.message}")
            }
    }


    private fun closeKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


//    fun validateEmail(emailForValidation: String): Boolean{
//
//        return Patterns.EMAIL_ADDRESS.matcher(emailForValidation).matches()
//    }

}

