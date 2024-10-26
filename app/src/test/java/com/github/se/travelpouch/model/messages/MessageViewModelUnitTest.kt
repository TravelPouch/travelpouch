package com.github.se.travelpouch.model.messages

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

class MessageViewModelUnitTest {

  @Mock private lateinit var messageRepository: MessageRepository
  @Mock private lateinit var messageViewModel: MessageViewModel

  @get:Rule val rule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    messageViewModel = MessageViewModel(messageRepository)
  }

  @Test
  fun loadMessagesForUser() {
    val userId = generateAutoId()
    val messages =
        listOf(
            Message(
                messageUid = generateAutoId(),
                senderUid = generateAutoId(),
                receiverUid = userId,
                travelUid = generateAutoId(),
                content =
                    MessageContent.InvitationMessage("John Doe", "Trip to Paris", Role.PARTICIPANT),
                messageType = MessageType.INVITATION))
    val observer = mock(Observer::class.java) as Observer<List<Message>>
    messageViewModel.messages.observeForever(observer)

    whenever(messageRepository.fetchMessagesForUser(eq(userId), any())).thenAnswer {
      val callback: (List<Message>) -> Unit = it.getArgument(1)
      callback(messages)
    }

    messageViewModel.loadMessagesForUser(userId)

    verify(messageRepository, times(1)).fetchMessagesForUser(eq(userId), any())
    verify(observer, times(1)).onChanged(eq(messages))
  }

  @Test
  fun markMessageAsRead() {
    val messageId = generateAutoId()
    messageViewModel.markMessageAsRead(messageId)
    verify(messageRepository, times(1)).markMessageAsRead(eq(messageId))
  }

  @Test
  fun sendMessage() {
    val message =
        Message(
            messageUid = generateAutoId(),
            senderUid = generateAutoId(),
            receiverUid = generateAutoId(),
            travelUid = generateAutoId(),
            content =
                MessageContent.InvitationMessage("John Doe", "Trip to Paris", Role.PARTICIPANT),
            messageType = MessageType.INVITATION)
    messageViewModel.sendMessage(message)
    verify(messageRepository, times(1)).addMessage(eq(message))
  }
}
