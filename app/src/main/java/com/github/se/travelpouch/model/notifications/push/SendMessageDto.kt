package com.github.se.travelpouch.model.notifications.push

import com.github.se.travelpouch.model.notifications.Notification

data class SendMessageDto(
    val to: String?,
    val notification: Notification
)

