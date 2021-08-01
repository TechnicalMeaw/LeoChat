package com.example.letschat

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.letschat.LoadingScreen.loadingView.Companion.hideLoadingScreen
import com.example.letschat.LoadingScreen.loadingView.Companion.showLoadingScreen
import com.example.letschat.LoadingScreen.loadingView.Companion.showLoadingSuccess
import com.example.letschat.UploadOperations.updateProfilePicture.Companion.uploading
import com.example.letschat.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.loading_dialog.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class UserInfoActivity : AppCompatActivity() {

    var currentUser: User? = null
    var user: User? = null
//    var updating = false


    companion object {
        private var instance: UserInfoActivity? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)


        user = intent.getParcelableExtra<User>("USER_INFO_KEY")

        setSupportActionBar(toolbar_userInfo)
        supportActionBar?.title = user!!.username
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        userInfoDefaultProfileImageView.setImageResource(R.drawable.default_user_icon)

        updateUserInfo(user!!)
        refreshUser(user!!.uid)

        userInfoProfileCircleImageView.setOnClickListener {
            if (user!!.uid == FirebaseAuth.getInstance().uid)
                user = currentUser
            val intent = Intent(this, userInfoFullImageActivity::class.java)
            intent.putExtra("USER_IMAGE_KEY", user?.profileImageUrl)
            intent.putExtra("TITLE", user?.username)
            intent.putExtra("SENT_IMAGE", "false")
            startActivity(intent)
        }

        if (user?.uid == FirebaseAuth.getInstance().uid){
//            refreshUserObject()
            editProfileImageBtn.visibility = View.VISIBLE
            if (user?.profileImageUrl != "")
                deleteProfileImageBtn.visibility = View.VISIBLE
            currentUser = user
        }

        updateStatus("online")



        // Loading Dialog View
//        mDialogView = LayoutInflater.from(this).inflate(R.layout.loading_dialog, null)
//        alartDialog = showLoadingDialog()
//        Handler().postDelayed(hideLoadingDialog, 1)

    }




