package com.github.se.travelpouch.model.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * ViewModel class for managing notifications in the travel pouch application.
 *
 * @property notificationRepository The repository used for notification operations.
 */
class NotificationViewModel(private val notificationRepository: NotificationRepository) :
    ViewModel() {

  // LiveData holding the list of notifications
  private val _notifications = MutableLiveData<List<Notification>>(emptyList())
  val notifications: LiveData<List<Notification>> = _notifications

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
            return NotificationViewModel(NotificationRepository(Firebase.firestore)) as T
          }
        }
  }

  /**
   * Loads notifications for a specific user.
   *
   * @param userId The UID of the user whose notifications are to be loaded.
   */
  fun loadNotificationsForUser(userId: String) {
    notificationRepository.fetchNotificationsForUser(userId) { notifications ->
      _notifications.value = notifications
    }
  }

  /**
   * Marks a notification as read.
   *
   * @param notificationsUid The UID of the notification to be marked as read.
   */
  fun markNotificationAsRead(notificationsUid: String) {
    notificationRepository.markNotificationAsRead(notificationsUid)
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
    notificationRepository.changeNotificationType(notificationsUid, notificationType)
  }
}
