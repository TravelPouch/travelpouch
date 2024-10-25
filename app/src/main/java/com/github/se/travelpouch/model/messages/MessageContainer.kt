package com.github.se.travelpouch.model.messages

import com.github.se.travelpouch.model.isValidUid
import com.google.firebase.Timestamp

/**
 * Data class representing a message in the travel pouch application.
 *
 * @property messageUid Unique ID of the message.
 * @property senderUid UID of the sender.
 * @property receiverUid UID of the receiver.
 * @property travelUid UID of the travel container.
 * @property content Content of the message.
 * @property messageType Type of the message.
 * @property timestamp Timestamp of when the message was created. Defaults to the current time.
 * @property status Status of the message. Defaults to UNREAD.
 */
data class Message(
    val messageUid: String, // Unique ID of the message
    val senderUid: String, // UID of the sender
    val receiverUid: String, // UID of the receiver
    val travelUid: String, // UID of the travel container
    val content: MessageContent,
    val messageType: MessageType,
    val timestamp: Timestamp = Timestamp.now(), // By default, timestamp is current time
    val status: MessageStatus = MessageStatus.UNREAD // By default, message is unread
) {

  init {
    require(isValidUid(messageUid)) { "Message UID cannot be blank" }
    // Todo: need to check if the sender exists in the database
    require(isValidUid(senderUid)) { "Sender UID cannot be blank" }
    // Todo: need to check if the receiver exists in the database
    require(isValidUid(receiverUid)) { "Receiver UID cannot be blank" }
    // Todo: need to check if the travel exists in the database
    require(isValidUid(travelUid)) { "Travel UID cannot be blank" }

    require(senderUid != receiverUid) { "Sender and receiver cannot be the same" }
  }

  /**
   * Converts the Message object to a Map.
   *
   * @return A map representation of the Message object.
   */
  fun toMap(): Map<String, Any> {
    return mapOf(
        "messageId" to messageUid,
        "senderId" to senderUid,
        "receiverId" to receiverUid,
        "travelId" to travelUid,
        "content" to content,
        "messageType" to messageType,
        "timestamp" to timestamp,
        "status" to status)
  }
}

/** Enum class representing the type of the message. */
enum class MessageType {
  INVITATION,
  ACCEPTED,
  DECLINED,
  ROLE_UPDATE
}

/** Enum class representing the status of the message. */
enum class MessageStatus {
  READ,
  UNREAD
}
