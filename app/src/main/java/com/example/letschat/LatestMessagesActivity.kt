package com.example.letschat

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.letschat.Login.LoginActivity
import com.example.letschat.Login.RegisterActivity
import com.example.letschat.models.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messegages.*
import kotlinx.android.synthetic.main.latest_messages_log_row.view.*
import kotlinx.android.synthetic.main.loading_dialog.view.*


class LatestMessagesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    val adapter = GroupAdapter<GroupieViewHolder>()

    val TAG = "LatestMessagesActivity"

    companion object{
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messegages)


        val currentUserReference = FirebaseDatabase.getInstance().getReference("/users/$fromId")


        // Updating Last Seen Status
        updateStatus("online")

        // Setting up the ToolBar
        val toolbar: Toolbar = findViewById(R.id.toolbar_latestMessages)
        setSupportActionBar(toolbar)

        auth = Firebase.auth


        if (checkLogin()){
            loadLatestMessages()
            grabUserFromDatabase()
        }


        adapter.setOnItemClickListener{item, view ->
            val itemUser = item as LatestUserItem

            val latestMessageReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/${itemUser.user?.uid}")
            val latestMessageToReference = FirebaseDatabase.getInstance().getReference("/latest-messages/${itemUser.user?.uid}/$fromId")
            if (itemUser.fromId.toString() != FirebaseAuth.getInstance().uid.toString()){
                latestMessageReference.child("read").setValue(true)
                latestMessageToReference.child("read").setValue(true)
            }
//            if (itemUser.fromId.toString() == FirebaseAuth.getInstance().uid.toString()){
//                latestMessageToReference.child("read").setValue(true)
//            }



            val intent = Intent(view.context, chatLogActivity::class.java)
            intent.putExtra(NewMessagesActivity.USER_KEY, itemUser.user)
            intent.putExtra(NewMessagesActivity.CURRENT_USER, currentUser)
            startActivity(intent)
        }

        // Vertical Divider
        latestMessagesRecyclerView.adapter = adapter
        latestMessagesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        refreshUserObject()



