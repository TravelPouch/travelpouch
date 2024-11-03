package com.github.se.travelpouch.model.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.TravelContainerMock.generateAutoObjectId
import com.github.se.travelpouch.model.TravelContainerMock.generateAutoUserId
import com.github.se.travelpouch.model.UserInfo
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.util.Date
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationSimulationBetweenTwoUsers {

  private lateinit var notificationRepository: NotificationRepository
  private lateinit var notificationViewModel: NotificationViewModel
  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext() // Get the app context
    FirebaseApp.initializeApp(context) // Initialize Firebase with the context

    notificationRepository =
        NotificationRepository(Firebase.firestore) // Initialize your repository with Firestore
    notificationViewModel =
        NotificationViewModel(notificationRepository) // Initialize your ViewModel
  }

  @Test
  fun simulate_invitationNotification_accept() {
    // Uids
    val travelUid = generateAutoObjectId()
    val user1Uid = generateAutoUserId()
    val user2Uid = generateAutoUserId()

    val travel =
        TravelContainer(
            travelUid,
            "Travel to the Moon",
            "A trip to the moon",
            Timestamp(Date(2024, 1, 1)),
            Timestamp(Date(2024, 1, 2)),
            Location(45.00, 45.00, Timestamp(Date(2024, 1, 1)), "Moon"),
            emptyMap(),
            mapOf(Participant(user1Uid) to Role.OWNER))
    val user1 = UserInfo(user1Uid, "Donkey Kong", listOf(travelUid), "donkey.kong@epfl.ch")
    val user2 = UserInfo(user2Uid, "Diddy Kong", emptyList(), "diddy.kong@epfl.ch")

    // User 1 sends an invitation to User 2
    // This situation happens when User 1 adds User 2 to the travel participants
    val invitationNotification =
        Notification(
            generateAutoObjectId(), // notification uid
            user1Uid,
            user2Uid,
            travelUid,
            NotificationContent.InvitationNotification(user1.name, travel.title, Role.PARTICIPANT),
            NotificationType.INVITATION)
    notificationViewModel.sendNotification(invitationNotification)

    Thread.sleep(1000) // Wait for the notification to be sent

    // User 2 receives the invitation
    notificationViewModel.loadNotificationsForUser(user2Uid)
    Thread.sleep(1000) // Wait for the notification to be loaded
    notificationViewModel.markNotificationAsRead(invitationNotification.notificationUid)
    notificationViewModel.notifications.observeForever { notifications ->
      if (notifications.isEmpty()) return@observeForever
      val receivedNotification = notifications.first()
      assert(receivedNotification.status == NotificationStatus.READ)
      assert(receivedNotification.content is NotificationContent.InvitationNotification)
      assert(
          receivedNotification.content.toDisplayString() ==
              "Donkey Kong invited you to join the travel Travel to the Moon as a PARTICIPANT.")
    }

    // User 2 accepts the invitation
    // This situation happens when User 2 performs click on the accept button
    notificationViewModel.changeNotificationType(
        invitationNotification.notificationUid, NotificationType.ACCEPTED)
    Thread.sleep(1000) // Wait for the notification to be updated
    notificationViewModel.sendNotification(
        Notification(
            invitationNotification.notificationUid,
            invitationNotification.receiverUid,
            invitationNotification.senderUid,
            invitationNotification.travelUid,
            NotificationContent.InvitationResponseNotification(
                user2.name, travel.title, NotificationType.ACCEPTED.isAccepted),
            NotificationType.ACCEPTED,
            invitationNotification.timestamp,
            NotificationStatus.UNREAD))
    Thread.sleep(1000) // Wait for the notification to be updated

    // User 1 receives the acceptance
    notificationViewModel.loadNotificationsForUser(user1Uid)
    Thread.sleep(1000) // Wait for the notification to be loaded
    // Verfiy that the notification is unread
    notificationViewModel.notifications.observeForever { notifications ->
      if (notifications.isEmpty()) return@observeForever
      val receivedNotification = notifications.first()
      assert(receivedNotification.status == NotificationStatus.UNREAD)
    }
    Thread.sleep(1000) // Wait for the notification to be loaded
    notificationViewModel.markNotificationAsRead(invitationNotification.notificationUid)
    notificationViewModel.notifications.observeForever { notifications ->
      if (notifications.isEmpty()) return@observeForever
      val receivedNotification = notifications.first()
      assert(receivedNotification.status == NotificationStatus.READ)
      assert(
          receivedNotification.content.toDisplayString() ==
              "Diddy Kong has accepted your invitation for the 'Travel to the Moon' trip.")
    }
  }
}
