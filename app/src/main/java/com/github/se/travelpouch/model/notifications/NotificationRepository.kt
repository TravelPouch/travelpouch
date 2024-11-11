package com.github.se.travelpouch.model.notifications

interface NotificationRepository {

  fun getNewUid(): String

  fun addNotification(notification: Notification)

  fun fetchNotificationsForUser(userId: String, onNotificationFetched: (List<Notification>) -> Unit)

  fun markNotificationAsRead(notificationUid: String)

  fun deleteAllNotificationsForUser(
      userUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun changeNotificationType(notificationUid: String, notificationType: NotificationType)
}
