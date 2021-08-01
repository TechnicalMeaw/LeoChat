package com.example.letschat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.letschat.UploadOperations.uploadImageToDatabase
import com.example.letschat.UploadOperations.uploadMessageToDatabase.Companion.sendMessageToFirebaseDatabase
import com.example.letschat.backgroundWallpaper.backgroundImage
import com.example.letschat.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.toolbar_conversation.*
import java.util.*


const val TOPIC = "/topics/messaging"

var activityVisible: Boolean = false

class chatLogActivity : AppCompatActivity() {

    init {
        instance = this
    }

    companion object {
        private var instance: chatLogActivity? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private var flag = false

    private val TAG = "chatLogActivity"

    private var currentUser: User? = null
    var user: User? = null

    var backgroundImageUrl: String? = null

    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAAUcDpD-k:APA91bH5-athDPNJ-46f4wTC9z04dh0pmumwoLLsuvewZirKmXQ8j-5VadPxaZr_YTXW8VQQrx0dIZcp8uCUubO6NAVEZnzxcon6hA7Ujn8udvcvZQvi1LMPYyuOLRmNBE2A697Sk4eg"
    private val contentType = "application/json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        // Setting Up Keyboard
//        messageEditText.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        messageEditText.isSingleLine = false

        // Getting the sender and the receiver
        user = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)
        currentUser = intent.getParcelableExtra<User>(NewMessagesActivity.CURRENT_USER)

        if (currentUser == null){
            getCurrentUser()
        }

        if (user == null){
            val userId = intent.getStringExtra("UserId")
            getFromUser(userId)
        }

        // Setting up the toolBar
        val toolbar: Toolbar = findViewById(R.id.toolbar_chats)
        setSupportActionBar(toolbar)
        chat_log_back_btn.setOnClickListener{
            finish()
        }

        // Opening User Info Activity
        chatUserInfoBtn.setOnClickListener{
            val intent = Intent(this, UserInfoActivity::class.java)
            intent.putExtra("USER_INFO_KEY", user)
            startActivity(intent)
        }
        // User Name
        action_bar_Username.text = user?.username
        // Profile Image
        action_bar_profileDefaultImage.setImageDrawable(getDrawable(R.drawable.default_user_icon))
        Picasso.get().load(user?.thumb).into(action_bar_profileImage)

        // Updating Last Seen Status
        updateStatus("online")

        // Last Seen
        UpdateLastSeen(user!!)

        // Setting up Adapter
        chatLogView.adapter = adapter

        // Updating The Chats
        updateChatLogUI(currentUser!!, user!!)

        // Placing The Background Image
        loadBackground()

        // Update Is Typing

        val mHandler = Handler()
        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateTypingStatus(true)
                updateStatus("typing...")
                mHandler.removeCallbacksAndMessages(null)
                mHandler.postDelayed(stoppedTyping, 400)
            }

            val stoppedTyping : Runnable = object : Runnable {
                // do my stuff
                override fun run() {
                    updateTypingStatus(false)
                    updateStatus("online")
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int) {
            }
        })





        // Open Full Image
        adapter.setOnItemClickListener { item, view ->
            var url: String? = null
            val type = item.toString()
            var isSent: Boolean = false
//            if (view.id == R.id.chatUserSendImageView)
            if (type.length >= 50){
                if (type.substring(0, 45) == "com.example.letschat.models.ChatImageUserItem"){
                    val chatItem = item as ChatImageUserItem
                    url = chatItem.url
                    isSent = true
                }else if (type.substring(0, 55) == "com.example.letschat.models.ChatImageToUserReceivedItem"){
                    val chatItem = item as ChatImageToUserReceivedItem
                    url = chatItem.url
                    isSent = false
                }
                Log.d("ChatLogActivity", "MessageItem -> $url")
            }


            if (url != null){
                val intent = Intent(this, userInfoFullImageActivity::class.java)
                intent.putExtra("USER_IMAGE_KEY", url)
                intent.putExtra("TITLE", user?.username)
                intent.putExtra("SENT_IMAGE", isSent.toString())
                intent.putExtra("IMAGE", Imageuri)
                startActivity(intent)
            }
        }





        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        // Check if toUser is typing...
