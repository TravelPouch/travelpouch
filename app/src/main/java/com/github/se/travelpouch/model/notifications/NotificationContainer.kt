package com.github.se.travelpouch.model.notifications

import com.github.se.travelpouch.model.isValidObjectUid
import com.github.se.travelpouch.model.isValidUserUid
import com.google.firebase.Timestamp

/**
 * Data class representing a notification in the travel pouch application.
 *
 * @property notificationUid Unique ID of the notification.
 * @property senderUid UID of the sender.
 * @property receiverUid UID of the receiver.
 * @property travelUid UID of the travel container.
 * @property content Content of the notification.
 * @property notificationType Type of the notification.
 * @property timestamp Timestamp of when the notification was created. Defaults to the current time.
 * @property status Status of the notification. Defaults to UNREAD.
 */
data class Notification(
    val notificationUid: String, // Unique ID of the notification
    val senderUid: String, // UID of the sender
    val receiverUid: String, // UID of the receiver
    val travelUid: String, // UID of the travel container
    val content: NotificationContent,
    val notificationType: NotificationType,
    val timestamp: Timestamp = Timestamp.now(), // By default, timestamp is current time
    val status: NotificationStatus = NotificationStatus.UNREAD // By default, Notification is unread
) {

  init {
    require(isValidObjectUid(notificationUid)) { "Notification UID cannot be blank" }
    // Todo: need to check if the sender exists in the database
    require(isValidUserUid(senderUid)) { "Sender UID cannot be blank" }
    // Todo: need to check if the receiver exists in the database
    require(isValidUserUid(receiverUid)) { "Receiver UID cannot be blank" }
    // Todo: need to check if the travel exists in the database
    require(isValidObjectUid(travelUid)) { "Travel UID cannot be blank" }

    require(senderUid != receiverUid) { "Sender and receiver cannot be the same" }
  }

  /**
   * Converts the Notification object to a Map.
   *
   * @return A map representation of the Notification object.
   */
  fun toMap(): Map<String, Any> {
    return mapOf(
        "notificationUid" to notificationUid,
        "senderUid" to senderUid,
        "receiverUid" to receiverUid,
        "travelUid" to travelUid,
        "content" to content,
        "notificationType" to notificationType,
        "timestamp" to timestamp,
        "status" to status)
  }
}

/** Enum class representing the type of the notification. */
enum class NotificationType(val isAccepted: Boolean) {
  INVITATION(false),
  ACCEPTED(true),
  DECLINED(false),
  ROLE_UPDATE(true)
}

/** Enum class representing the status of the notification. */
enum class NotificationStatus {
  READ,
  UNREAD
}
