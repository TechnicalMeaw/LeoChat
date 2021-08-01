package com.example.letschat.models

import java.util.*

fun getTime(): String {
    val timeNow = Calendar.getInstance().time
    val H = timeNow.hours
    var M = timeNow.minutes.toString()

    if (M.length < 2){
        M = "0$M"
    }
    var time: String = ""
    if (H > 12){
        time = """${(H - 12)}:$M pm"""
    }else{
        time = "$H:$M am"
    }
    return time
}

fun getDate(): String {
    val timeNow = Calendar.getInstance().time
    val day = timeNow.toString().substring(0,3)
    val mon = timeNow.toString().substring(4, 7)
    val dd = timeNow.toString().substring(8, 10)
    val yyyy = timeNow.toString().substring(30)

    return "$day, $dd $mon, $yyyy"

}