package com.example.letschat.Notifications

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.letschat.LatestMessagesActivity
import com.example.letschat.LatestMessagesActivity.Companion.currentUser
import com.example.letschat.chatLogActivity
import com.example.letschat.models.User
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class sendNotificationTask {



    companion object{

        val context: Context = chatLogActivity.applicationContext()

        private val FCM_API = "https://fcm.googleapis.com/fcm/send"
        private val serverKey =
            "key=" + "AAAAUcDpD-k:APA91bH5-athDPNJ-46f4wTC9z04dh0pmumwoLLsuvewZirKmXQ8j-5VadPxaZr_YTXW8VQQrx0dIZcp8uCUubO6NAVEZnzxcon6hA7Ujn8udvcvZQvi1LMPYyuOLRmNBE2A697Sk4eg"
        private val contentType = "application/json"

        fun sendNotificationToDevice(text: String, toUser: User, messageId: String){

            val notificationTitle = LatestMessagesActivity.currentUser?.username.toString()
            val recipientToken = toUser.notificationToken.toString()
            val thumbUrl = currentUser?.thumb.toString()

            if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(notificationTitle)) {
                val topic = "/topics/Enter_your_topic_name" //topic has to match what the receiver subscribed to

                val notification = JSONObject()
                val notificationBody = JSONObject()
                val userBody = JSONObject()

                try {

                    userBody.put("uid", currentUser)


                    notificationBody.put("title", notificationTitle)
                    notificationBody.put("message", text)   //Enter your notification message
                    notificationBody.put("dpUrl", thumbUrl)
                    notificationBody.put("fromId", FirebaseAuth.getInstance().uid)
//                notificationBody.put("toId", toUser.uid)
                    notificationBody.put("msgId", messageId)

                    notification.put("to", recipientToken)
                    notification.put("data", notificationBody)
                    Log.e("SEND_NOTIFICATION", "try")
                } catch (e: JSONException) {
                    Log.e("SEND_NOTIFICATION", "onCreate: " + e.message)
                }

                sendNotification(notification)
            }
        }


        private fun sendNotification(notification: JSONObject) {
            // Instantiate the RequestQueue.
            val requestQueue = Volley.newRequestQueue(context)

            Log.e("TAG", "sendNotification")
            val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
                Response.Listener<JSONObject> { response ->
                    Log.i("SEND_NOTIFICATION", "onResponse: $response")
                },
                Response.ErrorListener {
                    Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show()
                    Log.i("SEND_NOTIFICATION", "onErrorResponse: Didn't work")
                }) {

                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Authorization"] = serverKey
                    params["Content-Type"] = contentType
                    return params
                }
            }

            requestQueue.add(jsonObjectRequest)
        }

    }
}