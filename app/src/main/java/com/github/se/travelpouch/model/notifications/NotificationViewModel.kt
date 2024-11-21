package com.github.se.travelpouch.model.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel class for managing notifications in the travel pouch application.
 *
 * @property notificationRepository The repository used for notification operations.
 */
class NotificationViewModel(private val notificationRepository: NotificationRepository) :
    ViewModel() {

  // LiveData holding the list of notifications
  private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
  val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

  /**
   * Factory object for creating instances of NotificationViewModel. This factory is used to provide
   * the NotificationViewModel with a NotificationRepository that is initialized with Firebase
   * Firestore.
   */
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val firestore = Firebase.firestore
            if (BuildConfig.DEBUG)
              firestore.useEmulator("10.0.2.2", 8080)
            return NotificationViewModel(NotificationRepositoryFirestore(firestore)) as T
          }
        }
  }

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
  fun sendNotification(notification: Notification) {
    notificationRepository.addNotification(notification)
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
}
