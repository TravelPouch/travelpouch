package com.github.se.travelpouch.model.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * ViewModel class for managing messages in the travel pouch application.
 *
 * @property messageRepository The repository used for message operations.
 */
class MessageViewModel(private val messageRepository: MessageRepository) : ViewModel() {

  // LiveData holding the list of messages
  private val _messages = MutableLiveData<List<Message>>()
  val messages: LiveData<List<Message>> = _messages

  /**
   * Factory object for creating instances of MessageViewModel. This factory is used to provide the
   * MessageViewModel with a MessageRepository that is initialized with Firebase Firestore.
   */
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MessageViewModel(MessageRepository(Firebase.firestore)) as T
          }
        }
  }

  /**
   * Loads messages for a specific user.
   *
   * @param userId The UID of the user whose messages are to be loaded.
   */
  fun loadMessagesForUser(userId: String) {
    messageRepository.fetchMessagesForUser(userId) { messages -> _messages.value = messages }
  }

  /**
   * Marks a message as read.
   *
   * @param messageId The UID of the message to be marked as read.
   */
  fun markMessageAsRead(messageId: String) {
    messageRepository.markMessageAsRead(messageId)
  }

  /**
   * Sends a new message.
   *
   * @param message The message to be sent.
   */
  fun sendMessage(message: Message) {
    messageRepository.addMessage(message)
  }

  fun changeMessageType(messageId: String, messageType: MessageType) {
    messageRepository.changeMessageType(messageId, messageType)
  }
}