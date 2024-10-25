package com.github.se.travelpouch.model.messages

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Repository class for managing messages in the Firestore database.
 *
 * @property firestore The Firestore instance used for database operations.
 */
class MessageRepository(private val firestore: FirebaseFirestore) {

    // Reference to the "messages" collection in Firestore
    private val messageCollection = firestore.collection("messages")

    /**
     * Adds a new message to the Firestore database.
     *
     * @param message The message to be added.
     */
    fun addMessage(message: Message) {
        messageCollection.add(message)
    }

    /**
     * Fetches messages for a specific user from the Firestore database.
     *
     * @param userId The UID of the user whose messages are to be fetched.
     * @param onMessagesFetched Callback function to be invoked with the list of fetched messages.
     */
    fun fetchMessagesForUser(userId: String, onMessagesFetched: (List<Message>) -> Unit) {
        messageCollection.whereEqualTo("receiverId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val messages = querySnapshot.documents.map { it.toObject(Message::class.java)!! }
                onMessagesFetched(messages)
            }
            .addOnFailureListener { e -> Log.e("MessageRepository", "Error fetching messages", e) }
    }

    /**
     * Marks a message as read in the Firestore database.
     *
     * @param messageId The UID of the message to be marked as read.
     */
    fun markMessageAsRead(messageId: String) {
        messageCollection.document(messageId).update("status", MessageStatus.READ)
    }
}