//        checkIsTyping()

    }

    private fun getFromUser(userId: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("/users/$userId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("profileImageUrl"))
                    user = snapshot.getValue(User::class.java)
                else
                    Log.e(TAG, "User Not Found :: Unable To Fetch User From Database")
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getCurrentUser() {
        val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("profileImageUrl"))
                    currentUser = snapshot.getValue(User::class.java)
                else
                    Log.e(TAG, "User Not Found :: Unable To Fetch User From Database")
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun UpdateLastSeen(user: User){
        placeLastSeen(user.status)

        val ref = FirebaseDatabase.getInstance().getReference("/users/${user.uid}/").child("status")
        val currentRef = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}/").child(
            "status"
        )
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userLastSeen: String? = snapshot.value as String?
                if (userLastSeen != null) {
                    placeLastSeen(userLastSeen)
                    Log.d("ChatLogActivity", "Live Last Seen Updated Successfully: $userLastSeen")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        // If User Goes Offline
        currentRef.onDisconnect().setValue("offline")
    }




    // Loading The Background
    private fun loadBackground(){

        backgroundImageUrl = backgroundImage.getImageUrl()
//        Log.d("ChatLogActivity", "Image Size width: ${width}, height: ${height}")
        Picasso.get().load(backgroundImageUrl).into(
            chatLog_ImageView
        )
    }

    // Send Messages To Firebase Database
    fun sendBtn(view: View) {
        if (messageEditText.text.toString() != ""){
            // get text from input field
            val text = messageEditText.text.toString()
            messageEditText.text = null

            val user = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)

            // user: to whom the message will be sent
            sendMessageToFirebaseDatabase(text, user!!)

        }
    }



//    private fun sendNotificationToDevice(text: String, toUser: User, messageId: String){
//
//        val notificationTitle = currentUser?.username.toString()
//        val recipientToken = toUser.notificationToken.toString()
//        val thumbUrl = currentUser?.thumb.toString()
//
//        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(notificationTitle)) {
//            val topic = "/topics/Enter_your_topic_name" //topic has to match what the receiver subscribed to
//
//            val notification = JSONObject()
//            val notificationBody = JSONObject()
//            val userBody = JSONObject()
//
//            try {
//
//                userBody.put("uid", currentUser)
//
//
//                notificationBody.put("title", notificationTitle)
//                notificationBody.put("message", text)   //Enter your notification message
//                notificationBody.put("dpUrl", thumbUrl)
//                notificationBody.put("fromId", FirebaseAuth.getInstance().uid)
////                notificationBody.put("toId", toUser.uid)
//                notificationBody.put("msgId", messageId)
//
//                notification.put("to", recipientToken)
//                notification.put("data", notificationBody)
//                Log.e(TAG, "try")
//            } catch (e: JSONException) {
//                Log.e(TAG, "onCreate: " + e.message)
//            }
//
//            sendNotification(notification)
//        }
//    }