//    val hideLoadingDialog = Runnable { kotlin.run {
//        alartDialog?.dismiss()
//
//    } }
//
//    val pauseSuccessAnimation = Runnable { kotlin.run {
//        mDialogView?.loadingSpin?.pauseAnimation()
//        Handler().postDelayed(hideLoadingDialog, 250)
//    } }
//
//    val showSuccessText = Runnable { kotlin.run { mDialogView?.loadingSpin?.setMinAndMaxProgress(0.27f, 0.5f)
//        mDialogView?.loadingText?.text = "Success"
//        mDialogView?.loadingText?.setTextColor(getColor(R.color.colorPrimary))
//    } }
//
//    val showSuccessAnimation = Runnable { kotlin.run { mDialogView?.loadingSpin?.setMinAndMaxProgress(0.27f, 0.5f)
//        Handler().postDelayed(pauseSuccessAnimation, 3000)
//        Handler().postDelayed(showSuccessText, 2000)
//    } }
//
//
//
//    var alartDialog: AlertDialog? = null
//    var mDialogView : View? = null
//
//
//    fun showLoadingDialog(): AlertDialog? {
//
//        val mBuilder = AlertDialog.Builder(this)
//            .setView(mDialogView)
////            .setCancelable(false)
//
//        mDialogView?.loadingSpin?.setMinAndMaxProgress(0.0f, 0.1411f)
//
//
//        val mAlertDialog = mBuilder.show()
//        mAlertDialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);
//
//        return mAlertDialog
//    }




    private fun updateUserInfo(user: User) {
        supportActionBar?.title = user.username


        if (user.profileImageUrl != ""){
//            if (deleteProfileImageBtn.visibility == View.GONE){
//                showLoadingSuccess()
//                uploading = "null"
//            }
            if (user.uid == FirebaseAuth.getInstance().uid){
                deleteProfileImageBtn.visibility = View.VISIBLE
            }
            userInfoProfileCircleImageView.visibility = View.VISIBLE
            Picasso.get().load(user.profileImageUrl).into(userInfoProfileCircleImageView)
        }else{
            userInfoProfileCircleImageView.visibility = View.GONE
            deleteProfileImageBtn.visibility = View.GONE
        }
        userInfoUsername.text = user.username
        userInfoLastSeen.text = user.status
        userInfoPhoneNumber.text = (user.phoneNumber.reversed().substring(0,10) + " " + user.phoneNumber.reversed().substring(10)).reversed()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onPause() {
        super.onPause()
        updateStatus("Last Seen on ${getDate() + " at " + getTime()}")
//        if (currentUser != null)
//            refreshUserObject()
    }

    override fun onResume() {
        super.onResume()
        updateStatus("online")
//        if (updating){
//            updating = false
//            alartDialog?.show()
//            if (uploading != "uploading")
//                Handler().postDelayed(hideLoadingDialog, 1)
//
//        }
//        if (currentUser != null){
//            refreshUserObject()
//            updateProfileImage()
//        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        currentUser = null
//        CurrentUserEvents.deleteCache(this)
//    }


//    private fun updateProfileImage(){
//        if (currentUser != null){
//            if (currentUser!!.profileImageUrl != ""){
//                Picasso.get().load(currentUser!!.profileImageUrl).into(userInfoProfileCircleImageView)
//            }else{
//                userInfoProfileCircleImageView.visibility = View.GONE
//            }
//        }
//    }




    private fun updateStatus(string: String){
        val currentUserReference = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
        currentUserReference.child("status").setValue(string)
    }


    fun editProfileImage(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 2)
    }

    fun deleteProfileImage(view: View) {
//        alartDialog?.show()
        showLoadingScreen(this)

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentUser?.profileImageUrl.toString())
        val photoThumbRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentUser?.thumb.toString())


        photoRef.delete().addOnSuccessListener { // File deleted successfully
            Log.d("UserInfoActivity", "onSuccess: deleted file :: ProfileImage")
            ref.child("profileImageUrl").setValue("")
            ref.child("thumb").setValue("")
            userInfoProfileCircleImageView.visibility = View.GONE
            deleteProfileImageBtn.visibility = View.GONE
//            Handler().postDelayed(showSuccessAnimation, 1)
            showLoadingSuccess()
//            currentUser?.profileImageUrl = ""
//            currentUser?.thumb = ""
//            refreshUserObject()

        }.addOnFailureListener { // Uh-oh, an error occurred!
            Log.d("UserInfoActivity", "onFailure: did not delete file")
//            Handler().postDelayed(hideLoadingDialog, 1)
        }

        photoThumbRef.delete().addOnSuccessListener { // File deleted successfully
            Log.d("UserInfoActivity", "onSuccess: deleted file :: Thumbnail")
//            refreshUserObject()Handler().postDelayed(hideLoadingDialog, 1)
        }
    }




    private var bitmapDP : Bitmap? = null
    private var thumbBitmapDP : Bitmap?= null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            val imageUri = data.data
//            bitmapDP = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
//
//            bitmapDP = ImageResizer.reduceBitmapSize(bitmapDP, 630000)
//            thumbBitmapDP = ImageResizer.reduceBitmapSize(bitmapDP, 50000)
//
//            Log.d("UserInfoActivity", "Photo was selected")
//            UploadImageToFireStore()
            previewImage(imageUri)
        }
    }


    private fun previewImage(uri: Uri?) {

        val intent = Intent(this, userInfoFullImageActivity::class.java)
        intent.putExtra("USER_IMAGE_KEY", "NA")
        intent.putExtra("TITLE", "Edit Image")
        intent.putExtra("SENT_IMAGE", "DP")
        intent.putExtra("IMAGE", uri)
        intent.putExtra(NewMessagesActivity.USER_KEY, user)
        startActivity(intent)

//        updating = true
    }


