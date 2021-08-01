package com.example.letschat.UploadOperations

import android.util.Log
import com.example.letschat.Notifications.sendNotificationTask.Companion.sendNotificationToDevice
import com.example.letschat.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class uploadMessageToDatabase{
    companion object{
        fun sendMessageToFirebaseDatabase(text: String, user: User){
            // saves the message to firebase database
            val fromId = FirebaseAuth.getInstance().uid
            val toId = user.uid

            // get reference to the Messages node
            val reference = FirebaseDatabase.getInstance().getReference("/messages/$fromId/$toId").push()

            val toReference = FirebaseDatabase.getInstance().getReference("/messages/$toId/$fromId").push()

            val latestMessageReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")

            val latestMessageToReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

            val chatMessage = ChatMessage(
                reference.key!!,
                text,
                fromId!!,
                toId,
                getDate() + " " + getTime(),
                Calendar.getInstance().timeInMillis / 1000,
                "sent"
            )

            val chatToMessage = ChatMessage(
                reference.key!!,
                text,
                fromId,
                toId,
                getDate() + " " + getTime(),
                Calendar.getInstance().timeInMillis / 1000,
                "sent"
            )


            reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d("ChatLogActivity", "Sent Message to User's Database: success")
                }

            toReference.setValue(chatToMessage)
                .addOnSuccessListener {
                    Log.d("ChatLogActivity", "Sent Message to Sender's Database: success")

                    if (user.status != "online" && user.status != "typing...")
                        sendNotificationToDevice(chatMessage.text, user, chatMessage.id)
                }

            val latestChatMessage = LatestChatMessage(
                latestMessageReference.key!!, text,
                fromId, toId, getDate() + " " + getTime(), Calendar.getInstance().timeInMillis / 1000, false, false
            )

            val latestToChatMessage = LatestChatMessage(
                reference.key!!, text,
                fromId, toId, getDate() + " " + getTime(), Calendar.getInstance().timeInMillis / 1000, false, false
            )

            latestMessageReference.setValue(latestChatMessage)
                .addOnSuccessListener {
                    Log.d("ChatLogActivity", "Sent Message to Latest Messages User's Database: success")
                }

            latestMessageToReference.setValue(latestToChatMessage)
                .addOnSuccessListener {
                    Log.d("ChatLogActivity", "Sent Message to Latest Messages Sender's Database: success")
                }
        }

    }
}