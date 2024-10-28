package com.github.se.travelpouch.model.notifications

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Repository class for managing notifications in the Firestore database.
 *
 * @property firestore The Firestore instance used for database operations.
 */
class NotificationRepository(private val firestore: FirebaseFirestore) {

  // Reference to the "notifications" collection in Firestore
  private val notificationCollection = firestore.collection("notifications")

  fun getNewUid(): String {
    return notificationCollection.document().id
  }

  /**
   * Adds a new notification to the Firestore database.
   *
   * @param notification The notification to be added.
   */
  fun addNotification(notification: Notification) {
      notificationCollection
        .document(notification.notificationUid)
        .set(notification)
        .addOnSuccessListener { Log.d("NotificationRepository", "Notification added successfully") }
        .addOnFailureListener { e -> Log.e("NotificationRepository", "Error adding notification", e) }
  }

  /**
   * Fetches notifications for a specific user from the Firestore database.
   *
   * @param userId The UID of the user whose notifications are to be fetched.
   * @param onNotificationFetched Callback function to be invoked with the list of fetched notifications.
   */
  fun fetchNotificationsForUser(userId: String, onNotificationFetched: (List<Notification>) -> Unit) {
      notificationCollection
        .whereEqualTo("receiverId", userId)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val notifications = querySnapshot.documents.map { it.toObject(Notification::class.java)!! }
          onNotificationFetched(notifications)
        }
        .addOnFailureListener { e -> Log.e("NotificationRepository", "Error fetching notifications", e) }
  }

  /**
   * Marks a notification as read in the Firestore database.
   *
   * @param notificationUid The UID of the notification to be marked as read.
   */
  fun markNotificationAsRead(notificationUid: String) {
      notificationCollection.document(notificationUid).update("status", NotificationStatus.READ)
  }

  fun changeNotificationType(notificationUid: String, notificationType: NotificationType) {
      notificationCollection.document(notificationUid).update("notificationType", notificationType)
  }
}