//    private fun bitmapToByteArray(bitmap: Bitmap, quality: Int): ByteArray {
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
//
//        return stream.toByteArray()
//    }
//
//
//
//    private fun UploadImageToFireStore() {
//        if (bitmapDP == null){
//            Toast.makeText(this, "Photo Not Selected", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//
//        val filename = UUID.randomUUID().toString()
//        val ref = FirebaseStorage.getInstance().getReference("*/images/$filename")
//
//        ref.putBytes(bitmapToByteArray(bitmapDP!!, 85))
//            .addOnSuccessListener {
//                Log.d("UserInfoActivity", "Photo uploaded successfully")
//
//                ref.downloadUrl.addOnSuccessListener {
//                    Log.d("UserInfoActivity", "File location: $it")
//                    uploadThumbnail(it.toString(), filename)
//                }
//
//            }
//
//
//    }
//
//    fun uploadThumbnail(imageDownloadLink: String, filename: String){
//
//        val thumbRef = FirebaseStorage.getInstance().getReference("*/Thumbnails/$filename")
//
//        thumbRef.putBytes(bitmapToByteArray(thumbBitmapDP!!, 20)).addOnSuccessListener {
//            Log.d("UserInfoActivity", "Thumbnail uploaded successfully")
//
//            thumbRef.downloadUrl.addOnSuccessListener {
//                Log.d("UserInfoActivity", "File location: $it")
//                updateDatabase(imageDownloadLink, it.toString())
//            }
//        }
//    }
//
//    private fun updateDatabase(profileImageUrl: String, thumbnailUrl: String) {
//        val uid = FirebaseAuth.getInstance().uid
//        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
//
//        if (currentUser?.profileImageUrl.toString() != ""){
//            val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentUser?.profileImageUrl.toString())
//            val photoThumbRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentUser?.thumb.toString())
//
//            if (photoRef != null){
//                photoRef.delete().addOnSuccessListener { // File deleted successfully
//                    Log.d("UserInfoActivity", "onSuccess: deleted file")
//                    Picasso.get().load(thumbnailUrl).into(userInfoProfileCircleImageView)
//                }.addOnFailureListener { // Uh-oh, an error occurred!
//                    Log.d("UserInfoActivity", "onFailure: did not delete file")
//                }
//
//                photoThumbRef.delete()
//            }
//        }
//
//
//        ref.child("profileImageUrl").setValue(profileImageUrl).addOnSuccessListener {
////            currentUser?.profileImageUrl = profileImageUrl
////            refreshUserObject()
//            Picasso.get().load(profileImageUrl).into(userInfoProfileCircleImageView)
//
//        }
//        ref.child("thumb").setValue(thumbnailUrl).addOnSuccessListener {
////            currentUser?.thumb = thumbnailUrl
//            userInfoProfileCircleImageView.visibility = View.VISIBLE
//            deleteProfileImageBtn.visibility = View.VISIBLE
////            refreshUserObject()
//        }
//
//    }


//    private fun refreshUserObject(){
//        if(CurrentUserEvents.refreshCurrentUser(currentUser?.uid.toString()) != null) {
//            currentUser = CurrentUserEvents.refreshCurrentUser(currentUser?.uid.toString())
//            CurrentUserEvents.deleteCache(this)
//        }
//    }


    var isLoading = false

    private fun refreshUser(fromId : String){

        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val userObj = snapshot.getValue(User::class.java)

                if (userObj != null && userObj.uid == fromId){
                    if (userObj.uid == FirebaseAuth.getInstance().uid){

//                        if (userObj.profileImageUrl != "" && userObj.profileImageUrl != currentUser?.profileImageUrl){
//                            if (!alartDialog?.isShowing!!)
//                                alartDialog?.show()
//                            Handler().postDelayed(showSuccessAnimation, 1)
//                        }


                        if (userObj.status == currentUser?.status){
                            when(uploading){
                                "uploading" ->{
                                    if (userObj.profileImageUrl == currentUser?.profileImageUrl.toString() && !isLoading){
                                        showLoadingScreen(this@UserInfoActivity)
                                        isLoading = true
                                    }
                                }
                                "completed" ->{
                                    isLoading = false
                                    uploading = "null"
                                }
                            }
                        }


                        userObj.status = "online"
                        currentUser = userObj

//                        if (uploading == "uploading"){
//                            if (updating){
//                                alartDialog?.show()
//                                updating = false
//                            }
//                        }else if (uploading == "completed"){
//                            Handler().postDelayed(showSuccessAnimation, 1)
//                            uploading = "null"
//                        }
//                        else
//                            Handler().postDelayed(hideLoadingDialog, 1)




                    }
                    updateUserInfo(userObj)
                    user = userObj
                }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }


}