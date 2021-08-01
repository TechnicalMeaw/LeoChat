package com.example.letschat.models

import android.util.Log
import android.view.View
import com.example.letschat.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.date_stamp.view.*
import kotlinx.android.synthetic.main.image_received_box_view.view.*
import kotlinx.android.synthetic.main.image_send_box_view.view.*
import kotlinx.android.synthetic.main.to_chat_message.view.*
import kotlinx.android.synthetic.main.to_chat_message.view.timeStampTextView
import kotlinx.android.synthetic.main.user_chat_message.view.*

// User who sends the message
class ChatUserItem(private val text: String, private val currentUser: User, private val timeStamp: String, private val read: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.chatLogTextView.text = "$text          "

        viewHolder.itemView.timeStampTextView.text = timeStamp.substring(18)

        if (read == "unread"){
            viewHolder.itemView.messageStatus.visibility = View.VISIBLE
            viewHolder.itemView.messageStatus.setImageResource(R.drawable.unread)
        }else if (read == "sent"){
            viewHolder.itemView.messageStatus.visibility = View.VISIBLE
            viewHolder.itemView.messageStatus.setImageResource(R.drawable.sent)
        }else if (read == "read"){
            viewHolder.itemView.messageStatus.visibility = View.VISIBLE
            viewHolder.itemView.messageStatus.setImageResource(R.drawable.read)
        }

    }

    override fun getLayout(): Int {
        return R.layout.user_chat_message
    }


}



// To whom message is send
class ChatToItem(private val user: User, private val text: String, private val timeStamp: String): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.chatLogToTextView.text = "$text          "

        if(timeStamp != null && timeStamp.length > 16)
            viewHolder.itemView.timeStampTextView.text = timeStamp.substring(18)

    }

    override fun getLayout(): Int {
        return R.layout.to_chat_message
    }
}



// User Send Image Item
class ChatImageUserItem(private val Url: String, private val timeStamp: String, private val read: String): Item<GroupieViewHolder>(){
    val url = Url
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        if (read == "unread"){
            viewHolder.itemView.imageMessageStatus.visibility = View.VISIBLE
            viewHolder.itemView.imageMessageStatus.setImageResource(R.drawable.unread)
        }else if (read == "sent"){
            viewHolder.itemView.imageMessageStatus.visibility = View.VISIBLE
            viewHolder.itemView.imageMessageStatus.setImageResource(R.drawable.sent)
        }else if (read == "read") {
            viewHolder.itemView.imageMessageStatus.visibility = View.VISIBLE
            viewHolder.itemView.imageMessageStatus.setImageResource(R.drawable.read)
        }else{
            viewHolder.itemView.imageMessageStatus.visibility = View.GONE
        }

        Picasso.get().load(Url).into(viewHolder.itemView.chatUserSendImageView, object: com.squareup.picasso.Callback {
            override fun onSuccess() {
                //set animations here
                Log.d("chatLogActivity", "Image loading success: $url")
            }

            override fun onError(e: java.lang.Exception?) {
                //do smth when there is picture loading error
                Log.e("chatLogActivity", "Error: Image loading failed")
            }
        })

        viewHolder.itemView.chatLogUserSendImageTimeStamp.text = timeStamp.substring(18)

    }

    override fun getLayout(): Int {
        return R.layout.image_send_box_view
    }
}

// Received Image Item
class ChatImageToUserReceivedItem(private val Url: String, private val timeStamp: String): Item<GroupieViewHolder>(){
    val url = Url
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        Picasso.get().load(Url).into(viewHolder.itemView.chatToUserReceivedImageView)

        viewHolder.itemView.chatLogToUserReceivedImageTimeStamp.text = timeStamp.substring(18)
    }

    override fun getLayout(): Int {
        return R.layout.image_received_box_view
    }
}

// Chat Date Item RecyclerItem
class chatDateItem(private val timeStamp: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.dateStampTextView.text = timeStamp
    }

    override fun getLayout(): Int {
        return R.layout.date_stamp
    }
}


