package com.github.se.travelpouch.model.notifications

import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationRepositoryFirestoreUnitTest {

  private lateinit var firestore: FirebaseFirestore

  @Mock private lateinit var notificationCollection: CollectionReference

  @Mock private lateinit var query: Query

  @Mock private lateinit var task: Task<QuerySnapshot>
  @Mock private val documents: QuerySnapshot = mock()
  @Mock private val document: DocumentSnapshot = mock()

  private lateinit var notificationRepositoryFirestore: NotificationRepositoryFirestore

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
    firestore = mock(FirebaseFirestore::class.java)
    notificationCollection = mock(CollectionReference::class.java)
    `when`(firestore.collection("notifications")).thenReturn(notificationCollection)
    notificationRepositoryFirestore = NotificationRepositoryFirestore(firestore)
  }

  @Test
  fun newUid_returnsNonNullUid() {
    val documentReference = mock(DocumentReference::class.java)
    `when`(notificationCollection.document()).thenReturn(documentReference)
    `when`(documentReference.id).thenReturn("unique-id")

    val uid = notificationRepositoryFirestore.getNewUid()

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

    val uid1 = notificationRepositoryFirestore.getNewUid()
    val uid2 = notificationRepositoryFirestore.getNewUid()

    assertNotEquals(uid1, uid2)
  }

  @Test
  fun addNotification_successful() {
    val notification = mock(Notification::class.java)
    val documentReference = mock(DocumentReference::class.java)
    val task = Tasks.forResult<Void>(null) // Create a Task<Void> instance

    `when`(notificationCollection.document(notification.notificationUid))
        .thenReturn(documentReference)
    `when`(documentReference.set(any())).thenReturn(task)

    notificationRepositoryFirestore.addNotification(notification)

    verify(documentReference).set(any())
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
      notificationRepositoryFirestore.addNotification(notification)

      // Wait for the latch to be released or timeout
      latch.await(2, TimeUnit.SECONDS)

      // Assert
      verify(documentReference).set(notification)
      Log.e(
          eq("NotificationRepository"), eq("Error adding notification"), any(Exception::class.java))
    }
  }

  @Test
  fun markNotificationAsRead_callsDocumentUpdate() {
    val notificationUid = "6NU2zp2oGdA34s1Q1q5h"
    val documentReference = mock(DocumentReference::class.java)
    val task = Tasks.forResult<Void>(null) // Create a Task<Void> instance

    `when`(notificationCollection.document(notificationUid)).thenReturn(documentReference)
    `when`(documentReference.update("status", NotificationStatus.READ)).thenReturn(task)

    notificationRepositoryFirestore.markNotificationAsRead(notificationUid, {}, {})

    verify(documentReference).update("status", NotificationStatus.READ)
  }

  @Test
  fun convertDocumentToNotification() {
    val document: DocumentSnapshot = mock()

    // Mocking the data
    whenever(document.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(document.getString("senderUid")).thenReturn("ATPaDqjZyogRVtfszvv4d5mj1tp2")
    whenever(document.getString("receiverUid")).thenReturn("IIrRuQpDpzOlRPN52J5QAEj2xOq1")
    whenever(document.getString("travelUid")).thenReturn("6NU2zp2oGdA34s1Q1q5l")
    whenever(document.getString("notificationType")).thenReturn("INVITATION")
    whenever(document.getTimestamp("timestamp")).thenReturn(Timestamp.now())
    whenever(document.getString("status")).thenReturn("UNREAD")
    whenever(document.getString("sector")).thenReturn("TRAVEL")

    whenever(document["content"])
        .thenReturn(
            mapOf(
                "inviterName" to "John Doe",
                "travelTitle" to "Trip to Paris",
                "role" to "PARTICIPANT"))

    // Reflection to access the private method
    val method =
        NotificationRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToNotification", DocumentSnapshot::class.java)
    method.isAccessible = true

    try {
      val result = method.invoke(notificationRepositoryFirestore, document) as Notification
      assertEquals("6NU2zp2oGdA34s1Q1q5h", result.notificationUid)
      assertEquals("ATPaDqjZyogRVtfszvv4d5mj1tp2", result.senderUid)
      assertEquals("IIrRuQpDpzOlRPN52J5QAEj2xOq1", result.receiverUid)
      assertEquals("6NU2zp2oGdA34s1Q1q5l", result.travelUid)
      assertEquals(NotificationType.INVITATION, result.notificationType)
      assertEquals(NotificationStatus.UNREAD, result.status)
      assertEquals(NotificationSector.TRAVEL, result.sector)
    } catch (e: InvocationTargetException) {
      e.printStackTrace()
      fail("Method invocation failed: ${e.cause?.message}")
    } catch (e: Exception) {
      e.printStackTrace()
      fail("Unexpected error: ${e.message}")
    }
  }

  @Test
  fun convertDocumentToNotification_roleUpdate() {
    val document: DocumentSnapshot = mock()

    // Mocking the data
    whenever(document.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(document.getString("senderUid")).thenReturn("ATPaDqjZyogRVtfszvv4d5mj1tp2")
    whenever(document.getString("receiverUid")).thenReturn("IIrRuQpDpzOlRPN52J5QAEj2xOq1")
    whenever(document.getString("travelUid")).thenReturn("6NU2zp2oGdA34s1Q1q5l")
    whenever(document.getString("notificationType")).thenReturn("ROLE_UPDATE")
    whenever(document.getTimestamp("timestamp")).thenReturn(Timestamp.now())
    whenever(document.getString("status")).thenReturn("UNREAD")
    whenever(document.getString("sector")).thenReturn("TRAVEL")

    whenever(document["content"])
        .thenReturn(mapOf("travelTitle" to "Trip to Paris", "role" to "PARTICIPANT"))

    // Reflection to access the private method
    val method =
        NotificationRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToNotification", DocumentSnapshot::class.java)
    method.isAccessible = true

    try {
      val result = method.invoke(notificationRepositoryFirestore, document) as Notification
      assertEquals("6NU2zp2oGdA34s1Q1q5h", result.notificationUid)
      assertEquals("ATPaDqjZyogRVtfszvv4d5mj1tp2", result.senderUid)
      assertEquals("IIrRuQpDpzOlRPN52J5QAEj2xOq1", result.receiverUid)
      assertEquals("6NU2zp2oGdA34s1Q1q5l", result.travelUid)
      assertEquals(NotificationType.ROLE_UPDATE, result.notificationType)
      assertEquals(NotificationStatus.UNREAD, result.status)
      assertEquals(NotificationSector.TRAVEL, result.sector)
    } catch (e: InvocationTargetException) {
      e.printStackTrace()
      fail("Method invocation failed: ${e.cause?.message}")
    } catch (e: Exception) {
      e.printStackTrace()
      fail("Unexpected error: ${e.message}")
    }
  }

  @Test
  fun convertDocumentToNotification_accepted() {
    val document: DocumentSnapshot = mock()

    // Mocking the data
    whenever(document.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(document.getString("senderUid")).thenReturn("ATPaDqjZyogRVtfszvv4d5mj1tp2")
    whenever(document.getString("receiverUid")).thenReturn("IIrRuQpDpzOlRPN52J5QAEj2xOq1")
    whenever(document.getString("travelUid")).thenReturn("6NU2zp2oGdA34s1Q1q5l")
    whenever(document.getString("notificationType")).thenReturn("ACCEPTED")
    whenever(document.getTimestamp("timestamp")).thenReturn(Timestamp.now())
    whenever(document.getString("status")).thenReturn("UNREAD")
    whenever(document.getString("sector")).thenReturn("TRAVEL")

    whenever(document["content"])
        .thenReturn(mapOf("userName" to "John Doe", "travelTitle" to "Trip to Paris"))

    // Reflection to access the private method
    val method =
        NotificationRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToNotification", DocumentSnapshot::class.java)
    method.isAccessible = true

    try {
      val result = method.invoke(notificationRepositoryFirestore, document) as Notification
      assertEquals("6NU2zp2oGdA34s1Q1q5h", result.notificationUid)
      assertEquals("ATPaDqjZyogRVtfszvv4d5mj1tp2", result.senderUid)
      assertEquals("IIrRuQpDpzOlRPN52J5QAEj2xOq1", result.receiverUid)
      assertEquals("6NU2zp2oGdA34s1Q1q5l", result.travelUid)
      assertEquals(NotificationType.ACCEPTED, result.notificationType)
      assertEquals(NotificationStatus.UNREAD, result.status)
      assertEquals(NotificationSector.TRAVEL, result.sector)
    } catch (e: InvocationTargetException) {
      e.printStackTrace()
      fail("Method invocation failed: ${e.cause?.message}")
    } catch (e: Exception) {
      e.printStackTrace()
      fail("Unexpected error: ${e.message}")
    }
  }

  @Test
  fun convertDocumentToNotification_declined() {
    val document: DocumentSnapshot = mock()

    // Mocking the data
    whenever(document.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(document.getString("senderUid")).thenReturn("ATPaDqjZyogRVtfszvv4d5mj1tp2")
    whenever(document.getString("receiverUid")).thenReturn("IIrRuQpDpzOlRPN52J5QAEj2xOq1")
    whenever(document.getString("travelUid")).thenReturn("6NU2zp2oGdA34s1Q1q5l")
    whenever(document.getString("notificationType")).thenReturn("DECLINED")
    whenever(document.getTimestamp("timestamp")).thenReturn(Timestamp.now())
    whenever(document.getString("status")).thenReturn("UNREAD")
    whenever(document.getString("sector")).thenReturn("TRAVEL")

    whenever(document["content"])
        .thenReturn(mapOf("userName" to "John Doe", "travelTitle" to "Trip to Paris"))

    // Reflection to access the private method
    val method =
        NotificationRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToNotification", DocumentSnapshot::class.java)
    method.isAccessible = true

    try {
      val result = method.invoke(notificationRepositoryFirestore, document) as Notification
      assertEquals("6NU2zp2oGdA34s1Q1q5h", result.notificationUid)
      assertEquals("ATPaDqjZyogRVtfszvv4d5mj1tp2", result.senderUid)
      assertEquals("IIrRuQpDpzOlRPN52J5QAEj2xOq1", result.receiverUid)
      assertEquals("6NU2zp2oGdA34s1Q1q5l", result.travelUid)
      assertEquals(NotificationType.DECLINED, result.notificationType)
      assertEquals(NotificationStatus.UNREAD, result.status)
      assertEquals(NotificationSector.TRAVEL, result.sector)
    } catch (e: InvocationTargetException) {
      e.printStackTrace()
      fail("Method invocation failed: ${e.cause?.message}")
    } catch (e: Exception) {
      e.printStackTrace()
      fail("Unexpected error: ${e.message}")
    }
  }

  @Test
  fun convertDocumentToNotification_exception() {
    val document: DocumentSnapshot = mock()

    // Mocking the data to cause an exception
    whenever(document.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(document.getString("senderUid"))
        .thenReturn(null) // This will cause a NullPointerException

    // Reflection to access the private method
    val method =
        NotificationRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToNotification", DocumentSnapshot::class.java)
    method.isAccessible = true

    // Invoke the method and check that it returns null (indicating an error was caught)
    val result = method.invoke(notificationRepositoryFirestore, document)

    // Assert that the result is null, which is expected in case of an exception
    assertNull(result)
  }

  @Test
  fun changeNotificationType_callsDocumentUpdate() {
    val notificationUid = "6NU2zp2oGdA34s1Q1q5h"
    val documentReference = mock(DocumentReference::class.java)
    val task = Tasks.forResult<Void>(null) // Create a Task<Void> instance

    `when`(notificationCollection.document(notificationUid)).thenReturn(documentReference)
    `when`(documentReference.update("notificationType", NotificationType.INVITATION))
        .thenReturn(task)

    notificationRepositoryFirestore.changeNotificationType(
        notificationUid, NotificationType.INVITATION, {}, {})

    verify(documentReference).update("notificationType", NotificationType.INVITATION)
  }

  @Test
  fun deleteAllNotificationsForUser_successful() {
    val mockTask: Task<QuerySnapshot> = mock()
    val mockQuerySnapshot: QuerySnapshot = mock()
    val mockQueryDocumentSnapshot: QueryDocumentSnapshot = mock()
    val mockDocuments = listOf(mockQueryDocumentSnapshot)
    val mockDocumentReference: DocumentReference = mock()
    val mockDeleteTask: Task<Void> = mock()

    whenever(mockQueryDocumentSnapshot.id).thenReturn("documentId")
    whenever(notificationCollection.document("documentId")).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.delete()).thenReturn(mockDeleteTask)
    whenever(mockTask.isSuccessful).thenReturn(true)
    whenever(mockTask.result).thenReturn(mockQuerySnapshot)
    whenever(mockQuerySnapshot.iterator())
        .thenReturn(mockDocuments.iterator() as MutableIterator<QueryDocumentSnapshot>?)
    whenever(firestore.collection("notifications")).thenReturn(notificationCollection)
    whenever(notificationCollection.whereEqualTo("receiverUid", "userUid")).thenReturn(query)
    whenever(query.get()).thenReturn(mockTask)
    whenever(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
    whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
    whenever(mockDeleteTask.addOnSuccessListener(any())).thenReturn(mockDeleteTask)
    whenever(mockDeleteTask.addOnFailureListener(any())).thenReturn(mockDeleteTask)

    var succeeded = false
    var failed = false

    val latch = CountDownLatch(1)

    notificationRepositoryFirestore.deleteAllNotificationsForUser(
        "userUid",
        {
          succeeded = true
          latch.countDown()
        },
        {
          failed = true
          latch.countDown()
        })

    val onSuccessListenerCaptor = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
    verify(mockTask).addOnSuccessListener(onSuccessListenerCaptor.capture())
    onSuccessListenerCaptor.firstValue.onSuccess(mockQuerySnapshot)

    // Wait for the latch to be released
    latch.await(2, TimeUnit.SECONDS)

    assertTrue(succeeded)
    assertFalse(failed)
    verify(mockDocumentReference).delete()
  }

  @Test
  fun deleteAllNotificationsForUser_failure() {
    val mockTask: Task<QuerySnapshot> = mock()
    val exception = Exception("Simulated Firestore failure")

    whenever(mockTask.isSuccessful).thenReturn(false)
    whenever(mockTask.exception).thenReturn(exception)
    whenever(firestore.collection("notifications")).thenReturn(notificationCollection)
    whenever(notificationCollection.whereEqualTo("receiverUid", "userUid")).thenReturn(query)
    whenever(query.get()).thenReturn(mockTask)
    whenever(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
    whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

    var succeeded = false
    var failed = false

    notificationRepositoryFirestore.deleteAllNotificationsForUser(
        "userUid", { succeeded = true }, { failed = true })

    val onFailureListenerCaptor = argumentCaptor<OnFailureListener>()
    verify(mockTask).addOnFailureListener(onFailureListenerCaptor.capture())
    onFailureListenerCaptor.firstValue.onFailure(exception)

    assertFalse(succeeded)
    assertTrue(failed)
  }
}
