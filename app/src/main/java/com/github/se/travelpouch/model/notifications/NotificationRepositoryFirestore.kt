// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.notifications

import android.util.Log
import com.github.se.travelpouch.model.FirebasePaths
import com.github.se.travelpouch.model.travels.Role
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Repository class for managing notifications in the Firestore database.
 *
 * @property firestore The Firestore instance used for database operations.
 */
class NotificationRepositoryFirestore(private val firestore: FirebaseFirestore) :
    NotificationRepository {

  private val notificationCollection = FirebasePaths.notifications

  override fun getNewUid(): String {
    return firestore.collection(FirebasePaths.notifications).document().id
  }

  /**
   * Adds a new notification to the Firestore database.
   *
   * @param notification The notification to be added.
   */
  override fun addNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    firestore
        .collection(FirebasePaths.notifications)
        .document(notification.notificationUid)
        .set(notification)
        .addOnSuccessListener {
          onSuccess()
          Log.d("NotificationRepository", "Notification added successfully")
        }
        .addOnFailureListener { e ->
          onFailure(Exception("An error occurred. Could not send the notification"))
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
      onNotificationFetched: (List<Notification?>) -> Unit
  ) {
    firestore
        .collection(FirebasePaths.notifications)
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
   * @param onSuccess Callback function to be invoked when the operation is successful.
   * @param onFailure Callback function to be invoked when the operation fails.
   */
  override fun markNotificationAsRead(
      notificationUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    firestore
        .collection(FirebasePaths.notifications)
        .document(notificationUid)
        .update("status", NotificationStatus.READ)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onFailure(e) }
  }

  override fun deleteAllNotificationsForUser(
      userUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    firestore
        .collection(FirebasePaths.notifications)
        .whereEqualTo("receiverUid", userUid)
        .get()
        .addOnSuccessListener { documents ->
          for (document in documents) {
            firestore
                .collection(FirebasePaths.notifications)
                .document(document.id)
                .delete()
                .addOnFailureListener { exception -> onFailure(exception) }
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
   * @param onSuccess Callback function to be invoked when the operation is successful.
   * @param onFailure Callback function to be invoked when the operation fails.
   */
  override fun changeNotificationType(
      notificationUid: String,
      notificationType: NotificationType,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    firestore
        .collection(FirebasePaths.notifications)
        .document(notificationUid)
        .update("notificationType", notificationType)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onFailure(e) }
  }

  private fun documentToNotification(document: DocumentSnapshot): Notification? {
    return try {
      val notificationUid = document.id
      val senderUid = document.getString("senderUid")!!
      val receiverUid = document.getString("receiverUid")!!
      val contentData = document["content"] as Map<*, *>
      val notificationType = NotificationType.valueOf(document.getString("notificationType")!!)
      val notificationSector = NotificationSector.valueOf(document.getString("sector")!!)

      val travelUid =
          when (notificationSector) {
            NotificationSector.TRAVEL -> document.getString("travelUid")!!
            NotificationSector.PROFILE -> null
          }

      val content =
          when (notificationType) {
            NotificationType.INVITATION -> {
              when (notificationSector) {
                NotificationSector.TRAVEL -> {
                  val inviterName = contentData["inviterName"] as? String ?: "Unknown Inviter"
                  val travelTitle = contentData["travelTitle"] as? String ?: "No Travel Title"
                  val role = contentData["role"] as? String ?: "PARTICIPANT"
                  NotificationContent.InvitationNotification(
                      inviterName, travelTitle, Role.valueOf(role))
                }
                NotificationSector.PROFILE -> {
                  val inviterEmail = contentData["userEmail"] as? String ?: "Unknown Inviter"
                  NotificationContent.FriendInvitationNotification(userEmail = inviterEmail)
                }
              }
            }
            NotificationType.ROLE_UPDATE -> {
              val travelTitle = contentData["travelTitle"] as? String ?: "No Title"
              val role = contentData["role"] as? String ?: "PARTICIPANT"
              NotificationContent.RoleChangeNotification(travelTitle, Role.valueOf(role))
            }
            NotificationType.ACCEPTED -> {
              when (notificationSector) {
                NotificationSector.TRAVEL -> {
                  val userName = contentData["userName"] as? String ?: "Unknown User"
                  val travelTitle = contentData["travelTitle"] as? String ?: "No Travel Title"
                  NotificationContent.InvitationResponseNotification(userName, travelTitle, true)
                }
                NotificationSector.PROFILE -> {
                  val email = contentData["email"] as? String ?: "Unknown User"
                  NotificationContent.FriendInvitationResponseNotification(email, true)
                }
              }
            }
            NotificationType.DECLINED -> {
              when (notificationSector) {
                NotificationSector.TRAVEL -> {
                  val userName = contentData["userName"] as? String ?: "Unknown User"
                  val travelTitle = contentData["travelTitle"] as? String ?: "No Travel Title"
                  NotificationContent.InvitationResponseNotification(userName, travelTitle, false)
                }
                NotificationSector.PROFILE -> {
                  val email = contentData["email"] as? String ?: "Unknown User"
                  NotificationContent.FriendInvitationResponseNotification(email, false)
                }
              }
            }
          }
      val timestamp = document.getTimestamp("timestamp")!!
      val status = NotificationStatus.valueOf(document.getString("status")!!)

      Notification(
          notificationUid,
          senderUid,
          receiverUid,
          travelUid,
          content,
          notificationType,
          timestamp,
          status,
          notificationSector)
    } catch (e: Exception) {
      Log.e("NotificationRepository", "Error converting document to Notification", e)
      null
    }
  }
}