//        val mDialogView = LayoutInflater.from(this).inflate(R.layout.loading_dialog, null)
//        mDialogView.loadingSpin.setMinAndMaxProgress(0.0f, 0.1411f)
//        val mBuilder = AlertDialog.Builder(this)
//            .setView(mDialogView)
//            .setCancelable(false)
//
//
//
//        val mAlertDialog = mBuilder.show()
//        mAlertDialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);
//
//
//
//        val hideLoadingDialog = Runnable { kotlin.run {
//            mAlertDialog.dismiss()
//
//        } }
//
//        val pauseSuccessAnimation = Runnable { kotlin.run {
//            mDialogView.loadingSpin.pauseAnimation()
//            Handler().postDelayed(hideLoadingDialog, 500)
//        } }
//
//        val showSuccessText = Runnable { kotlin.run { mDialogView.loadingSpin.setMinAndMaxProgress(0.27f, 0.5f)
//            mDialogView.loadingText.text = "Success"
//            mDialogView.loadingText.setTextColor(getColor(R.color.colorPrimary))
//        } }
//
//        val showSuccessAnimation = Runnable { kotlin.run { mDialogView.loadingSpin.setMinAndMaxProgress(0.27f, 0.5f)
//            Handler().postDelayed(pauseSuccessAnimation, 3000)
//            Handler().postDelayed(showSuccessText, 2000)
//            } }
//
//
//        Handler().postDelayed(showSuccessAnimation, 3000)



    }





    private fun grabUserFromDatabase(){
        val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("username"))
                    currentUser = snapshot.getValue(User::class.java)
                else
                    redirectToRegisterActivity()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })


    }

    fun redirectToRegisterActivity(){
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("PHONE_NUMBER",FirebaseAuth.getInstance().currentUser?.phoneNumber)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    val latestChatMessageMap = HashMap<String, LatestChatMessage>()

    fun refreshLatestChatMessages(){
        adapter.clear()
        latestChatMessageMap.values.sortedBy { it.timeInMillis }.reversed().forEach{
            adapter.add(LatestUserItem(it))
        }
    }

    private fun checkLogin() : Boolean{
        if (FirebaseAuth.getInstance().uid == null){
            intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return false
        }else{
            return true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        refreshUserObject()
        when(item.itemId){
            R.id.logOutBtn -> {
                FirebaseAuth.getInstance().signOut()
                updateStatus("Last Seen on ${getDate() + " at " + getTime()}")
                checkLogin()
            }
            R.id.newMessageBtn -> {
                intent = Intent(this, NewMessagesActivity::class.java)
                startActivity(intent)
            }
            R.id.settingsBtn -> {
                // Opening User Info Activity

                val intent = Intent(this, UserInfoActivity::class.java)
                intent.putExtra("USER_INFO_KEY", currentUser)
                startActivity(intent)

            }
        }

        return super.onOptionsItemSelected(item)
    }

    private val fromId = FirebaseAuth.getInstance().uid
    private fun loadLatestMessages(){
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val latestChatMessage = snapshot.getValue(LatestChatMessage::class.java)
                if (latestChatMessage != null){
                    latestChatMessageMap[snapshot.key!!] = latestChatMessage
                    refreshLatestChatMessages()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val latestChatMessage = snapshot.getValue(LatestChatMessage::class.java)
                if (latestChatMessage != null){
                    latestChatMessageMap[snapshot.key!!] = latestChatMessage
                    refreshLatestChatMessages()
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



    override fun onPause() {
        updateStatus("Last Seen on ${getDate() + " at " + getTime()}")
        super.onPause()
    }

    override fun onResume() {
        updateStatus("online")
        super.onResume()
        refreshUserObject()
    }
    override fun onDestroy() {
        updateStatus("Last Seen on ${getDate() + " at " + getTime()}")
        super.onDestroy()
    }

    private fun updateStatus(string:String){
        val currentUserReference = FirebaseDatabase.getInstance().getReference("/users/$fromId")
        currentUserReference.child("status").setValue(string)
    }


    private fun refreshUserObject(){
        if(CurrentUserEvents.refreshCurrentUser(currentUser?.uid.toString()) != null) {
            currentUser = CurrentUserEvents.refreshCurrentUser(currentUser?.uid.toString())
        }
    }




}

// Latest Message User Item
// Groupie RecyclerView
class LatestUserItem(private val chatMessage: LatestChatMessage): Item<GroupieViewHolder>(){
    var user: User? = null
    val fromId = chatMessage.fromId
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        // Default Profile Image
        viewHolder.itemView.latestMessageDefaultProfileImageView.setImageResource(R.drawable.default_user_icon)

        // If Typing
        if (chatMessage.typing){
            viewHolder.itemView.typingIcon.visibility = View.VISIBLE
        }else{
            viewHolder.itemView.typingIcon.visibility = View.GONE
        }

        //If Message is Unread
        if (!chatMessage.read && fromId != FirebaseAuth.getInstance().uid){
            viewHolder.itemView.unreadSign.visibility = View.VISIBLE
            viewHolder.itemView.unreadSign.speed = 0.3f
            viewHolder.itemView.latestMessageLastMessageTextView.setTextColor(Color.parseColor("#525252"))
            viewHolder.itemView.latestMessageLastMessageTextView.setTypeface(null, Typeface.BOLD)
            viewHolder.itemView.latestMessageTimeStamp.setTextColor(Color.parseColor("#25c296"))
        }else{
            viewHolder.itemView.unreadSign.visibility = View.GONE
            viewHolder.itemView.latestMessageLastMessageTextView.setTextColor(Color.parseColor("#646464"))
            viewHolder.itemView.latestMessageLastMessageTextView.setTypeface(null, Typeface.NORMAL)
            viewHolder.itemView.latestMessageTimeStamp.setTextColor(Color.parseColor("#757575"))
        }

        // If I Sent The Message
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
            if (chatMessage.read){
                viewHolder.itemView.youTextView.text = "You: "
                if (viewHolder.itemView.sentMessageStatus.visibility == View.GONE){
                    viewHolder.itemView.sentMessageStatus.visibility = View.VISIBLE
                    viewHolder.itemView.sentMessageStatus.playAnimation()
                }
            }else{
                viewHolder.itemView.youTextView.text = "You: "
                viewHolder.itemView.sentMessageStatus.visibility = View.GONE
            }
        }else{
            viewHolder.itemView.youTextView.text = ""
            viewHolder.itemView.sentMessageStatus.visibility = View.GONE
        }

        // Last Message
        if (chatMessage.text.length < 26)
            viewHolder.itemView.latestMessageLastMessageTextView.text = chatMessage.text
        else
            viewHolder.itemView.latestMessageLastMessageTextView.text = chatMessage.text.substring(0,26) + "..."

        // Last Message Checking for Image
        if (chatMessage.text.length> 4){
            if (chatMessage.text.substring(0,4) == "IMG>"){
                viewHolder.itemView.latestMessageLastMessageTextView.text = "        Image"
                viewHolder.itemView.latestMessageLastMessageImageIconImageView.visibility = View.VISIBLE
            }else
                viewHolder.itemView.latestMessageLastMessageImageIconImageView.visibility = View.GONE
        }else
            viewHolder.itemView.latestMessageLastMessageImageIconImageView.visibility = View.GONE

        // Time Stamp
        if(chatMessage.timeStamp != null && chatMessage.timeStamp.length > 16){
            if (getDate() == chatMessage.timeStamp.substring(0,17))
                viewHolder.itemView.latestMessageTimeStamp.text = chatMessage.timeStamp.substring(18)
            else
                viewHolder.itemView.latestMessageTimeStamp.text = chatMessage.timeStamp.substring(0, 11)
        }


        // Username & Profile Photo
        val chatPartnerId = if (FirebaseAuth.getInstance().uid == chatMessage.fromId){
            chatMessage.toId
        }else{
            chatMessage.fromId
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
                // Username
                viewHolder.itemView.latestMessageUsername.text = user?.username
                // ProfileImage
                if (user?.thumb != "")
                    Picasso.get().load(user?.thumb).into(viewHolder.itemView.latestMessageProfileImageView)
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    override fun getLayout(): Int {
        return R.layout.latest_messages_log_row
    }

}