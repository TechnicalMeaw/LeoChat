package com.example.letschat

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.letschat.UploadOperations.updateProfilePicture
import com.example.letschat.UploadOperations.uploadImageToDatabase
import com.example.letschat.models.*
import com.example.letschat.models.SaveImage.Companion.saveToGallery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_user_info_full_image.*
import java.io.IOException
import java.net.URL


class userInfoFullImageActivity : AppCompatActivity() {

    var image: Bitmap? = null
    var thumb: Bitmap? = null
    var isSent: String? = "false"
    var url: String? = null
    var uri: Uri? = null
    var flag = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info_full_image)

        setSupportActionBar(toolbar_userInfoFullImage)


        url = intent.getStringExtra("USER_IMAGE_KEY")
        val title = intent.getStringExtra("TITLE")
        isSent = intent.getStringExtra("SENT_IMAGE")
        uri = intent.getParcelableExtra("IMAGE")

        // Preview Image
        if (url == "NA"){

            BtnSend.visibility = View.VISIBLE

            image = MediaStore.Images.Media.getBitmap(contentResolver, uri)

            userInfoFullImageView.setImageBitmap(image)

            image = ImageResizer.reduceBitmapSize(image, 650000)
            if (isSent == "DP"){
                CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setBorderCornerColor(getColor(R.color.colorAccent))
                    .setCropMenuCropButtonIcon(R.drawable.done)
                    .setAspectRatio(1,1)
                    .setFixAspectRatio(true)
                    .start(this);
            }else
                flag = true


//            Log.d("ChatLogActivity", "Photo was selected")
//            val user = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = "image/*"
//            startActivityForResult(intent, 16)


        }else{
            BtnSend.visibility = View.GONE
            Picasso.get().load(url).into(userInfoFullImageView)

            val thread = Thread {
                try {
                    //Your code goes here
                    val url = URL(url)
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                } catch (e: Exception) {
                    e.printStackTrace()
//                    Toast.makeText(this, "Failed to download image in background", Toast.LENGTH_SHORT).show()
                }
            }

            thread.start()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        supportActionBar?.title = title


        updateStatus("online")


//        BtnSend.setOnClickListener(
//
//            uploadImageToDatabase.UploadImageToFireStore(bitmap, user)
//        )

    }

    fun sendImageToFirebase(view: View) {
        if (image != null){
            if (url == "NA" && isSent == "DP"){
                if (thumb != null){
                    updateProfilePicture.updateProfileImage(image, thumb)
                    finish()
                }
            }else {
                val user = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)
                uploadImageToDatabase.UploadImageToFireStore(image, user)
                finish()
            }

        }
    }


//    var Imageuri : Uri? = null
//    var bitmap : Bitmap? = null
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if(requestCode == 16 && resultCode == Activity.RESULT_OK && data != null){
//            Imageuri = data.data
//            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Imageuri)
//
//            userInfoFullImageView.setImageBitmap(bitmap)
//
//            bitmap = ImageResizer.reduceBitmapSize(bitmap, 650000)
//            Log.d("ChatLogActivity", "Photo was selected")
//
//
//        }else{
//            finish()
//        }
//    }


    private var globalMenuItem: Menu? = null
    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.full_image_view_menu, menu)

//        menu?.getItem(0)?.getIcon()?.setTint(getColor(R.color.colorAccent));

        globalMenuItem = menu
        if ( url == "NA"){
            menu?.findItem(R.id.saveImageBtn)?.isVisible = false
            menu?.findItem(R.id.cropImageBtn)?.isVisible = true
        }else{
            menu?.findItem(R.id.saveImageBtn)?.isVisible = true
            menu?.findItem(R.id.cropImageBtn)?.isVisible = false
        }
        invalidateOptionsMenu()

        return super.onCreateOptionsMenu(menu)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.saveImageBtn -> {

                try {
                    if (image != null) {
                        if (isSent == "true")
                            saveToGallery(
                                this,
                                image!!,
                                "Lets Chat/${toolbar_userInfoFullImage.title}/Sent/"
                            )
                        else
                            saveToGallery(
                                this,
                                image!!,
                                "Lets Chat/${toolbar_userInfoFullImage.title}"
                            )

                        Toast.makeText(this, "Downloading Image...", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    println(e)
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.cropImageBtn -> {
                // start cropping activity for pre-acquired image saved on the device
                if (uri != null) {
                    if (isSent == "DP"){
                        CropImage.activity(uri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setBorderCornerColor(getColor(R.color.colorAccent))
                            .setCropMenuCropButtonIcon(R.drawable.done)
                            .setAspectRatio(1,1)
                            .setFixAspectRatio(true)
                            .start(this);
                    }else{
                        CropImage.activity(uri)
                            .setBorderCornerColor(getColor(R.color.colorAccent))
                            .setCropMenuCropButtonIcon(R.drawable.done)
                            .start(this);
                    }
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (data == null){
                if (!flag)
                    finish()
            }
            else
                flag = true
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                userInfoFullImageView.setImageURI(resultUri)
                image = MediaStore.Images.Media.getBitmap(contentResolver, resultUri)

                image = ImageResizer.reduceBitmapSize(image, 650000)

                thumb = ImageResizer.reduceBitmapSize(image, 50000)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                finish()
            }
        }
    }



    override fun onPause() {
        updateStatus("Last Seen on ${getDate() + " at " + getTime()}")
        super.onPause()
    }

    override fun onResume() {
        updateStatus("online")
        super.onResume()
    }
    override fun onDestroy() {
        updateStatus("Last Seen on ${getDate() + " at " + getTime()}")
        super.onDestroy()
        CurrentUserEvents.deleteCache(this)
    }

    private fun updateStatus(string: String){
        val currentUserReference = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
        currentUserReference.child("status").setValue(string)
    }




}



