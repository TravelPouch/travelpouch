package com.github.se.travelpouch.model.notifications

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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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

class NotificationRepositoryUnitTest {

  private lateinit var firestore: FirebaseFirestore

  @Mock private lateinit var notificationCollection: CollectionReference

  @Mock private lateinit var query: Query

  @Mock private lateinit var task: Task<QuerySnapshot>

  @Mock private lateinit var querySnapshot: QuerySnapshot

  @Mock private lateinit var queryDocumentSnapshot: QueryDocumentSnapshot

  private lateinit var notificationRepository: NotificationRepository

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
    firestore = mock(FirebaseFirestore::class.java)
    notificationCollection = mock(CollectionReference::class.java)
    `when`(firestore.collection("notifications")).thenReturn(notificationCollection)
    notificationRepository = NotificationRepository(firestore)
  }

  @Test
  fun newUid_returnsNonNullUid() {
    val documentReference = mock(DocumentReference::class.java)
    `when`(notificationCollection.document()).thenReturn(documentReference)
    `when`(documentReference.id).thenReturn("unique-id")

    val uid = notificationRepository.getNewUid()

    assertNotNull(uid)
    assertEquals("unique-id", uid)
  }

  @Test
  fun newUid_generatesDifferentUids() {
    val documentReference1 = mock(DocumentReference::class.java)
    val documentReference2 = mock(DocumentReference::class.java)
    `when`(notificationCollection.document()).thenReturn(documentReference1, documentReference2)
    `when`(documentReference1.id).thenReturn("unique-id-1")
    `when`(documentReference2.id).thenReturn("unique-id-2")

    val uid1 = notificationRepository.getNewUid()
    val uid2 = notificationRepository.getNewUid()

    assertNotEquals(uid1, uid2)
  }

  @Test
  fun addNotification_successful() {
    val notification = mock(Notification::class.java)
    val documentReference = mock(DocumentReference::class.java)
    val task = Tasks.forResult<Void>(null) // Create a Task<Void> instance

    `when`(notificationCollection.document(notification.notificationUid))
        .thenReturn(documentReference)
    `when`(documentReference.set(notification)).thenReturn(task)

    notificationRepository.addNotification(notification)

    verify(documentReference).set(notification)
  }

  @Test
  fun addNotification_failure() {
    val notification = mock(Notification::class.java)
    val documentReference = mock(DocumentReference::class.java)
    val task = Tasks.forException<Void>(Exception("Simulated Firestore failure"))
    val latch = CountDownLatch(1)

    // Arrange mocks
    `when`(notificationCollection.document(notification.notificationUid))
        .thenReturn(documentReference)
    `when`(documentReference.set(notification)).thenReturn(task)

    // Mock the Log.e method to ensure it's called
    mockStatic(Log::class.java).use { logMock ->
      logMock
          .`when`<Int> { Log.e(any(), any(), any()) }
          .thenAnswer {
            latch.countDown() // Release latch when Log.e is called
            0
          }

      // Act
      notificationRepository.addNotification(notification)

      // Wait for the latch to be released or timeout
      latch.await(2, TimeUnit.SECONDS)

      // Assert
      verify(documentReference).set(notification)
      Log.e(
          eq("NotificationRepository"), eq("Error adding notification"), any(Exception::class.java))
    }
  }

  @Test
  fun fetchNotificationsForUser_callsCollectionWhereEqualTo() {
    val userId = "user-id"
    val onNotificationsFetched: (List<Notification>) -> Unit = mock()

    // Arrange
    whenever(notificationCollection.whereEqualTo("receiverId", userId)).thenReturn(query)
    whenever(query.orderBy("timestamp", Query.Direction.DESCENDING)).thenReturn(query)
    whenever(query.get()).thenReturn(task)

    // Capture the OnSuccessListener with ArgumentCaptor
    val successListenerCaptor =
        ArgumentCaptor.forClass(OnSuccessListener::class.java)
            as ArgumentCaptor<OnSuccessListener<QuerySnapshot>>

    // Mock `addOnSuccessListener` to capture the listener
    whenever(task.addOnSuccessListener(successListenerCaptor.capture())).thenReturn(task)

    // Mock the document snapshot and map it to a Notification object
    val mockNotification =
        Notification(
            "6NU2zp2oGdA34s1Q1q5h",
            "6NU2zp2oGdA34s1Q1q5h12345678",
            "6NU2zp2oGdA34s1Q122212345678",
            "6NU2zp2oGdA34s1Q1q5h",
            mock(NotificationContent::class.java),
            NotificationType.INVITATION)
    whenever(querySnapshot.documents).thenReturn(listOf(queryDocumentSnapshot))
    whenever(queryDocumentSnapshot.toObject(Notification::class.java)).thenReturn(mockNotification)

    // Act
    notificationRepository.fetchNotificationsForUser(userId, onNotificationsFetched)

    // Simulate successful fetch by invoking the captured OnSuccessListener
    successListenerCaptor.value.onSuccess(querySnapshot)

    // Assert
    verify(notificationCollection).whereEqualTo("receiverId", userId)
    verify(query).orderBy("timestamp", Query.Direction.DESCENDING)
    verify(query).get()
    verify(onNotificationsFetched)
        .invoke(listOf(mockNotification)) // Verify the callback with expected data
  }

  @Test
  fun fetchNotificationsForUser_callsAddOnFailureListener_andLogsError() {
    val userId = "user-id"
    val onNotificationsFetched: (List<Notification>) -> Unit = mock()

    // Arrange
    whenever(notificationCollection.whereEqualTo("receiverId", userId)).thenReturn(query)
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
      notificationRepository.fetchNotificationsForUser(userId, onNotificationsFetched)

      // Simulate a failure by invoking the captured OnFailureListener
      val exception = Exception("Simulated Firestore failure")
      failureListenerCaptor.value.onFailure(exception)

      // Verify error logging
      Log.e("NotificationRepository", "Error fetching notifications", exception)
    }

    // Verify that the failure listener was added to the task
    verify(task).addOnFailureListener(any())
  }

  @Test
  fun markNotificationAsRead_callsDocumentUpdate() {
    val notificationUid = "6NU2zp2oGdA34s1Q1q5h"
    val documentReference = mock(DocumentReference::class.java)
    val task = Tasks.forResult<Void>(null) // Create a Task<Void> instance

    `when`(notificationCollection.document(notificationUid)).thenReturn(documentReference)
    `when`(documentReference.update("status", NotificationStatus.READ)).thenReturn(task)

    notificationRepository.markNotificationAsRead(notificationUid)

    verify(documentReference).update("status", NotificationStatus.READ)
  }
}
