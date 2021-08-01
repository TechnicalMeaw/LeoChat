package com.example.letschat.Notifications

import android.R.attr.bitmap
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.letschat.*
import com.example.letschat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.net.URL
import kotlin.random.Random


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val ADMIN_CHANNEL_ID = "admin_channel"


    companion object {
        var sharedPref: SharedPreferences? = null

        var token: String?
            get() {
                return sharedPref?.getString("token", "")
            }
            set(value) {
                sharedPref?.edit()?.putString("token", value)?.apply()
            }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val msgId= message.data["msgId"].toString()
        val fromId= message.data["fromId"].toString()

        val msgRef = FirebaseDatabase.getInstance().getReference("/messages/${fromId}/${FirebaseAuth.getInstance().uid}/${msgId}/")

        try {
            msgRef.child("read").setValue("unread")
        } catch (e: IOException) {
            println(e)
        }

//
//        val msgRef = FirebaseDatabase.getInstance().getReference("/messages/${fromId}/${FirebaseAuth.getInstance().uid}/${msgId}")
//        msgRef.child("read").setValue("unread")


//        var currentUser : User? = null
//        var user : User? = null
//
//        val currentUserRef = FirebaseDatabase.getInstance().getReference("/users/${message.data["fromId"]}")
//        val toUserRef = FirebaseDatabase.getInstance().getReference("/users/${message.data["toId"]}")
//
//        currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                currentUser = snapshot.getValue(User::class.java)
//            }
//            override fun onCancelled(error: DatabaseError) {
//            }
//        })
//
//        toUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                user = snapshot.getValue(User::class.java)
//            }
//            override fun onCancelled(error: DatabaseError) {
//            }
//        })


        val intent = Intent(this, LatestMessagesActivity::class.java)

//        intent.putExtra(NewMessagesActivity.USER_KEY, user)
//        intent.putExtra(NewMessagesActivity.CURRENT_USER, currentUser)
//        intent.putExtra("UserId", message.data["fromId"])

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt(3000)

        /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them.
      */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        var image : Bitmap? = null
        try {
            val url = URL(message.data["dpUrl"].toString())
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: IOException) {
            println(e)
        }



        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.send)
            .setLargeIcon(image)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setAutoCancel(true)
            .setSound(notificationSoundUri)
            .setContentIntent(pendingIntent)

        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color = resources.getColor(R.color.colorPrimaryDark)
        }
        notificationManager.notify(notificationID, notificationBuilder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to device notification"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(
            ADMIN_CHANNEL_ID,
            adminChannelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }
}
