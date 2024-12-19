// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel class for managing notifications in the travel pouch application.
 *
 * @property notificationRepository The repository used for notification operations.
 */
@HiltViewModel
class NotificationViewModel
@Inject
constructor(private val notificationRepository: NotificationRepository) : ViewModel() {

  // Preparing branch PR
  // LiveData holding the list of notifications
  private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
  val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

  private val functions = FirebaseFunctions.getInstance("europe-west9")

  fun getNewUid(): String {
    return notificationRepository.getNewUid()
  }

  /**
   * Loads notifications for a specific user.
   *
   * @param userId The UID of the user whose notifications are to be loaded.
   */
  fun loadNotificationsForUser(userId: String) {
    notificationRepository.fetchNotificationsForUser(userId) {
      _notifications.value = it.filterNotNull()
    }
  }

  /**
   * Marks a notification as read.
   *
   * @param notificationsUid The UID of the notification to be marked as read.
   */
  fun markNotificationAsRead(notificationsUid: String) {
    notificationRepository.markNotificationAsRead(notificationsUid, {}, {})
  }

  /**
   * Sends a new notification.
   *
   * @param notification The notification to be sent.
   */
  fun sendNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    notificationRepository.addNotification(notification, onSuccess, onFailure)
  }

  /**
   * Changes the type of a notification.
   *
   * @param notificationsUid The UID of the notification to be changed.
   * @param notificationType The new type of the notification.
   */
  fun changeNotificationType(notificationsUid: String, notificationType: NotificationType) {
    notificationRepository.changeNotificationType(notificationsUid, notificationType, {}, {})
  }

  fun deleteAllNotificationsForUser(
      userUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    notificationRepository.deleteAllNotificationsForUser(userUid, onSuccess, onFailure)
  }

  fun sendNotificationToUser(userId: String, notificationContent: NotificationContent) {
    val data =
        hashMapOf(
            "userId" to userId,
            "message" to notificationContent.toDisplayString(),
        )

    functions
        .getHttpsCallable("sendNotification")
        .call(data)
        .addOnSuccessListener { result -> Log.d("Notification", "Success: ${result.data}") }
        .addOnFailureListener { e -> Log.e("Notification", "Error: ${e.message}") }
  }
}
