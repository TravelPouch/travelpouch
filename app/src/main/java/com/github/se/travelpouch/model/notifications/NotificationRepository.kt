// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.notifications

interface NotificationRepository {

  fun getNewUid(): String

  fun addNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun fetchNotificationsForUser(
      userId: String,
      onNotificationFetched: (List<Notification?>) -> Unit
  )

  fun markNotificationAsRead(
      notificationUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteAllNotificationsForUser(
      userUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun changeNotificationType(
      notificationUid: String,
      notificationType: NotificationType,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