//    private fun sendNotification(notification: JSONObject) {
//        // Instantiate the RequestQueue.
//        val requestQueue = Volley.newRequestQueue(this)
//
//        Log.e("TAG", "sendNotification")
//        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
//            Response.Listener<JSONObject> { response ->
//                Log.i("TAG", "onResponse: $response")
//            },
//            Response.ErrorListener {
//                Toast.makeText(this@chatLogActivity, "Request error", Toast.LENGTH_LONG).show()
//                Log.i("TAG", "onErrorResponse: Didn't work")
//            }) {
//
//            override fun getHeaders(): Map<String, String> {
//                val params = HashMap<String, String>()
//                params["Authorization"] = serverKey
//                params["Content-Type"] = contentType
//                return params
//            }
//        }
//
//        requestQueue.add(jsonObjectRequest)
//    }


    val chatMessageMap = HashMap<String, ChatMessage>()

    fun refreshChatMessages(currentUser: User, user: User){
        adapter.clear()
        date = ""
        chatMessageMap.values.sortedBy{ it.timeInMillis }.forEach{

            placeMessages(it, currentUser, user)

            // Read the message
//            val latestMessageReference = FirebaseDatabase.getInstance()
//                .getReference("/latest-messages/${currentUser.uid}/${user.uid}")
//
//            latestMessageReference.child("read").setValue(true)
        }
    }


    private fun placeMessages(it: ChatMessage, currentUser: User, user: User){
        if(it.timeStamp.length > 16)
            placeDateStamp(it.timeStamp.substring(0, 17))
        Log.d("ChatLogActivity", it.text)

        if (FirebaseAuth.getInstance().uid == it.fromId) {

            // Checking For Image Item
            if (it.text.length > 4) {
                if (it.text.substring(0, 4) == "IMG>") {
                    adapter.add(
                        ChatImageUserItem(
                            it.text.substring(4),
                            it.timeStamp,
                            it.read
                        )
                    )
                } else {
                    adapter.add(
                        ChatUserItem(
                            it.text,
                            currentUser,
                            it.timeStamp,
                            it.read
                        )
                    )
                }
            } else {
                adapter.add(
                    ChatUserItem(
                        it.text,
                        currentUser,
                        it.timeStamp,
                        it.read
                    )
                )
            }

        } else {

//                if (currentUser.uid != it.fromId){
//                    val msgRef = FirebaseDatabase.getInstance().getReference("/messages/${it.fromId}/${it.toId}/${it.id}")
//                    if (activityVisible)
//                        msgRef.child("read").setValue("read")
//                }

            if (it.text.length > 4) {
                if (it.text.substring(0, 4).equals("IMG>")) {
                    adapter.add(
                        ChatImageToUserReceivedItem(
                            it.text.substring(4),
                            it.timeStamp
                        )
                    )
                } else {
                    adapter.add(
                        ChatToItem(
                            user,
                            it.text,
                            it.timeStamp
                        )
                    )
                }
            } else
                adapter.add(ChatToItem(user, it.text, it.timeStamp))
        }
        // scroll to the bottom of the RecyclerView
        chatLogView.scrollToPosition(adapter.itemCount - 1)

    }


    // Load Chat Messages
    // And Images
    private fun updateChatLogUI(currentUser: User, user: User){
        var addedChildCount = 0

        val ref = FirebaseDatabase.getInstance().getReference("/messages/${currentUser.uid}/${user.uid}")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                addedChildCount += 1
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                Log.d(TAG, "CALLED ON CHILD ADDED")
                if (chatMessage != null){
                    chatMessageMap[snapshot.key!!] = chatMessage
                    placeMessages(chatMessage, currentUser, user)

                    if (activityVisible && chatMessage.fromId.toString() == user.uid.toString() && chatMessage.read.toString() != "read"){
                        val latestMessageReference = FirebaseDatabase.getInstance()
                            .getReference("/latest-messages/${currentUser.uid}/${user.uid}")

                        val latestMessageToReference = FirebaseDatabase.getInstance()
                            .getReference("/latest-messages/${chatMessage.fromId}/${chatMessage.toId}")

                        latestMessageReference.child("read").setValue(true)
                        latestMessageToReference.child("read").setValue(true)
                    }


                    if (FirebaseAuth.getInstance().uid.toString() != chatMessage.fromId.toString()){
                        if (!activityVisible){
                            val msgRef = FirebaseDatabase.getInstance()
                                .getReference("/messages/${user.uid}/${FirebaseAuth.getInstance().uid}/${chatMessage.id}")

                            val msgToRef = FirebaseDatabase.getInstance()
                                .getReference("/messages/${FirebaseAuth.getInstance().uid}/${user.uid}/${snapshot.key}")

                            if (chatMessage.read == "sent"){
                                msgRef.child("read").setValue("unread").addOnSuccessListener {
                                    msgToRef.child("read").setValue("unread")
                                }
                            }

                        }

                        if (activityVisible){
//                            val latestMessageReference = FirebaseDatabase.getInstance()
//                                .getReference("/latest-messages/${chatMessage.toId}/${chatMessage.fromId}")
//
//                            val latestMessageToReference = FirebaseDatabase.getInstance()
//                                .getReference("/latest-messages/${chatMessage.fromId}/${chatMessage.toId}")
//
                            val msgRef = FirebaseDatabase.getInstance()
                                .getReference("/messages/${user.uid}/${FirebaseAuth.getInstance().uid}/${chatMessage.id}")

                            val msgToRef = FirebaseDatabase.getInstance()
                                .getReference("/messages/${FirebaseAuth.getInstance().uid}/${user.uid}/${snapshot.key.toString()}")
//
//                            if (chatMessage.read.toString() != "read"){
//                                latestMessageReference.child("read").setValue(true)
//                                latestMessageToReference.child("read").setValue(true)
//                            }
                            if (chatMessage.read != "read"){
                                msgRef.child("read").setValue("read").addOnSuccessListener {
                                    msgToRef.child("read").setValue("read")
                                }
                            }
                        }


                    }


//                    if (currentUser.uid != chatMessage.fromId) {
//                        val msgRef = FirebaseDatabase.getInstance()
//                            .getReference("/messages/${chatMessage.fromId}/${chatMessage.toId}/${chatMessage.id}")
//
//
//
//                        if (!activityVisible)
//                            msgRef.child("read").setValue("unread")
////                        else {
////                            msgRef.child("read").setValue("read")
////                        }
//
//                    }
//                    else {
//                        // Checking For Image Item
//                        if (chatMessage.text.length > 4) {
//
//                            if (chatMessage.text.substring(0, 4) == "IMG>") {
//                                SaveImage.saveToGallery(
//                                    this,
//                                    image!!,
//                                    "Lets Chat/${toolbar_userInfoFullImage.title}"
//                                )
//                            }
//                        }
//                    }

//                    else{
//                        if (action_bar_timeStamp.text != "online" && action_bar_timeStamp.text != "typing..." && chatMessage.read == "sent")
//                            sendNotificationToDevice(chatMessage.text, user, chatMessage.id)
//
//                    }

//                    if (user.uid == chatMessage.fromId){
//                        // Read the message
//                        val latestMessageReference = FirebaseDatabase.getInstance()
//                            .getReference("/latest-messages/${currentUser.uid}/${user.uid}")
//                        if (!activityVisible)
//                            latestMessageReference.child("read").setValue(false)
//                        else
//                            latestMessageReference.child("read").setValue(true)
//                    }

//                    if (addedChildCount >= chatMessageMap.size){
//                        refreshChatMessages(currentUser, user)
//                        Log.d(TAG, "CALLED REFRESH CHAT MESSAGE")
//                    }
                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                updateChatLogUI(currentUser, user)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null){
                    if (chatMessageMap[snapshot.key!!]?.read != "read"){
                        chatMessageMap[snapshot.key!!] = chatMessage
                        refreshChatMessages(currentUser, user)
                    }else{
                        if (chatMessage.read == "unread" && chatMessage.fromId == FirebaseAuth.getInstance().uid){
                            val msgRef = FirebaseDatabase.getInstance()
                                .getReference("/messages/${FirebaseAuth.getInstance().uid}/${user.uid}/${snapshot.key.toString()}")
                            msgRef.child("read").setValue("read")
                        }

                    }
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


    // Saving Image To Gallery
    fun saveImageToLocalStorage(url: String){

//        val image: Bitmap =
//        saveToGallery(this, image, "Meme Share")
    }







    // Placing The Last Seen In ToolBar
    fun placeLastSeen(status: String){

        action_bar_timeStamp.visibility = View.VISIBLE
        if( status == "online" || status == "offline" || status == "typing..."){
            action_bar_timeStamp.text = status
//            val latestMessageReference = FirebaseDatabase.getInstance()
//                .getReference("/latest-messages/${user?.uid}/${currentUser?.uid}")
            if (status == "typing..."){
                action_bar_timeStamp.setTextColor(getColor(R.color.typing_color))
                typingStatus.visibility = View.VISIBLE
                chatLogView.scrollToPosition(adapter.itemCount - 1)
////                    latestMessageReference.child("typing").setValue(true)
//
            }else{
                action_bar_timeStamp.setTextColor(getColor(R.color.white))
                typingStatus.visibility = View.GONE
////                latestMessageReference.child("typing").setValue(false)
            }
        }else{
            action_bar_timeStamp.setTextColor(getColor(R.color.white))
            typingStatus.visibility = View.GONE
            if( status.substring(13, 30) == getDate()){
                action_bar_timeStamp.text = "Last Seen" + status.substring(30)
            }else if (status.substring(26, 30) == getDate().substring(13)){
                action_bar_timeStamp.text = status.substring(0, 24) + status.substring(30)
            }else{
                action_bar_timeStamp.text = status
            }
        }
//        if (chatMessage.timeStamp.substring(0,17) == getDate()){
//            action_bar_timeStamp.text = "Last seen at " + chatMessage.timeStamp.substring(18)
//        }else{
//            action_bar_timeStamp.text = "Last seen at " + chatMessage.timeStamp.substring(0,11)
//        }
    }


    private var date: String = ""
    fun placeDateStamp(Date: String){

        if (date != Date)
            flag = false

        if (!flag){
            if (Date == getDate()){
                adapter.add(chatDateItem("Today"))
            }
            else{
                adapter.add(chatDateItem(Date))
            }
            date = Date
            flag = true
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_log_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.BtnChooseChatLogWallpaper -> {
//                loadBackground()
                val intent = Intent(this, BackgroundWallpaperActivity::class.java)
                startActivity(intent)
            }

            R.id.btnInfo -> {
                val intent = Intent(this, UserInfoActivity::class.java)
                intent.putExtra("USER_INFO_KEY", user)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // When Image Send Button is pressed
    fun sendImage(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)

        uploadImageToDatabase.getFromUser(user?.uid)
    }

    var Imageuri : Uri? = null
    var bitmap : Bitmap? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 1 && resultCode == Activity.RESULT_OK && data != null){
            Imageuri = data.data
//            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Imageuri)
//
//            bitmap = ImageResizer.reduceBitmapSize(bitmap, 650000)
//            Log.d("ChatLogActivity", "Photo was selected")
//            val user = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)


            previewImage(Imageuri)
//            UploadImageToFireStore(bitmap, user)
        }
    }


    private fun previewImage(uri: Uri?) {

        val intent = Intent(this, userInfoFullImageActivity::class.java)
        intent.putExtra("USER_IMAGE_KEY", "NA")
        intent.putExtra("TITLE", user?.username)
        intent.putExtra("SENT_IMAGE", "true")
        intent.putExtra("IMAGE", uri)
        intent.putExtra(NewMessagesActivity.USER_KEY, user)
        startActivity(intent)
    }


    // Updating Status
    override fun onPause() {
        updateStatus("Last Seen on ${getDate() + " at " + getTime()}")
        super.onPause()
        activityVisible = false
    }

    override fun onResume() {
        super.onResume()
        updateStatus("online")
        activityVisible = true

        if (backgroundImage.getImageUrl() != backgroundImageUrl)
            loadBackground()
    }

    private fun updateStatus(string: String){
        val currentUserReference = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
        currentUserReference.child("status").setValue(string)
    }

    private fun updateTypingStatus(isTyping: Boolean){
        val latestMessageReference = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/${user?.uid}/${currentUser?.uid}")
        latestMessageReference.child("typing").setValue(isTyping)

    }


//    private fun checkIsTyping(){
//        val latestMessageReference = FirebaseDatabase.getInstance()
//            .getReference("/latest-messages/${currentUser?.uid}/${user?.uid}").child("typing")
//        latestMessageReference.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()){
//                    val isTyping: Boolean = snapshot.value as Boolean
//                    if (isTyping) {
//                        placeLastSeen("typing...")
//                        Log.d("ChatLogActivity", "Live Last Seen Updated Successfully: typing... : $isTyping")
//                    }else{
//                        placeLastSeen(user!!.status)
//                    }
//                }
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                println(error)
//            }
//        })
//    }


//    private fun UploadImageToFireStore(bitmap: Bitmap?, user: User?) {
//        if (bitmap == null) return
//
//        val filename = UUID.randomUUID().toString()
//        val ref = FirebaseStorage.getInstance().getReference("*/images/$filename")
//
//        ref.putBytes(bitmapToByteArray(bitmap!!))
//            .addOnSuccessListener {
//                Log.d("RegisterActivity", "Photo uploaded successfully")
//
//                ref.downloadUrl.addOnSuccessListener {
//                    Log.d("ChatLogActivity", "File location: $it")
////                    val user = intent.getParcelableExtra<User>(NewMessagesActivity.USER_KEY)
//                    sendMessageToFirebaseDatabase("IMG>$it", user!!)
//                }
//
//            }
//
//    }

//    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
//
//        return stream.toByteArray()
//    }





//    private fun sendMessageToFirebaseDatabase(text: String, user: User){
//        // saves the message to firebase database
//        val fromId = FirebaseAuth.getInstance().uid
//        val toId = user.uid
//
//        // get reference to the Messages node
//        val reference = FirebaseDatabase.getInstance().getReference("/messages/$fromId/$toId").push()
//
//        val toReference = FirebaseDatabase.getInstance().getReference("/messages/$toId/$fromId").push()
//
//        val latestMessageReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
//
//        val latestMessageToReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
//
//        val chatMessage = ChatMessage(
//            reference.key!!,
//            text,
//            fromId!!,
//            toId,
//            getDate() + " " + getTime(),
//            Calendar.getInstance().timeInMillis / 1000,
//            "sent"
//        )
//
//        val chatToMessage = ChatMessage(
//            reference.key!!,
//            text,
//            fromId,
//            toId,
//            getDate() + " " + getTime(),
//            Calendar.getInstance().timeInMillis / 1000,
//            "read"
//        )
//
//
//        reference.setValue(chatMessage)
//            .addOnSuccessListener {
//                Log.d("ChatLogActivity", "Sent Message to User's Database: success")
//            }
//
//        toReference.setValue(chatToMessage)
//            .addOnSuccessListener {
//                Log.d("ChatLogActivity", "Sent Message to Sender's Database: success")
//
//                if (action_bar_timeStamp.text != "online" && action_bar_timeStamp.text != "typing...")
//                    sendNotificationToDevice(chatMessage.text, user, chatMessage.id)
//            }
//
//        val latestChatMessage = LatestChatMessage(
//            latestMessageReference.key!!, text,
//            fromId, toId, getDate() + " " + getTime(), Calendar.getInstance().timeInMillis / 1000, false, false
//        )
//
//        val latestToChatMessage = LatestChatMessage(
//            reference.key!!, text,
//            fromId, toId, getDate() + " " + getTime(), Calendar.getInstance().timeInMillis / 1000, false, false
//        )
//
//        latestMessageReference.setValue(latestChatMessage)
//            .addOnSuccessListener {
//                Log.d("ChatLogActivity", "Sent Message to Latest Messages User's Database: success")
//            }
//
//        latestMessageToReference.setValue(latestToChatMessage)
//            .addOnSuccessListener {
//                Log.d("ChatLogActivity", "Sent Message to Latest Messages Sender's Database: success")
//            }
//    }


}






