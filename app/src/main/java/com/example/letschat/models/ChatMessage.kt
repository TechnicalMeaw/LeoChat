package com.example.letschat.models

import java.sql.Timestamp
import java.util.*


class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timeStamp: String, val timeInMillis: Long, val read: String){
    constructor(): this("", "", "", "", "", -1, "")
}

class LatestChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timeStamp: String, val timeInMillis: Long, val read: Boolean, val typing: Boolean){
    constructor(): this("", "", "", "", "", -1, false, false)
}