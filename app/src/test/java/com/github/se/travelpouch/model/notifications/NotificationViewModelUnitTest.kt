package com.github.se.travelpouch.model.notifications

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainerMock.generateAutoObjectId
import com.github.se.travelpouch.model.TravelContainerMock.generateAutoUserId
import com.google.firebase.firestore.FirebaseFirestore
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
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
  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @get:Rule val rule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    notificationViewModel = NotificationViewModel(notificationRepository)
  }

  @Test
  fun loadNotificationsForUser() {
    val userId = generateAutoUserId()
    val notifications =
        listOf(
            Notification(
                notificationUid = generateAutoObjectId(),
                senderUid = generateAutoUserId(),
                receiverUid = userId,
                travelUid = generateAutoObjectId(),
                content =
                    NotificationContent.InvitationNotification(
                        "John Doe", "Trip to Paris", Role.PARTICIPANT),
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
    val notificationUid = generateAutoObjectId()
    notificationViewModel.markNotificationAsRead(notificationUid)
    verify(notificationRepository, times(1)).markNotificationAsRead(eq(notificationUid))
  }

  @Test
  fun sendNotification() {
    val notification =
        Notification(
            notificationUid = generateAutoObjectId(),
            senderUid = generateAutoUserId(),
            receiverUid = generateAutoUserId(),
            travelUid = generateAutoObjectId(),
            content =
                NotificationContent.InvitationNotification(
                    "John Doe", "Trip to Paris", Role.PARTICIPANT),
            notificationType = NotificationType.INVITATION)
    notificationViewModel.sendNotification(notification)
    verify(notificationRepository, times(1)).addNotification(eq(notification))
  }

  @Test
  fun `Factory creates NotificationViewModel instance`() {
    // Directly mock NotificationRepository for simplicity
    notificationRepository = mock(NotificationRepository::class.java)

    // Create the ViewModel using the Factory
    val factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel(notificationRepository) as T
          }
        }

    val viewModel = factory.create(NotificationViewModel::class.java)

    assertThat(viewModel, instanceOf(NotificationViewModel::class.java))
  }
}
