package com.github.se.travelpouch.model.notifications

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.generateAutoId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class NotificationViewModelUnitTest {

  @Mock private lateinit var notificationRepository: NotificationRepository
  @Mock private lateinit var notificationViewModel: NotificationViewModel

  @get:Rule val rule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    notificationViewModel = NotificationViewModel(notificationRepository)
  }

  @Test
  fun loadNotificationsForUser() {
    val userId = generateAutoId()
    val notifications =
        listOf(
            Notification(
                notificationUid = generateAutoId(),
                senderUid = generateAutoId(),
                receiverUid = userId,
                travelUid = generateAutoId(),
                content =
                    NotificationContent.InvitationNotification("John Doe", "Trip to Paris", Role.PARTICIPANT),
                notificationType = NotificationType.INVITATION))
    val observer = mock(Observer::class.java) as Observer<List<Notification>>
    notificationViewModel.notifications.observeForever(observer)

    whenever(notificationRepository.fetchNotificationsForUser(eq(userId), any())).thenAnswer {
      val callback: (List<Notification>) -> Unit = it.getArgument(1)
      callback(notifications)
    }

    notificationViewModel.loadNotificationsForUser(userId)

    verify(notificationRepository, times(1)).fetchNotificationsForUser(eq(userId), any())
    verify(observer, times(1)).onChanged(eq(notifications))
  }

  @Test
  fun markNotificationAsRead() {
    val notificationUid = generateAutoId()
    notificationViewModel.markNotificationAsRead(notificationUid)
    verify(notificationRepository, times(1)).markNotificationAsRead(eq(notificationUid))
  }

  @Test
  fun sendNotification() {
    val notification =
        Notification(
            notificationUid = generateAutoId(),
            senderUid = generateAutoId(),
            receiverUid = generateAutoId(),
            travelUid = generateAutoId(),
            content =
                NotificationContent.InvitationNotification("John Doe", "Trip to Paris", Role.PARTICIPANT),
            notificationType = NotificationType.INVITATION)
    notificationViewModel.sendNotification(notification)
    verify(notificationRepository, times(1)).addNotification(eq(notification))
  }
}
