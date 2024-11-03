package com.github.se.travelpouch.model

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.fail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class TravelRepositoryFirestoreUnitTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockDocumentReference: DocumentReference

  @Mock private lateinit var mockCollectionReference: CollectionReference

  @Mock private lateinit var mockAuth: FirebaseAuth

  private lateinit var travelRepositoryFirestore: TravelRepositoryFirestore

  private val travel =
      TravelContainer(
          "6NU2zp2oGdA34s1Q1q5h",
          "Test Title",
          "Test Description",
          Timestamp.now(),
          Timestamp(Timestamp.now().seconds + 1000, 0),
          Location(
              0.0,
              0.0,
              Timestamp.now(),
              "Test Location",
          ),
          mapOf("Test Key item" to "Test Value item"),
          mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    travelRepositoryFirestore = TravelRepositoryFirestore(mockFirestore, mockAuth)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun callsOnSuccessWhenUserIsAuthenticated() {
    val mockAuthStateListenerCaptor = argumentCaptor<FirebaseAuth.AuthStateListener>()
    whenever(mockAuth.currentUser).thenReturn(mock())

    var successCalled = false

    travelRepositoryFirestore.init { successCalled = true }

    verify(mockAuth).addAuthStateListener(mockAuthStateListenerCaptor.capture())
    mockAuthStateListenerCaptor.firstValue.onAuthStateChanged(mockAuth)
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

    assertTrue(successCalled)
  }

  @Test
  fun doesNotCallOnSuccessWhenUserIsNotAuthenticated() {
    val mockAuthStateListenerCaptor = argumentCaptor<FirebaseAuth.AuthStateListener>()
    whenever(mockAuth.currentUser).thenReturn(null)

    var successCalled = false
    travelRepositoryFirestore.init { successCalled = true }

    verify(mockAuth).addAuthStateListener(mockAuthStateListenerCaptor.capture())
    mockAuthStateListenerCaptor.firstValue.onAuthStateChanged(mockAuth)

    assertFalse(successCalled)
  }

  @Test
  fun generatesNewUidSuccessfully() {
    val newUid = "newUid123"
    whenever(mockFirestore.collection("travels").document().id).thenReturn(newUid)

    val result = travelRepositoryFirestore.getNewUid()

    assertEquals(newUid, result)
  }

  @Test
  fun generatesDifferentUidsOnSubsequentCalls() {
    val newUid1 = "newUid123"
    val newUid2 = "newUid456"
    whenever(mockFirestore.collection("travels").document().id).thenReturn(newUid1, newUid2)

    val result1 = travelRepositoryFirestore.getNewUid()
    val result2 = travelRepositoryFirestore.getNewUid()

    assertNotNull(result1)
    assertNotNull(result2)
    assertNotEquals(result1, result2)
  }

  @Test
  fun addsTravelSuccessfully() {
    val task: Task<Void> = mock()
    whenever(mockFirestore.collection("travels").document(travel.fsUid).set(travel.toMap()))
        .thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)

    var successCalled = false
    travelRepositoryFirestore.addTravel(
        travel, { successCalled = true }, { fail("Should not call onFailure") })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(successCalled)
  }

  @Test
  fun failsToAddTravel() {
    val task: Task<Void> = mock()
    whenever(mockFirestore.collection("travels").document(travel.fsUid).set(travel.toMap()))
        .thenReturn(task)
    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(Exception("Firestore error"))

    var failureCalled = false
    travelRepositoryFirestore.addTravel(
        travel, { fail("Should not call onSuccess") }, { failureCalled = true })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(failureCalled)
  }

  @Test
  fun updatesTravelSuccessfully() {
    val task: Task<Void> = mock()
    whenever(mockFirestore.collection("travels").document(travel.fsUid).set(travel.toMap()))
        .thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)

    var successCalled = false
    travelRepositoryFirestore.updateTravel(
        travel, { successCalled = true }, { fail("Should not call onFailure") })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(successCalled)
  }

  @Test
  fun failsToUpdateTravel() {
    val task: Task<Void> = mock()
    whenever(mockFirestore.collection("travels").document(travel.fsUid).set(travel.toMap()))
        .thenReturn(task)
    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(Exception("Firestore error"))

    var failureCalled = false
    travelRepositoryFirestore.updateTravel(
        travel, { fail("Should not call onSuccess") }, { failureCalled = true })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(failureCalled)
  }

  @Test
  fun deletesTravelByIdSuccessfully() {
    val task: Task<Void> = mock()
    whenever(mockFirestore.collection("travels").document(travel.fsUid).delete()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)

    var successCalled = false
    travelRepositoryFirestore.deleteTravelById(
        travel.fsUid, { successCalled = true }, { fail("Should not call onFailure") })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(successCalled)
  }

  @Test
  fun failsToDeleteTravelById() {
    val task: Task<Void> = mock()
    whenever(mockFirestore.collection("travels").document(travel.fsUid).delete()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(Exception("Firestore error"))

    var failureCalled = false
    travelRepositoryFirestore.deleteTravelById(
        travel.fsUid, { fail("Should not call onSuccess") }, { failureCalled = true })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(failureCalled)
  }

  @Test
  fun retrievesTravelsSuccessfully() {
    val task = mock<Task<QuerySnapshot>>()
    val querySnapshot = mock<QuerySnapshot>()

    whenever(mockFirestore.collection("travels").get()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.result).thenReturn(querySnapshot)
    whenever(querySnapshot.documents).thenReturn(listOf(mock()))

    var successCalled = false
    travelRepositoryFirestore.getTravels(
        { successCalled = true }, { fail("Should not call onFailure") })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(successCalled)
  }

  @Test
  fun noTravelDocumentsRetrieved() {
    val task: Task<QuerySnapshot> = mock()
    val querySnapshot: QuerySnapshot = mock()

    whenever(mockFirestore.collection("travels").get()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.result).thenReturn(querySnapshot)
    whenever(querySnapshot.documents).thenReturn(emptyList())

    var successCalled = false
    travelRepositoryFirestore.getTravels(
        { successCalled = true }, { fail("Should not call onFailure") })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(successCalled)
  }

  @Test
  fun performsFirestoreOperationSuccessfullyUsingReflection() {
    val task: Task<Void> = mock()
    whenever(task.isSuccessful).thenReturn(true)

    var successCalled = false
    val method =
        travelRepositoryFirestore::class
            .java
            .getDeclaredMethod(
                "performFirestoreOperation",
                Task::class.java,
                Function0::class.java,
                Function1::class.java)
    method.isAccessible = true

    val onSuccess: () -> Unit = { successCalled = true }
    val onFailure: (Exception) -> Unit = { fail("Should not call onFailure") }

    method.invoke(travelRepositoryFirestore, task, onSuccess, onFailure)

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(successCalled)
  }

  @Test
  fun failsToPerformFirestoreOperationUsingReflection() {
    val task: Task<Void> = mock()
    val exception = Exception("Firestore error")
    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(exception)

    var failureCalled = false
    val method =
        travelRepositoryFirestore::class
            .java
            .getDeclaredMethod(
                "performFirestoreOperation",
                Task::class.java,
                Function0::class.java, // onSuccess
                Function1::class.java // onFailure
                )
    method.isAccessible = true

    // Create the lambda functions to match the expected types
    val onSuccess: () -> Unit = { fail("Should not call onSuccess") }
    val onFailure: (Exception) -> Unit = { failureCalled = true }

    method.invoke(travelRepositoryFirestore, task, onSuccess, onFailure)

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(failureCalled)
  }

  @Test
  fun retrievesTravelsSuccessfullyWithDocumentToTravel() {
    val task = mock<Task<QuerySnapshot>>()
    val querySnapshot = mock<QuerySnapshot>()
    val document = mock<DocumentSnapshot>()

    whenever(mockFirestore.collection("travels").get()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.result).thenReturn(querySnapshot)
    whenever(querySnapshot.documents).thenReturn(listOf(document))
    whenever(document.id).thenReturn("6NU2zp2oGdA34s1Q1q5h") // Ensure getId() returns a String
    whenever(document.getString("title")).thenReturn("Test Title")
    whenever(document.getString("description")).thenReturn("Test Description")
    whenever(document.getTimestamp("startTime")).thenReturn(Timestamp.now())
    whenever(document.getTimestamp("endTime"))
        .thenReturn(Timestamp(Timestamp.now().seconds + 1000, 0))
    val attachmentsMap = mapOf("Test Key item" to "Test Value item")
    whenever(document.get("allAttachments")).thenReturn(attachmentsMap)

    val participantsMap = mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER)
    whenever(document.get("allParticipants"))
        .thenReturn(participantsMap.map { (key, value) -> key.fsUid to value.name }.toMap())

    val locationMap =
        mapOf(
            "latitude" to 0.0,
            "longitude" to 0.0,
            "name" to "Test Location",
            "insertTime" to Timestamp.now())
    whenever(document.get("location")).thenReturn(locationMap)

    val method =
        travelRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToTravel", DocumentSnapshot::class.java)
    method.isAccessible = true

    val result = method.invoke(travelRepositoryFirestore, document) as TravelContainer?
    assertNotNull(result) // This checks if the result is not null
    assertEquals("6NU2zp2oGdA34s1Q1q5h", result?.fsUid)
    assertEquals("Test Title", result?.title)
    assertEquals("Test Description", result?.description)
    assertNotNull(result?.startTime)
    assertNotNull(result?.endTime)
    assertNotNull(result?.location)
    assertEquals(0.0, result?.location?.latitude)
    assertEquals(0.0, result?.location?.longitude)
    assertEquals("Test Location", result?.location?.name)
    assertEquals(attachmentsMap, result?.allAttachments)
    assertEquals(participantsMap, result?.allParticipants)

    // Mock the static Log class
    mockStatic(Log::class.java).use { logMock ->
      var successCalled = false
      travelRepositoryFirestore.getTravels(
          { successCalled = true }, { fail("Should not call onFailure") })

      val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
      verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
      onCompleteListenerCaptor.firstValue.onComplete(task)

      assertTrue(successCalled)

      // Verify the log call
      logMock.verify { Log.d("TravelRepositoryFirestore", "getTravels") }
    }
  }

  @Test
  fun returnsNullWhenTravelsFieldsAreNull() {
    val document: DocumentSnapshot = mock()
    whenever(document.id).thenReturn(null)
    whenever(document.getString("title")).thenReturn(null)
    whenever(document.getString("description")).thenReturn(null)
    whenever(document.getTimestamp("startTime")).thenReturn(null)
    whenever(document.getTimestamp("endTime")).thenReturn(null)

    whenever(document.get("location")).thenReturn(null)
    whenever(document.get("allAttachments")).thenReturn(null)

    whenever(document.get("allParticipants")).thenReturn(null)

    val method =
        travelRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToTravel", DocumentSnapshot::class.java)
    method.isAccessible = true
    val result = method.invoke(travelRepositoryFirestore, document) as TravelContainer?

    assertNull(result)
    assertNull(result?.fsUid)
    assertNull(result?.title)
    assertNull(result?.description)
    assertNull(result?.startTime)
    assertNull(result?.endTime)
    assertNull(result?.location)
    assertNull(result?.allAttachments)
    assertNull(result?.allParticipants)
  }

  @Test
  fun convertsDocumentToTravelWithInvalidLatitude() {
    val document: DocumentSnapshot = mock()
    whenever(document.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(document.getString("title")).thenReturn("Test Title")
    whenever(document.getString("description")).thenReturn("Test Description")
    whenever(document.getTimestamp("startTime")).thenReturn(Timestamp.now())
    whenever(document.getTimestamp("endTime"))
        .thenReturn(Timestamp(Timestamp.now().seconds + 1000, 0))
    val attachmentsMap = mapOf("Test Key item" to "Test Value item")
    whenever(document.get("allAttachments")).thenReturn(attachmentsMap)

    val participantsMap = mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER)
    whenever(document.get("allParticipants"))
        .thenReturn(participantsMap.map { (key, value) -> key.fsUid to value.name }.toMap())

    val locationMap =
        mapOf(
            "latitude" to null,
            "longitude" to 20.0,
            "name" to "Test Location",
            "insertTime" to Timestamp.now())
    whenever(document.get("location")).thenReturn(locationMap)

    val method =
        travelRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToTravel", DocumentSnapshot::class.java)
    method.isAccessible = true
    val result = method.invoke(travelRepositoryFirestore, document) as TravelContainer?

    assertNotNull(result)
    assertEquals(0.0, result?.location?.latitude)
  }

  @Test
  fun convertsDocumentToTravelWithInvalidLongitude() {
    val document: DocumentSnapshot = mock()
    whenever(document.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(document.getString("title")).thenReturn("Test Title")
    whenever(document.getString("description")).thenReturn("Test Description")
    whenever(document.getTimestamp("startTime")).thenReturn(Timestamp.now())
    whenever(document.getTimestamp("endTime"))
        .thenReturn(Timestamp(Timestamp.now().seconds + 1000, 0))
    val attachmentsMap = mapOf("Test Key item" to "Test Value item")
    whenever(document.get("allAttachments")).thenReturn(attachmentsMap)

    val participantsMap = mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER)
    whenever(document.get("allParticipants"))
        .thenReturn(participantsMap.map { (key, value) -> key.fsUid to value.name }.toMap())

    val locationMap =
        mapOf(
            "latitude" to 10.0,
            "longitude" to null,
            "name" to "Test Location",
            "insertTime" to Timestamp.now())
    whenever(document.get("location")).thenReturn(locationMap)

    val method =
        travelRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToTravel", DocumentSnapshot::class.java)
    method.isAccessible = true
    val result = method.invoke(travelRepositoryFirestore, document) as TravelContainer?

    assertNotNull(result)
    assertEquals(0.0, result?.location?.longitude)
  }

  @Test
  fun convertsDocumentToTravelWithInvalidLocationName() {
    val document: DocumentSnapshot = mock()
    whenever(document.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(document.getString("title")).thenReturn("Test Title")
    whenever(document.getString("description")).thenReturn("Test Description")
    whenever(document.getTimestamp("startTime")).thenReturn(Timestamp.now())
    whenever(document.getTimestamp("endTime"))
        .thenReturn(Timestamp(Timestamp.now().seconds + 1000, 0))
    val attachmentsMap = mapOf("Test Key item" to "Test Value item")
    whenever(document.get("allAttachments")).thenReturn(attachmentsMap)

    val participantsMap = mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER)
    whenever(document.get("allParticipants"))
        .thenReturn(participantsMap.map { (key, value) -> key.fsUid to value.name }.toMap())

    val locationMap =
        mapOf(
            "latitude" to 10.0,
            "longitude" to 20.0,
            "name" to null,
            "insertTime" to Timestamp.now())
    whenever(document.get("location")).thenReturn(locationMap)

    val method =
        travelRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToTravel", DocumentSnapshot::class.java)
    method.isAccessible = true
    val result = method.invoke(travelRepositoryFirestore, document) as TravelContainer?

    assertNull(result)
  }

  @Test
  fun convertsDocumentToTravelWithInvalidInsertTime() {
    val document: DocumentSnapshot = mock()
    whenever(document.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(document.getString("title")).thenReturn("Test Title")
    whenever(document.getString("description")).thenReturn("Test Description")
    whenever(document.getTimestamp("startTime")).thenReturn(Timestamp.now())
    whenever(document.getTimestamp("endTime"))
        .thenReturn(Timestamp(Timestamp.now().seconds + 1000, 0))
    val attachmentsMap = mapOf("Test Key item" to "Test Value item")
    whenever(document.get("allAttachments")).thenReturn(attachmentsMap)

    val participantsMap = mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER)
    whenever(document.get("allParticipants"))
        .thenReturn(participantsMap.map { (key, value) -> key.fsUid to value.name }.toMap())

    val locationMap =
        mapOf(
            "latitude" to 10.0,
            "longitude" to 20.0,
            "name" to "Test Location",
            "insertTime" to null)
    whenever(document.get("location")).thenReturn(locationMap)

    val method =
        travelRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToTravel", DocumentSnapshot::class.java)
    method.isAccessible = true
    val result = method.invoke(travelRepositoryFirestore, document) as TravelContainer?

    assertNotNull(result)
    assertEquals(Timestamp(0, 0), result?.location?.insertTime)
  }

  @Test
  fun callsOnFailureWhenFirestoreOperationFails() {
    val task: Task<Void> = mock()
    val exception = Exception("Firestore error")
    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(exception)

    var failureCalled = false
    val method =
        travelRepositoryFirestore::class
            .java
            .getDeclaredMethod(
                "performFirestoreOperation",
                Task::class.java,
                Function0::class.java,
                Function1::class.java)
    method.isAccessible = true

    val onSuccess: () -> Unit = { fail("Should not call onSuccess") }
    val onFailure: (Exception) -> Unit = { failureCalled = true }

    method.invoke(travelRepositoryFirestore, task, onSuccess, onFailure)

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(failureCalled)
  }

  @Test
  fun getTravels_taskResultDocumentsIsNull() {
    val task: Task<QuerySnapshot> = mock()
    whenever(mockFirestore.collection("travels").get()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.result).thenReturn(null)

    var successCalled = false
    travelRepositoryFirestore.getTravels(
        { successCalled = true }, { fail("Should not call onFailure") })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(successCalled)
  }

  @Test
  fun getTravels_taskResultDocumentsIsEmpty() {
    val task: Task<QuerySnapshot> = mock()
    val querySnapshot: QuerySnapshot = mock()

    whenever(mockFirestore.collection("travels").get()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.result).thenReturn(querySnapshot)
    whenever(querySnapshot.documents).thenReturn(emptyList())

    var successCalled = false
    travelRepositoryFirestore.getTravels(
        { successCalled = true }, { fail("Should not call onFailure") })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(successCalled)
  }

  @Test
  fun getTravels_taskExceptionIsNotNull() {
    val task: Task<QuerySnapshot> = mock()
    val exception = Exception("Firestore error")

    whenever(mockFirestore.collection("travels").get()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(exception)

    mockStatic(Log::class.java).use { logMock ->
      var failureCalled = false
      travelRepositoryFirestore.getTravels(
          { fail("Should not call onSuccess") }, { failureCalled = true })

      val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
      verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
      onCompleteListenerCaptor.firstValue.onComplete(task)

      assertTrue(failureCalled)
      logMock.verify { Log.e("TravelRepositoryFirestore", "Error getting documents", exception) }
    }
  }

  //  @Test
  //  fun testDocumentToUserInfo() {
  //    val document: DocumentSnapshot = mock()
  //    val fsUid = generateAutoId()
  //    whenever(document.id).thenReturn(fsUid)
  //    whenever(document.getString("fsUid")).thenReturn(fsUid)
  //    whenever(document.getString("name")).thenReturn("Test User")
  //    whenever(document.get("listoftravellinked")).thenReturn(listOf(fsUid, fsUid))
  //    whenever(document.getString("email")).thenReturn("testuser@example.com")
  //
  //    val method =
  //        travelRepositoryFirestore::class
  //            .java
  //            .getDeclaredMethod("documentToUserInfo", DocumentSnapshot::class.java)
  //    method.isAccessible = true
  //
  //    val result = method.invoke(travelRepositoryFirestore, document) as UserInfo?
  //
  //    assertNotNull(result)
  //    assertEquals(fsUid, result?.fsUid)
  //    assertEquals("Test User", result?.name)
  //    assertEquals(listOf(fsUid, fsUid), result?.userTravelList)
  //    assertEquals("testuser@example.com", result?.email)
  //  }
}
