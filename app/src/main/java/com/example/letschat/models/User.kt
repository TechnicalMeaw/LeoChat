package com.example.letschat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val username: String, var profileImageUrl: String, var status: String, val phoneNumber: String, var thumb: String, val notificationToken: String): Parcelable {
    constructor(): this("", "", "", "", "", "", "")
}