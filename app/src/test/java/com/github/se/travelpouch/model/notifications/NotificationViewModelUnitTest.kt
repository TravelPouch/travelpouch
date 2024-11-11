package com.github.se.travelpouch.model.notifications

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainerMock.generateAutoObjectId
import com.github.se.travelpouch.model.travels.TravelContainerMock.generateAutoUserId
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationViewModelUnitTest {

  @Mock private lateinit var notificationRepository: NotificationRepository
  @Mock private lateinit var notificationViewModel: NotificationViewModel
  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @get:Rule val rule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

      val context = ApplicationProvider.getApplicationContext<Context>()
      FirebaseApp.initializeApp(context)

    notificationViewModel = NotificationViewModel(notificationRepository)

  }

    val senderUid = generateAutoUserId()
    val receiverUid = generateAutoUserId()
    val notificationUid = generateAutoObjectId()
    val travel1Uid = generateAutoObjectId()
    val content1 = NotificationContent.InvitationNotification(
        "John Doe", "Trip to Paris", Role.PARTICIPANT)
    val notificationType1 = NotificationType.INVITATION

    val notification1 = Notification(
        notificationUid = notificationUid,
        senderUid = senderUid,
        receiverUid = receiverUid,
        travelUid = travel1Uid,
        content = content1,
        notificationType = notificationType1)

    val senderUid2 = generateAutoUserId()
    val receiverUid2 = generateAutoUserId()
    val notificationUid2 = generateAutoObjectId()
    val travel2Uid = generateAutoObjectId()
    val content2 = NotificationContent.InvitationNotification(
        "Ludovic Beethoven", "Trip to Berlin", Role.PARTICIPANT)
    val notificationType2 = NotificationType.INVITATION

    val notification2 = Notification(
        notificationUid = notificationUid2,
        senderUid = senderUid2,
        receiverUid = receiverUid2,
        travelUid = travel2Uid,
        content = content2,
        notificationType = notificationType2
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadNotificationsForUser_updatesNotifications() = runBlockingTest {
        val notifications = listOf(notification1, notification2)
        `when`(notificationRepository.fetchNotificationsForUser(any(), any())).then {
            val callback = it.arguments[1] as (List<Notification>) -> Unit
            callback(notifications)
        }

        notificationViewModel.loadNotificationsForUser(receiverUid)

        assertEquals(notifications, notificationViewModel.notifications.value)
    }

    @Test
    fun loadNotificationsForUser_nullNotifications() = runBlockingTest {
        val userId = "user123"
        `when`(notificationRepository.fetchNotificationsForUser(eq(userId), any())).thenAnswer {
            (it.arguments[1] as (List<Notification>) -> Unit).invoke(emptyList())
        }

        notificationViewModel.loadNotificationsForUser(userId)

        assertEquals(emptyList<Notification>(), notificationViewModel.notifications.value)
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

    @Test
    fun deleteAllNotificationsForUser() {
        val userUid = generateAutoUserId()
        val onSuccess = mock(Runnable::class.java)
        val onFailure = mock(Function1::class.java) as (Exception) -> Unit

        notificationViewModel.deleteAllNotificationsForUser(userUid, onSuccess::run, onFailure)

        verify(notificationRepository).deleteAllNotificationsForUser(eq(userUid), any(), any())
    }

    @Test
    fun changeNotificationType() {
        val notificationUid = generateAutoObjectId()
        val notificationType = NotificationType.INVITATION

        notificationViewModel.changeNotificationType(notificationUid, notificationType)

        verify(notificationRepository).changeNotificationType(eq(notificationUid), eq(notificationType))
    }

    @Test
    fun `Factory creates NotificationViewModel instance 2`() {
        val factory = NotificationViewModel.Factory
        val viewModel = factory.create(NotificationViewModel::class.java)

        assertThat(viewModel, instanceOf(NotificationViewModel::class.java))
    }
}
