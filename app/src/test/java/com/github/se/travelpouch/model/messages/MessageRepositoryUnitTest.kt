package com.github.se.travelpouch.model.messages

import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class MessageRepositoryUnitTest {

  private lateinit var firestore: FirebaseFirestore

  @Mock private lateinit var messageCollection: CollectionReference

  @Mock private lateinit var query: Query

  @Mock private lateinit var task: Task<QuerySnapshot>

  @Mock private lateinit var querySnapshot: QuerySnapshot

  @Mock private lateinit var queryDocumentSnapshot: QueryDocumentSnapshot

  private lateinit var messageRepository: MessageRepository

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
    firestore = mock(FirebaseFirestore::class.java)
    messageCollection = mock(CollectionReference::class.java)
    `when`(firestore.collection("messages")).thenReturn(messageCollection)
    messageRepository = MessageRepository(firestore)
  }

  @Test
  fun newUid_returnsNonNullUid() {
    val documentReference = mock(DocumentReference::class.java)
    `when`(messageCollection.document()).thenReturn(documentReference)
    `when`(documentReference.id).thenReturn("unique-id")

    val uid = messageRepository.getNewUid()

    assertNotNull(uid)
    assertEquals("unique-id", uid)
  }

  @Test
  fun newUid_generatesDifferentUids() {
    val documentReference1 = mock(DocumentReference::class.java)
    val documentReference2 = mock(DocumentReference::class.java)
    `when`(messageCollection.document()).thenReturn(documentReference1, documentReference2)
    `when`(documentReference1.id).thenReturn("unique-id-1")
    `when`(documentReference2.id).thenReturn("unique-id-2")

    val uid1 = messageRepository.getNewUid()
    val uid2 = messageRepository.getNewUid()

    assertNotEquals(uid1, uid2)
  }

  @Test
  fun addMessage_callsCollectionAdd() {
    val message = mock(Message::class.java)

    messageRepository.addMessage(message)

    verify(messageCollection).add(message)
  }

  @Test
  fun fetchMessagesForUser_callsCollectionWhereEqualTo() {
    val userId = "user-id"
    val onMessagesFetched: (List<Message>) -> Unit = mock()

    // Arrange
    whenever(messageCollection.whereEqualTo("receiverId", userId)).thenReturn(query)
    whenever(query.orderBy("timestamp", Query.Direction.DESCENDING)).thenReturn(query)
    whenever(query.get()).thenReturn(task)

    // Capture the OnSuccessListener with ArgumentCaptor
    val successListenerCaptor =
        ArgumentCaptor.forClass(OnSuccessListener::class.java)
            as ArgumentCaptor<OnSuccessListener<QuerySnapshot>>

    // Mock `addOnSuccessListener` to capture the listener
    whenever(task.addOnSuccessListener(successListenerCaptor.capture())).thenReturn(task)

    // Mock the document snapshot and map it to a Message object
    val mockMessage =
        Message(
            "6NU2zp2oGdA34s1Q1q5h",
            "6NU2zp2oGdA34s1Q1q5h",
            "6NU2zp2oGdA34s1Q1222",
            "6NU2zp2oGdA34s1Q1q5h",
            mock(MessageContent::class.java),
            MessageType.INVITATION)
    whenever(querySnapshot.documents).thenReturn(listOf(queryDocumentSnapshot))
    whenever(queryDocumentSnapshot.toObject(Message::class.java)).thenReturn(mockMessage)

    // Act
    messageRepository.fetchMessagesForUser(userId, onMessagesFetched)

    // Simulate successful fetch by invoking the captured OnSuccessListener
    successListenerCaptor.value.onSuccess(querySnapshot)

    // Assert
    verify(messageCollection).whereEqualTo("receiverId", userId)
    verify(query).orderBy("timestamp", Query.Direction.DESCENDING)
    verify(query).get()
    verify(onMessagesFetched).invoke(listOf(mockMessage)) // Verify the callback with expected data
  }

  @Test
  fun fetchMessagesForUser_callsAddOnFailureListener_andLogsError() {
    val userId = "user-id"
    val onMessagesFetched: (List<Message>) -> Unit = mock()

    // Arrange
    whenever(messageCollection.whereEqualTo("receiverId", userId)).thenReturn(query)
    whenever(query.orderBy("timestamp", Query.Direction.DESCENDING)).thenReturn(query)
    whenever(query.get()).thenReturn(task)

    // Mock Task to chain success and failure listeners
    whenever(task.addOnSuccessListener(any<OnSuccessListener<QuerySnapshot>>())).thenReturn(task)

    // Capture the OnFailureListener using ArgumentCaptor
    val failureListenerCaptor = ArgumentCaptor.forClass(OnFailureListener::class.java)
    whenever(task.addOnFailureListener(failureListenerCaptor.capture())).thenReturn(task)

    // Mock Log.e to verify error logging
    mockStatic(Log::class.java).use { logMock ->
      logMock.`when`<Int> { Log.e(any(), any(), any()) }.thenReturn(0)

      // Act
      messageRepository.fetchMessagesForUser(userId, onMessagesFetched)

      // Simulate a failure by invoking the captured OnFailureListener
      val exception = Exception("Simulated Firestore failure")
      failureListenerCaptor.value.onFailure(exception)

      // Verify error logging
      Log.e("MessageRepository", "Error fetching messages", exception)
    }

    // Verify that the failure listener was added to the task
    verify(task).addOnFailureListener(any())
  }

  @Test
  fun markMessageAsRead_callsDocumentUpdate() {
    val messageId = "6NU2zp2oGdA34s1Q1q5h"
    val documentReference = mock(DocumentReference::class.java)
    val task = Tasks.forResult<Void>(null) // Create a Task<Void> instance

    `when`(messageCollection.document(messageId)).thenReturn(documentReference)
    `when`(documentReference.update("status", MessageStatus.READ)).thenReturn(task)

    messageRepository.markMessageAsRead(messageId)

    verify(documentReference).update("status", MessageStatus.READ)
  }
}
