package com.github.se.travelpouch.model.messages

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.UserInfo
import com.github.se.travelpouch.model.generateAutoId
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.util.Date
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageSimulationBetweenTwoUsers {

  private lateinit var messageRepository: MessageRepository
  private lateinit var messageViewModel: MessageViewModel
  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext() // Get the app context
    FirebaseApp.initializeApp(context) // Initialize Firebase with the context

    messageRepository =
        MessageRepository(Firebase.firestore) // Initialize your repository with Firestore
    messageViewModel = MessageViewModel(messageRepository) // Initialize your ViewModel
  }

  @Test
  fun simulate_invitationMessage_accept() {
    // Uids
    val travelUid = generateAutoId()
    val user1Uid = generateAutoId()
    val user2Uid = generateAutoId()

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
    val invitationMessage =
        Message(
            generateAutoId(), // message uid
            user1Uid,
            user2Uid,
            travelUid,
            MessageContent.InvitationMessage(user1.name, travel.title, Role.PARTICIPANT),
            MessageType.INVITATION)
    messageViewModel.sendMessage(invitationMessage)

    Thread.sleep(1000) // Wait for the message to be sent

    // User 2 receives the invitation
    messageViewModel.loadMessagesForUser(user2Uid)
    Thread.sleep(1000) // Wait for the message to be loaded
    messageViewModel.markMessageAsRead(invitationMessage.messageUid)
    messageViewModel.messages.observeForever { messages ->
      val receivedMessage = messages.first()
      assert(receivedMessage.status == MessageStatus.READ)
      assert(receivedMessage.content is MessageContent.InvitationMessage)
      assert(
          receivedMessage.content.toDisplayString() ==
              "Donkey Kong invited you to join the travel Travel to the Moon as a PARTICIPANT.")
    }

    // User 2 accepts the invitation
    // This situation happens when User 2 performs click on the accept button
    messageViewModel.changeMessageType(invitationMessage.messageUid, MessageType.ACCEPTED)
    Thread.sleep(1000) // Wait for the message to be updated
    messageViewModel.sendMessage(
        Message(
            invitationMessage.messageUid,
            invitationMessage.receiverUid,
            invitationMessage.senderUid,
            invitationMessage.travelUid,
            MessageContent.InvitationResponseMessage(
                user2.name, travel.title, MessageType.ACCEPTED.isAccepted),
            MessageType.ACCEPTED,
            invitationMessage.timestamp,
            MessageStatus.UNREAD))
    Thread.sleep(1000) // Wait for the message to be updated

    // User 1 receives the acceptance
    messageViewModel.loadMessagesForUser(user1Uid)
    Thread.sleep(1000) // Wait for the message to be loaded
    // Verfiy that the message is unread
    messageViewModel.messages.observeForever { messages ->
      val receivedMessage = messages.first()
      assert(receivedMessage.status == MessageStatus.UNREAD)
    }
    Thread.sleep(1000) // Wait for the message to be loaded
    messageViewModel.markMessageAsRead(invitationMessage.messageUid)
    messageViewModel.messages.observeForever { messages ->
      val receivedMessage = messages.first()
      assert(receivedMessage.status == MessageStatus.READ)
      assert(
          receivedMessage.content.toDisplayString() ==
              "Diddy Kong has accepted your invitation for the 'Travel to the Moon' trip.")
    }
  }
}
