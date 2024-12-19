// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.notifications

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainerMock.generateAutoObjectId
import com.github.se.travelpouch.model.travels.TravelContainerMock.generateAutoUserId
import com.google.firebase.FirebaseApp
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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationViewModelUnitTest {

  @Mock private lateinit var notificationRepositoryFirestore: NotificationRepositoryFirestore
  @Mock private lateinit var notificationViewModel: NotificationViewModel

  @get:Rule val rule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    val context = ApplicationProvider.getApplicationContext<Context>()
    FirebaseApp.initializeApp(context)

    notificationViewModel = NotificationViewModel(notificationRepositoryFirestore)
  }

  val senderUid = generateAutoUserId()
  val receiverUid = generateAutoUserId()
  val notificationUid = generateAutoObjectId()
  val travel1Uid = generateAutoObjectId()
  val content1 =
      NotificationContent.InvitationNotification("John Doe", "Trip to Paris", Role.PARTICIPANT)
  val notificationType1 = NotificationType.INVITATION
  val sector: NotificationSector = NotificationSector.TRAVEL

  val notification1 =
      Notification(
          notificationUid = notificationUid,
          senderUid = senderUid,
          receiverUid = receiverUid,
          travelUid = travel1Uid,
          content = content1,
          notificationType = notificationType1,
          sector = sector)

  val senderUid2 = generateAutoUserId()
  val receiverUid2 = generateAutoUserId()
  val notificationUid2 = generateAutoObjectId()
  val travel2Uid = generateAutoObjectId()
  val content2 =
      NotificationContent.InvitationNotification(
          "Ludovic Beethoven", "Trip to Berlin", Role.PARTICIPANT)
  val notificationType2 = NotificationType.INVITATION

  val notification2 =
      Notification(
          notificationUid = notificationUid2,
          senderUid = senderUid2,
          receiverUid = receiverUid2,
          travelUid = travel2Uid,
          content = content2,
          notificationType = notificationType2,
          sector = sector)

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun loadNotificationsForUser_updatesNotifications() = runBlockingTest {
    val notifications = listOf(notification1, notification2)
    `when`(notificationRepositoryFirestore.fetchNotificationsForUser(any(), any())).then {
      val callback = it.arguments[1] as (List<Notification>) -> Unit
      callback(notifications)
    }

    notificationViewModel.loadNotificationsForUser(receiverUid)

    assertEquals(notifications, notificationViewModel.notifications.value)
  }

  @Test
  fun loadNotificationsForUser_nullNotifications() = runBlockingTest {
    val userId = "user123"
    `when`(notificationRepositoryFirestore.fetchNotificationsForUser(eq(userId), any()))
        .thenAnswer { (it.arguments[1] as (List<Notification>) -> Unit).invoke(emptyList()) }

    notificationViewModel.loadNotificationsForUser(userId)

    assertEquals(emptyList<Notification>(), notificationViewModel.notifications.value)
  }

  @Test
  fun markNotificationAsRead() {
    val notificationUid = generateAutoObjectId()
    notificationViewModel.markNotificationAsRead(notificationUid)
    verify(notificationRepositoryFirestore, times(1))
        .markNotificationAsRead(eq(notificationUid), any(), any())
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
            notificationType = NotificationType.INVITATION,
            sector = sector)
    notificationViewModel.sendNotification(notification, {}, {})
    verify(notificationRepositoryFirestore, times(1))
        .addNotification(eq(notification), anyOrNull(), anyOrNull())
  }

  @Test
  fun `Factory creates NotificationViewModel instance`() {
    // Directly mock NotificationRepository for simplicity
    notificationRepositoryFirestore = mock(NotificationRepositoryFirestore::class.java)

    // Create the ViewModel using the Factory
    val factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel(notificationRepositoryFirestore) as T
          }
        }

    val viewModel = factory.create(NotificationViewModel::class.java)

    assertThat(viewModel, instanceOf(NotificationViewModel::class.java))
  }

  @Test
  fun deleteAllNotificationsForUser() {
    val userUid = generateAutoUserId()
    val onSuccess = {}
    val onFailure = mock(Function1::class.java) as (Exception) -> Unit

    notificationViewModel.deleteAllNotificationsForUser(userUid, onSuccess, onFailure)

    verify(notificationRepositoryFirestore).deleteAllNotificationsForUser(eq(userUid), any(), any())
  }

  @Test
  fun changeNotificationType() {
    val notificationUid = generateAutoObjectId()
    val notificationType = NotificationType.INVITATION

    notificationViewModel.changeNotificationType(notificationUid, notificationType)

    verify(notificationRepositoryFirestore)
        .changeNotificationType(eq(notificationUid), eq(notificationType), any(), any())
  }
}
