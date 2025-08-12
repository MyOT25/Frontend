package com.example.myot.notification

data class NotificationItem(
    val id: Long,
    val message: String,
    val profileRes: Int,
    var isNew: Boolean
)