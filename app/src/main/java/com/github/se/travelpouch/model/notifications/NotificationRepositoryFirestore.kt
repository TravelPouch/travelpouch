package com.github.se.travelpouch.model.notifications

import android.util.Log
import com.github.se.travelpouch.model.travels.Role
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Repository class for managing notifications in the Firestore database.
 *
 * @property firestore The Firestore instance used for database operations.
 */
class NotificationRepositoryFirestore(private val firestore: FirebaseFirestore) : NotificationRepository {

  // Reference to the "notifications" collection in Firestore
  private val notificationCollection = firestore.collection("notifications")

  override fun getNewUid(): String {
    return notificationCollection.document().id
  }

  /**
   * Adds a new notification to the Firestore database.
   *
   * @param notification The notification to be added.
   */
  override fun addNotification(notification: Notification) {
    notificationCollection
        .document(notification.notificationUid)
        .set(notification)
        .addOnSuccessListener { Log.d("NotificationRepository", "Notification added successfully") }
        .addOnFailureListener { e ->
          Log.e("NotificationRepository", "Error adding notification", e)
        }
  }

  /**
   * Fetches notifications for a specific user from the Firestore database.
   *
   * @param userId The UID of the user whose notifications are to be fetched.
   * @param onNotificationFetched Callback function to be invoked with the list of fetched
   *   notifications.
   */
  override fun fetchNotificationsForUser(
      userId: String,
      onNotificationFetched: (List<Notification>) -> Unit
  ) {
    notificationCollection
        .whereEqualTo("receiverUid", userId)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .get()
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            val notifications = task.result?.documents?.map { documentToNotification(it) }
            onNotificationFetched(notifications ?: emptyList())
          } else {
            Log.e("NotificationRepository", "Error fetching notifications", task.exception)
          }
        }
        .addOnFailureListener { e ->
          Log.e("NotificationRepository", "Error fetching notifications", e)
        }
  }

  /**
   * Marks a notification as read in the Firestore database.
   *
   * @param notificationUid The UID of the notification to be marked as read.
   */
  override fun markNotificationAsRead(notificationUid: String) {
    notificationCollection.document(notificationUid).update("status", NotificationStatus.READ)
  }

  override fun deleteAllNotificationsForUser(
      userUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    notificationCollection
        .whereEqualTo("receiverUid", userUid)
        .get()
        .addOnSuccessListener { documents ->
          for (document in documents) {
            notificationCollection.document(document.id).delete()
          }
          onSuccess()
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Change the notification type in the Firestore database.
   *
   * @param notificationUid The UID of the notification to be changed.
   * @param notificationType The new type of the notification.
   */
  override fun changeNotificationType(notificationUid: String, notificationType: NotificationType) {
    notificationCollection.document(notificationUid).update("notificationType", notificationType)
  }

  private fun documentToNotification(document: DocumentSnapshot): Notification {
    try {
      val notificationUid = document.id
      val senderUid = document.getString("senderUid")!!
      val receiverUid = document.getString("receiverUid")!!
      val travelUid = document.getString("travelUid")!!
      val contentData = document["content"] as Map<*, *>
      val notificationType = NotificationType.valueOf(document.getString("notificationType")!!)
      val content =
          when (notificationType) {
            NotificationType.INVITATION -> {
              val inviterName = contentData["inviterName"] as String
              val travelTitle = contentData["travelTitle"] as String
              val role = contentData["role"] as String
              NotificationContent.InvitationNotification(
                  inviterName, travelTitle, Role.valueOf(role))
            }
            NotificationType.ROLE_UPDATE -> {
              val travelTitle = contentData["travelTitle"] as String
              val role = Role.valueOf(contentData["role"] as String)
              NotificationContent.RoleChangeNotification(travelTitle, role)
            }
            NotificationType.ACCEPTED -> {
              val userName = contentData["userName"] as String
              val travelTitle = contentData["travelTitle"] as String
              NotificationContent.InvitationResponseNotification(userName, travelTitle, true)
            }
            NotificationType.DECLINED -> {
              val userName = contentData["userName"] as String
              val travelTitle = contentData["travelTitle"] as String
              NotificationContent.InvitationResponseNotification(userName, travelTitle, false)
            }
          }
      val timestamp = document.getTimestamp("timestamp")!!
      val status = NotificationStatus.valueOf(document.getString("status")!!)

      return Notification(
          notificationUid,
          senderUid,
          receiverUid,
          travelUid,
          content,
          notificationType,
          timestamp,
          status)
    } catch (e: Exception) {
      Log.e("NotificationRepository", "Error converting document to Notification", e)
      throw e
    }
  }
}
