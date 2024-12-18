// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.travel

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.github.se.travelpouch.model.FirebasePaths
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.TravelContainerMock
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.model.travels.TravelRepositoryFirestore
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TravelRepositoryFirestoreUnitTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot

  @Mock private lateinit var mockDocumentSnapshotNotInitialised: DocumentSnapshot

  @Mock private lateinit var mockCollectionReference: CollectionReference

  @Mock private lateinit var mockQuery: Query

  @Mock private lateinit var mockDocumentReference: DocumentReference

  private lateinit var travelRepositoryFirestore: TravelRepositoryFirestore

  private lateinit var mockTaskQuerySnapshot: Task<QuerySnapshot>

  private lateinit var mockQuerySnapshot: QuerySnapshot

  private val profile =
      Profile(
          "qwertzuiopasdfghjklyxcvbnm12",
          "username",
          "email@test.ch",
          emptyMap(),
          "name",
          emptyList(),
          false)

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
          mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER),
          listOf("uid"))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    travelRepositoryFirestore = TravelRepositoryFirestore(mockFirestore)
    mockTaskQuerySnapshot = mock()
    mockQuerySnapshot = mock()
    mockDocumentReference = mock()
    mockDocumentSnapshotNotInitialised = mock()

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.whereArrayContains(eq("listParticipant"), anyOrNull()))
        .thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockTaskQuerySnapshot)
    `when`(mockCollectionReference.document(anyOrNull())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(null))
    `when`(mockCollectionReference.document().id).thenReturn("newUid123", "newUid456")
  }

  @Test
  fun generatesNewUidSuccessfully() {
    //    val newUid = "newUid123"
    //    whenever(mockFirestore.collection("travels").document().id).thenReturn(newUid)

    val result = travelRepositoryFirestore.getNewUid()

    assertEquals("newUid123", result)
  }

  @Test
  fun generatesDifferentUidsOnSubsequentCalls() {
    //    val newUid1 = "newUid123"
    //    val newUid2 = "newUid456"
    //    whenever(mockFirestore.collection("allTravels").document().id).thenReturn(newUid1,
    // newUid2)

    val result1 = travelRepositoryFirestore.getNewUid()
    val result2 = travelRepositoryFirestore.getNewUid()

    assertNotNull(result1)
    assertNotNull(result2)
    assertNotEquals(result1, result2)
  }

  @Test
  fun addsTravelSuccessfully() {
    //    val task: Task<Void> = mock()
    //
    // whenever(mockFirestore.collection("allTravels").document(travel.fsUid).set(travel.toMap()))
    //        .thenReturn(task)
    //    whenever(task.isSuccessful).thenReturn(true)
    //
    //    var successCalled = false
    //    travelRepositoryFirestore.addTravel(
    //        travel, { successCalled = true }, { fail("Should not call onFailure") })
    //
    //    // Simulate task completion
    //    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    //    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    //    onCompleteListenerCaptor.firstValue.onComplete(task)
    //
    //    assertTrue(successCalled)
    val travel = TravelContainerMock.createMockTravelContainer(listParticipant = emptyList())

    val firestoreMock: FirebaseFirestore = mock()
    val travelCollectionMock: CollectionReference = mock()
    val profileCollectionMock: CollectionReference = mock()

    val travelDocumentMock: DocumentReference = mock()
    val profileDocumentMock: DocumentReference = mock()
    val eventDocumentReference: DocumentReference = mock()

    val transaction: Transaction = mock()
    val task: Task<Void> = mock()
    val function: Transaction.Function<Void> = mock()

    val travelRepository: TravelRepository = TravelRepositoryFirestore(firestoreMock)
    whenever(firestoreMock.collection(eq(FirebasePaths.TravelsSuperCollection)))
        .thenReturn(travelCollectionMock)
    whenever(firestoreMock.collection(eq(FirebasePaths.ProfilesSuperCollection)))
        .thenReturn(profileCollectionMock)

    whenever(travelCollectionMock.document(anyOrNull())).thenReturn(travelDocumentMock)
    whenever(profileCollectionMock.document(anyOrNull())).thenReturn(profileDocumentMock)
    whenever(eventDocumentReference.id).thenReturn("qwertzuiopasdfghjkly")

    whenever(firestoreMock.runTransaction<Void>(anyOrNull())).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.addOnSuccessListener(anyOrNull())).thenReturn(task)
    whenever(task.addOnFailureListener(anyOrNull())).thenReturn(task)

    var succeeded = false
    var failed = false

    travelRepository.addTravel(
        travel, { succeeded = true }, { failed = true }, eventDocumentReference)

    val profileDocumentSnapShot: DocumentSnapshot = mock()
    whenever(transaction.get(anyOrNull())).thenReturn(profileDocumentSnapShot)
    whenever(profileDocumentSnapShot.get("friends")).thenReturn(emptyMap<String, String>())
    whenever(transaction.set(anyOrNull(), anyOrNull())).thenReturn(transaction)
    whenever(transaction.update(anyOrNull(), anyOrNull())).thenReturn(transaction)

    val transactionCaptor = argumentCaptor<Transaction.Function<Void>>()
    verify(firestoreMock).runTransaction(transactionCaptor.capture())
    transactionCaptor.firstValue.apply(transaction)

    verify(transaction, times(2)).set(anyOrNull(), anyOrNull())
    verify(transaction).update(anyOrNull(), eq("userTravelList"), anyOrNull())
    verify(transaction).get(anyOrNull())

    val onSuccessListenerCaptor = argumentCaptor<OnSuccessListener<Void>>()
    verify(task).addOnSuccessListener(onSuccessListenerCaptor.capture())
    onSuccessListenerCaptor.firstValue.onSuccess(task.result)

    assertTrue(succeeded)
    assertFalse(failed)
  }

  @Test
  fun failsToAddTravel() {
    val travel = TravelContainerMock.createMockTravelContainer(listParticipant = emptyList())

    val firestoreMock: FirebaseFirestore = mock()
    val travelCollectionMock: CollectionReference = mock()
    val profileCollectionMock: CollectionReference = mock()

    val travelDocumentMock: DocumentReference = mock()
    val profileDocumentMock: DocumentReference = mock()
    val eventDocumentReference: DocumentReference = mock()

    val transaction: Transaction = mock()
    val task: Task<Void> = mock()

    val travelRepository: TravelRepository = TravelRepositoryFirestore(firestoreMock)
    whenever(firestoreMock.collection(eq(FirebasePaths.TravelsSuperCollection)))
        .thenReturn(travelCollectionMock)
    whenever(firestoreMock.collection(eq(FirebasePaths.ProfilesSuperCollection)))
        .thenReturn(profileCollectionMock)

    whenever(travelCollectionMock.document(anyOrNull())).thenReturn(travelDocumentMock)
    whenever(profileCollectionMock.document(anyOrNull())).thenReturn(profileDocumentMock)
    whenever(eventDocumentReference.id).thenReturn("qwertzuiopasdfghjkly")

    whenever(firestoreMock.runTransaction<Void>(anyOrNull())).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(Exception("message"))
    whenever(task.addOnSuccessListener(anyOrNull())).thenReturn(task)
    whenever(task.addOnFailureListener(anyOrNull())).thenReturn(task)

    var succeeded = false
    var failed = false

    travelRepository.addTravel(
        travel, { succeeded = true }, { failed = true }, eventDocumentReference)

    val onFailureListenerCaptor = argumentCaptor<OnFailureListener>()
    verify(task).addOnFailureListener(onFailureListenerCaptor.capture())
    onFailureListenerCaptor.firstValue.onFailure(task.exception!!)

    assertTrue(failed)
    assertFalse(succeeded)
  }

  @Test
  fun updatesTravelFieldsSuccessfully() {
    val task: Task<Void> = mock()
    whenever(mockFirestore.collection("allTravels").document(travel.fsUid).set(travel.toMap()))
        .thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)

    var successCalled = false
    travelRepositoryFirestore.updateTravel(
        travel,
        TravelRepository.UpdateMode.FIELDS_UPDATE,
        null,
        { successCalled = true },
        { fail("Should not call onFailure") },
        null)

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(successCalled)
  }

  @Test
  fun updatesAddingAUserSuccessfully() {
    val friendProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm16",
            "usernameFriend",
            "email@friend.ch",
            emptyMap(),
            "nameFriend",
            emptyList())

    val task: Task<Void> = mock()

    val firestoreMock: FirebaseFirestore = mock()
    val travelRepository: TravelRepository = TravelRepositoryFirestore(firestoreMock)
    val transactionMock: Transaction = mock()

    val travelCollectionReference: CollectionReference = mock()
    val profileCollectionReference: CollectionReference = mock()

    val travelDocumentReference: DocumentReference = mock()
    val profileDocumentReference: DocumentReference = mock()
    val eventDocumentReference: DocumentReference = mock()

    whenever(eventDocumentReference.id).thenReturn("qwertzuiopasdfghjklb")

    whenever(firestoreMock.collection(eq(FirebasePaths.TravelsSuperCollection)))
        .thenReturn(travelCollectionReference)
    whenever(firestoreMock.collection(eq(FirebasePaths.ProfilesSuperCollection)))
        .thenReturn(profileCollectionReference)

    whenever(travelCollectionReference.document(anyOrNull())).thenReturn(travelDocumentReference)
    whenever(profileCollectionReference.document(anyOrNull())).thenReturn(profileDocumentReference)
    whenever(firestoreMock.runTransaction<Void>(anyOrNull())).thenReturn(task)

    val mockDocumentSnapshotFriend: DocumentSnapshot = mock()
    whenever(transactionMock.get(anyOrNull())).thenReturn(mockDocumentSnapshotFriend)
    whenever(transactionMock.update(any(), eq("userTravelList"), anyOrNull()))
        .thenReturn(transactionMock)
    whenever(transactionMock.set(anyOrNull(), anyOrNull())).thenReturn(transactionMock)

    `when`(mockDocumentSnapshotFriend.id).thenReturn(friendProfile.fsUid)
    `when`(mockDocumentSnapshotFriend.getString("email")).thenReturn(friendProfile.email)
    `when`(mockDocumentSnapshotFriend.getString("name")).thenReturn(friendProfile.name)
    `when`(mockDocumentSnapshotFriend.getString("username")).thenReturn(friendProfile.username)
    `when`(mockDocumentSnapshotFriend.get("userTravelList"))
        .thenReturn(friendProfile.userTravelList)
    `when`(mockDocumentSnapshotFriend.get("friends")).thenReturn(friendProfile.friends)

    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.addOnSuccessListener(anyOrNull())).thenReturn(task)
    whenever(task.addOnFailureListener(anyOrNull())).thenReturn(task)

    var succeeded = false
    var failed = false

    travelRepository.updateTravel(
        TravelContainerMock.createMockTravelContainer(listParticipant = emptyList()),
        TravelRepository.UpdateMode.ADD_PARTICIPANT,
        friendProfile.fsUid,
        { succeeded = true },
        { failed = true },
        eventDocumentReference = eventDocumentReference)

    val onSuccessListenerCaptor = argumentCaptor<OnSuccessListener<Void>>()
    verify(task).addOnSuccessListener(onSuccessListenerCaptor.capture())
    onSuccessListenerCaptor.firstValue.onSuccess(task.result)

    val transactionCaptor = argumentCaptor<Transaction.Function<Void>>()
    verify(firestoreMock).runTransaction(transactionCaptor.capture())
    transactionCaptor.firstValue.apply(transactionMock)

    verify(transactionMock).update(anyOrNull(), eq("userTravelList"), anyOrNull())
    verify(transactionMock, times(2)).set(anyOrNull(), anyOrNull())
    verify(transactionMock).get(anyOrNull())

    assertTrue(succeeded)
    assertFalse(failed)
  }

  @Test
  fun updatesRemovingAUserSuccessfully() {
    val task: Task<Void> = mock()

    val friendProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm16",
            "usernameFriend",
            "email@friend.ch",
            emptyMap(),
            "nameFriend",
            emptyList())

    val mockDocumentSnapshotFriend: DocumentSnapshot = mock()

    `when`(mockDocumentSnapshotFriend.id).thenReturn(friendProfile.fsUid)
    `when`(mockDocumentSnapshotFriend.getString("email")).thenReturn(friendProfile.email)
    `when`(mockDocumentSnapshotFriend.getString("name")).thenReturn(friendProfile.name)
    `when`(mockDocumentSnapshotFriend.getString("username")).thenReturn(friendProfile.username)
    `when`(mockDocumentSnapshotFriend.get("userTravelList"))
        .thenReturn(friendProfile.userTravelList)
    `when`(mockDocumentSnapshotFriend.get("friends")).thenReturn(friendProfile.friends)

    val firestoreMock: FirebaseFirestore = mock()
    val travelRepository: TravelRepository = TravelRepositoryFirestore(firestoreMock)
    val transactionTask: Task<Void> = mock()
    val transaction: Transaction = mock()

    whenever(transaction.set(any(), any())).thenReturn(transaction)
    whenever(transaction.update(any(), eq("userTravelList"), any())).thenReturn(transaction)
    whenever(transaction.get(any())).thenReturn(mockDocumentSnapshotFriend)

    val travelCollectionReference: CollectionReference = mock()
    val profileCollectionReference: CollectionReference = mock()

    val travelDocumentReference: DocumentReference = mock()
    val profileDocumentReference: DocumentReference = mock()
    val eventDocumentReference: DocumentReference = mock()
    whenever(eventDocumentReference.id).thenReturn("qwertzuioplkjhgfdsax")

    whenever(firestoreMock.collection(eq(FirebasePaths.TravelsSuperCollection)))
        .thenReturn(travelCollectionReference)
    whenever(firestoreMock.collection(eq(FirebasePaths.ProfilesSuperCollection)))
        .thenReturn(profileCollectionReference)

    whenever(travelCollectionReference.document(anyOrNull())).thenReturn(travelDocumentReference)
    whenever(profileCollectionReference.document(anyOrNull())).thenReturn(profileDocumentReference)
    whenever(firestoreMock.runTransaction<Void>(anyOrNull())).thenReturn(task)

    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.addOnSuccessListener(anyOrNull())).thenReturn(task)
    whenever(task.addOnFailureListener(anyOrNull())).thenReturn(task)

    var succeeded = false
    var failed = false

    travelRepository.updateTravel(
        TravelContainerMock.createMockTravelContainer(listParticipant = emptyList()),
        TravelRepository.UpdateMode.REMOVE_PARTICIPANT,
        "user",
        { succeeded = true },
        { failed = true },
        eventDocumentReference)

    val onSuccessListenerCaptor = argumentCaptor<OnSuccessListener<Void>>()
    verify(task).addOnSuccessListener(onSuccessListenerCaptor.capture())
    onSuccessListenerCaptor.firstValue.onSuccess(task.result)

    val transactionCaptor = argumentCaptor<Transaction.Function<Void>>()
    verify(firestoreMock).runTransaction(transactionCaptor.capture())
    transactionCaptor.firstValue.apply(transaction)

    verify(transaction, times(2)).set(any(), any())
    verify(transaction).update(any(), eq("userTravelList"), any())
    verify(transaction).get(any())

    assertTrue(succeeded)
    assertFalse(failed)
  }

  @Test
  fun failsToUpdateTravel() {
    val task: Task<Void> = mock()
    whenever(mockFirestore.collection("allTravels").document(travel.fsUid).set(travel.toMap()))
        .thenReturn(task)
    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(Exception("Firestore error"))

    var failureCalled = false
    travelRepositoryFirestore.updateTravel(
        travel,
        TravelRepository.UpdateMode.FIELDS_UPDATE,
        null,
        { fail("Should not call onSuccess") },
        { failureCalled = true },
        null)

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(failureCalled)
  }

  @Test
  fun deletesTravelByIdSuccessfully() {
    val task: Task<Void> = mock()
    val transaction: Transaction
    whenever(mockFirestore.runTransaction<Void>(anyOrNull())).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)

    whenever(task.addOnSuccessListener(anyOrNull())).thenReturn(task)
    whenever(task.addOnFailureListener(anyOrNull())).thenReturn(task)

    var successCalled = false
    travelRepositoryFirestore.deleteTravelById(
        travel.fsUid, { successCalled = true }, { fail("Should not call onFailure") })

    // Simulate task completion
    val onSuccessListenerCaptor = argumentCaptor<OnSuccessListener<Void>>()
    verify(task).addOnSuccessListener(onSuccessListenerCaptor.capture())
    onSuccessListenerCaptor.firstValue.onSuccess(task.result)

    assertTrue(successCalled)
  }

  @Test
  fun failsToDeleteTravelById() {
    val task: Task<Void> = mock()
    whenever(mockFirestore.runTransaction<Void>(anyOrNull())).thenReturn(task)

    whenever(task.addOnSuccessListener(anyOrNull())).thenReturn(task)
    whenever(task.addOnFailureListener(anyOrNull())).thenReturn(task)

    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(Exception("Firestore error"))

    var failureCalled = false
    travelRepositoryFirestore.deleteTravelById(
        travel.fsUid, { fail("Should not call onSuccess") }, { failureCalled = true })

    // Simulate task completion
    val onFailureListenerCaptor = argumentCaptor<OnFailureListener>()
    verify(task).addOnFailureListener(onFailureListenerCaptor.capture())
    onFailureListenerCaptor.firstValue.onFailure(task.exception!!)

    assertTrue(failureCalled)
  }

  @Test
  fun retrievesTravelsSuccessfully() {
    val task = mock<Task<QuerySnapshot>>()
    val querySnapshot = mock<QuerySnapshot>()

    `when`(mockTaskQuerySnapshot.result).thenReturn(mockQuerySnapshot)
    `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(true)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    whenever(mockDocumentSnapshot.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(mockDocumentSnapshot.getString("title")).thenReturn("Test Title")
    whenever(mockDocumentSnapshot.getString("description")).thenReturn("Test Description")
    whenever(mockDocumentSnapshot.getTimestamp("startTime")).thenReturn(Timestamp.now())
    whenever(mockDocumentSnapshot.getTimestamp("endTime"))
        .thenReturn(Timestamp(Timestamp.now().seconds + 1000, 0))
    val attachmentsMap = mapOf("Test Key item" to "Test Value item")
    whenever(mockDocumentSnapshot.get("allAttachments")).thenReturn(attachmentsMap)

    val participantsMap = mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER)
    whenever(mockDocumentSnapshot.get("allParticipants"))
        .thenReturn(participantsMap.map { (key, value) -> key.fsUid to value.name }.toMap())

    val locationMap =
        mapOf(
            "latitude" to null,
            "longitude" to 20.0,
            "name" to "Test Location",
            "insertTime" to Timestamp.now())
    whenever(mockDocumentSnapshot.get("location")).thenReturn(locationMap)
    whenever(mockDocumentSnapshot.get("listParticipant")).thenReturn(listOf("uid"))

    //    whenever(mockFirestore.collection("travels").get()).thenReturn(task)
    //    whenever(task.isSuccessful).thenReturn(true)
    //    whenever(task.result).thenReturn(querySnapshot)
    //    whenever(querySnapshot.documents).thenReturn(listOf(mock()))

    var successCalled = false
    travelRepositoryFirestore.getTravels(
        { successCalled = true }, { fail("Should not call onFailure") })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(mockTaskQuerySnapshot).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(mockTaskQuerySnapshot)

    assertTrue(successCalled)
  }

  @Test
  fun noTravelDocumentsRetrieved() {

    `when`(mockTaskQuerySnapshot.result).thenReturn(mockQuerySnapshot)
    `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(true)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf())

    val task: Task<QuerySnapshot> = mock()
    val querySnapshot: QuerySnapshot = mock()

    //    whenever(mockFirestore.collection("travels").get()).thenReturn(task)
    //    whenever(task.isSuccessful).thenReturn(true)
    //    whenever(task.result).thenReturn(querySnapshot)
    //    whenever(querySnapshot.documents).thenReturn(emptyList())

    var successCalled = false
    travelRepositoryFirestore.getTravels(
        { successCalled = true }, { fail("Should not call onFailure") })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(mockTaskQuerySnapshot).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(mockTaskQuerySnapshot)

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

    `when`(mockTaskQuerySnapshot.result).thenReturn(mockQuerySnapshot)
    `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(true)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    whenever(mockDocumentSnapshot.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(mockDocumentSnapshot.getString("title")).thenReturn("Test Title")
    whenever(mockDocumentSnapshot.getString("description")).thenReturn("Test Description")
    whenever(mockDocumentSnapshot.getTimestamp("startTime")).thenReturn(Timestamp.now())
    whenever(mockDocumentSnapshot.getTimestamp("endTime"))
        .thenReturn(Timestamp(Timestamp.now().seconds + 1000, 0))
    val attachmentsMap = mapOf("Test Key item" to "Test Value item")
    whenever(mockDocumentSnapshot.get("allAttachments")).thenReturn(attachmentsMap)

    val participantsMap = mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER)
    whenever(mockDocumentSnapshot.get("allParticipants"))
        .thenReturn(participantsMap.map { (key, value) -> key.fsUid to value.name }.toMap())

    val locationMap =
        mapOf(
            "latitude" to 0.0,
            "longitude" to 0.0,
            "name" to "Test Location",
            "insertTime" to Timestamp.now())
    whenever(mockDocumentSnapshot.get("location")).thenReturn(locationMap)
    whenever(mockDocumentSnapshot.get("listParticipant")).thenReturn(listOf("uid"))

    val method =
        travelRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToTravel", DocumentSnapshot::class.java)
    method.isAccessible = true

    val result = method.invoke(travelRepositoryFirestore, mockDocumentSnapshot) as TravelContainer?
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
    assertEquals(listOf("uid"), result?.listParticipant)

    // Mock the static Log class
    mockStatic(Log::class.java).use { logMock ->
      var successCalled = false
      travelRepositoryFirestore.getTravels(
          { successCalled = true }, { fail("Should not call onFailure") })

      val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
      verify(mockTaskQuerySnapshot).addOnCompleteListener(onCompleteListenerCaptor.capture())
      onCompleteListenerCaptor.firstValue.onComplete(mockTaskQuerySnapshot)

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
    whenever(document.get("listParticipant")).thenReturn(null)

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
    whenever(document.get("listParticipant")).thenReturn(listOf("uid"))

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
    whenever(document.get("listParticipant")).thenReturn(listOf("uid"))

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
    whenever(document.get("listParticipant")).thenReturn(listOf("uid"))

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
    whenever(document.get("listParticipant")).thenReturn(listOf("uid"))

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

    `when`(mockTaskQuerySnapshot.result).thenReturn(null)
    `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(true)

    //    val task: Task<QuerySnapshot> = mock()
    //    whenever(mockFirestore.collection("travels").get()).thenReturn(task)
    //    whenever(task.isSuccessful).thenReturn(true)
    //    whenever(task.result).thenReturn(null)

    var successCalled = false
    travelRepositoryFirestore.getTravels(
        { successCalled = true }, { fail("Should not call onFailure") })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(mockTaskQuerySnapshot).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(mockTaskQuerySnapshot)

    assertTrue(successCalled)
  }

  @Test
  fun getTravels_taskResultDocumentsIsEmpty() {
    `when`(mockTaskQuerySnapshot.result).thenReturn(mockQuerySnapshot)
    `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(true)
    `when`(mockQuerySnapshot.documents).thenReturn(emptyList())

    //    val task: Task<QuerySnapshot> = mock()
    //    val querySnapshot: QuerySnapshot = mock()
    //
    //    whenever(mockFirestore.collection("travels").get()).thenReturn(task)
    //    whenever(task.isSuccessful).thenReturn(true)
    //    whenever(task.result).thenReturn(querySnapshot)
    //    whenever(querySnapshot.documents).thenReturn(emptyList())

    var successCalled = false
    travelRepositoryFirestore.getTravels(
        { successCalled = true }, { fail("Should not call onFailure") })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(mockTaskQuerySnapshot).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(mockTaskQuerySnapshot)

    assertTrue(successCalled)
  }

  @Test
  fun getTravels_taskExceptionIsNotNull() {

    val exception = Exception("Firestore error")
    `when`(mockTaskQuerySnapshot.result).thenReturn(mockQuerySnapshot)
    `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(false)
    `when`(mockTaskQuerySnapshot.exception).thenReturn(exception)

    //    val task: Task<QuerySnapshot> = mock()
    //    val exception = Exception("Firestore error")
    //
    //    //whenever(mockFirestore.collection("travels").get()).thenReturn(task)
    //    whenever(task.isSuccessful).thenReturn(false)
    //    whenever(task.exception).thenReturn(exception)

    mockStatic(Log::class.java).use { logMock ->
      var failureCalled = false
      travelRepositoryFirestore.getTravels(
          { fail("Should not call onSuccess") }, { failureCalled = true })

      val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
      verify(mockTaskQuerySnapshot).addOnCompleteListener(onCompleteListenerCaptor.capture())
      onCompleteListenerCaptor.firstValue.onComplete(mockTaskQuerySnapshot)

      assertTrue(failureCalled)
      logMock.verify { Log.e("TravelRepositoryFirestore", "Error getting documents", exception) }
    }
  }

  @Test
  fun getParticipantFromUidTest() {
    val task: Task<DocumentSnapshot> = mock()
    val documentReferenceProfile: DocumentReference = mock()
    val documentSnapshot: DocumentSnapshot = mock()

    val mockFirestoreBis: FirebaseFirestore = mock()
    val travelRepositoryFirestoreBis = TravelRepositoryFirestore(mockFirestoreBis)
    val collectionReferenceProfiles: CollectionReference = mock()

    `when`(mockFirestoreBis.collection(anyOrNull())).thenReturn(collectionReferenceProfiles)
    `when`(collectionReferenceProfiles.document(anyOrNull())).thenReturn(documentReferenceProfile)
    `when`(documentReferenceProfile.get()).thenReturn(task)

    `when`(documentSnapshot.id).thenReturn(profile.fsUid)
    `when`(documentSnapshot.getString("email")).thenReturn(profile.email)
    `when`(documentSnapshot.getString("name")).thenReturn(profile.name)
    `when`(documentSnapshot.getString("username")).thenReturn(profile.username)
    `when`(documentSnapshot.get("userTravelList")).thenReturn(profile.userTravelList)
    `when`(documentSnapshot.get("friends")).thenReturn(profile.friends)

    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.result).thenReturn(documentSnapshot)

    var profileGot: Profile? = null
    travelRepositoryFirestoreBis.getParticipantFromfsUid(
        profile.fsUid, { profileGot = it }, { fail("Should not call onFailure") })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<DocumentSnapshot>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertEquals(profileGot, profile)
  }

  @Test
  fun checkParticipantExistsSucceeds() {
    val mockFirestoreBis: FirebaseFirestore = mock()
    val travelRepositoryFirestoreBis = TravelRepositoryFirestore(mockFirestoreBis)
    val collectionReferenceProfiles: CollectionReference = mock()
    val queryProfiles: Query = mock()

    val task: Task<QuerySnapshot> = mock()
    val query: QuerySnapshot = mock()
    val documentSnapshot: DocumentSnapshot = mock()

    `when`(mockFirestoreBis.collection(anyOrNull())).thenReturn(collectionReferenceProfiles)
    `when`(collectionReferenceProfiles.whereEqualTo(eq("email"), anyOrNull()))
        .thenReturn(queryProfiles)
    `when`(queryProfiles.get()).thenReturn(task)

    `when`(documentSnapshot.id).thenReturn(profile.fsUid)
    `when`(documentSnapshot.getString("email")).thenReturn(profile.email)
    `when`(documentSnapshot.getString("name")).thenReturn(profile.name)
    `when`(documentSnapshot.getString("username")).thenReturn(profile.username)
    `when`(documentSnapshot.get("userTravelList")).thenReturn(profile.userTravelList)
    `when`(documentSnapshot.get("friends")).thenReturn(profile.friends)

    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.result).thenReturn(query)
    whenever(query.documents).thenReturn(listOf(documentSnapshot))
    whenever(query.isEmpty).thenReturn(false)

    var profileGot: Profile? = null
    travelRepositoryFirestoreBis.checkParticipantExists(
        profile.email, { profileGot = it }, { fail("Should not call onFailure") })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertEquals(profileGot, profile)
  }

  @Test
  fun checkParticipantExistsFailsWithException() {
    val mockFirestoreBis: FirebaseFirestore = mock()
    val travelRepositoryFirestoreBis = TravelRepositoryFirestore(mockFirestoreBis)
    val collectionReferenceProfiles: CollectionReference = mock()
    val queryProfiles: Query = mock()

    val task: Task<QuerySnapshot> = mock()
    val query: QuerySnapshot = mock()
    val documentSnapshot: DocumentSnapshot = mock()

    `when`(mockFirestoreBis.collection(anyOrNull())).thenReturn(collectionReferenceProfiles)
    `when`(collectionReferenceProfiles.whereEqualTo(eq("email"), anyOrNull()))
        .thenReturn(queryProfiles)
    `when`(queryProfiles.get()).thenReturn(task)

    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(Exception("message"))

    var profileGot: Profile? = null
    var failed = false
    travelRepositoryFirestoreBis.checkParticipantExists(
        profile.email, { profileGot = it }, { failed = true })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertEquals(profileGot, null)
    assertEquals(failed, true)
  }

  @Test
  fun checkParticipantExistsFailsWithEmptyList() {
    val mockFirestoreBis: FirebaseFirestore = mock()
    val travelRepositoryFirestoreBis = TravelRepositoryFirestore(mockFirestoreBis)
    val collectionReferenceProfiles: CollectionReference = mock()
    val queryProfiles: Query = mock()

    val task: Task<QuerySnapshot> = mock()
    val query: QuerySnapshot = mock()

    `when`(mockFirestoreBis.collection(anyOrNull())).thenReturn(collectionReferenceProfiles)
    `when`(collectionReferenceProfiles.whereEqualTo(eq("email"), anyOrNull()))
        .thenReturn(queryProfiles)
    `when`(queryProfiles.get()).thenReturn(task)

    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.result).thenReturn(query)
    whenever(query.documents).thenReturn(emptyList())
    whenever(query.isEmpty).thenReturn(true)

    var profileGot: Profile? = profile
    travelRepositoryFirestoreBis.checkParticipantExists(
        profile.email, { profileGot = it }, { fail("Should not call onFailure") })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertEquals(profileGot, null)
  }

  @Test
  fun checkParticipantExistsFailsOnListWithMoreThanOneElement() {
    val mockFirestoreBis: FirebaseFirestore = mock()
    val travelRepositoryFirestoreBis = TravelRepositoryFirestore(mockFirestoreBis)
    val collectionReferenceProfiles: CollectionReference = mock()
    val queryProfiles: Query = mock()

    val task: Task<QuerySnapshot> = mock()
    val query: QuerySnapshot = mock()
    val documentSnapshot: DocumentSnapshot = mock()
    val secondDocumentSnapshot: DocumentSnapshot = mock()

    `when`(mockFirestoreBis.collection(anyOrNull())).thenReturn(collectionReferenceProfiles)
    `when`(collectionReferenceProfiles.whereEqualTo(eq("email"), anyOrNull()))
        .thenReturn(queryProfiles)
    `when`(queryProfiles.get()).thenReturn(task)

    `when`(documentSnapshot.id).thenReturn(profile.fsUid)
    `when`(documentSnapshot.getString("email")).thenReturn(profile.email)
    `when`(documentSnapshot.getString("name")).thenReturn(profile.name)
    `when`(documentSnapshot.getString("username")).thenReturn(profile.username)
    `when`(documentSnapshot.get("userTravelList")).thenReturn(profile.userTravelList)
    `when`(documentSnapshot.get("friends")).thenReturn(profile.friends)

    `when`(secondDocumentSnapshot.id).thenReturn("failed")
    `when`(secondDocumentSnapshot.getString("email")).thenReturn("email")
    `when`(secondDocumentSnapshot.getString("name")).thenReturn("name")
    `when`(secondDocumentSnapshot.getString("username")).thenReturn("username")
    `when`(secondDocumentSnapshot.get("userTravelList")).thenReturn(emptyList<String>())
    `when`(secondDocumentSnapshot.get("friends")).thenReturn(null)

    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.result).thenReturn(query)
    whenever(query.documents).thenReturn(listOf(documentSnapshot, secondDocumentSnapshot))
    whenever(query.isEmpty).thenReturn(false)

    var profileGot: Profile? = null
    travelRepositoryFirestoreBis.checkParticipantExists(
        profile.email, { profileGot = it }, { fail("Should not call onFailure") })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertEquals(profileGot, profile)
  }

  @Test
  fun getParticipantForFsUidFailsWithException() {
    val mockFirestoreBis: FirebaseFirestore = mock()
    val travelRepositoryFirestoreBis = TravelRepositoryFirestore(mockFirestoreBis)
    val collectionReferenceProfiles: CollectionReference = mock()
    val documentReference: DocumentReference = mock()
    val queryProfiles: Query = mock()

    val task: Task<DocumentSnapshot> = mock()

    `when`(mockFirestoreBis.collection(anyOrNull())).thenReturn(collectionReferenceProfiles)
    `when`(collectionReferenceProfiles.document(anyOrNull())).thenReturn(documentReference)
    `when`(documentReference.get()).thenReturn(task)

    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(Exception("message"))

    var profileGot: Profile? = null
    var failed = false
    travelRepositoryFirestoreBis.getParticipantFromfsUid(
        profile.fsUid, { profileGot = it }, { failed = true })

    // Simulate task completion
    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<DocumentSnapshot>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertEquals(profileGot, null)
    assertEquals(failed, true)
  }

  @Test
  fun getTravelByIdTestSucceedsIfValidDocument() {

    // setting the document
    whenever(mockDocumentSnapshot.id).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    whenever(mockDocumentSnapshot.getString("title")).thenReturn("Test Title")
    whenever(mockDocumentSnapshot.getString("description")).thenReturn("Test Description")
    whenever(mockDocumentSnapshot.getTimestamp("startTime")).thenReturn(Timestamp.now())
    whenever(mockDocumentSnapshot.getTimestamp("endTime"))
        .thenReturn(Timestamp(Timestamp.now().seconds + 1000, 0))
    val attachmentsMap = mapOf("Test Key item" to "Test Value item")
    whenever(mockDocumentSnapshot.get("allAttachments")).thenReturn(attachmentsMap)

    val participantsMap = mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER)
    whenever(mockDocumentSnapshot.get("allParticipants"))
        .thenReturn(participantsMap.map { (key, value) -> key.fsUid to value.name }.toMap())

    val locationMap =
        mapOf(
            "latitude" to null,
            "longitude" to 20.0,
            "name" to "Test Location",
            "insertTime" to Timestamp.now())
    whenever(mockDocumentSnapshot.get("location")).thenReturn(locationMap)
    whenever(mockDocumentSnapshot.get("listParticipant")).thenReturn(listOf("uid"))
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)

    val mockFirestoreBis: FirebaseFirestore = mock()
    val travelRepositoryFirestoreBis = TravelRepositoryFirestore(mockFirestoreBis)
    val mockCollectionReference: CollectionReference = mock()
    val documentReference: DocumentReference = mock()

    val mockTask: Task<DocumentSnapshot> = mock()
    whenever(mockTask.isSuccessful).thenReturn(true)
    whenever(mockTask.result).thenReturn(mockDocumentSnapshot)

    whenever(mockFirestoreBis.collection(anyOrNull())).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.document(anyOrNull())).thenReturn(documentReference)
    whenever(documentReference.get()).thenReturn(mockTask)

    var succeeded = false
    var failed = false

    travelRepositoryFirestoreBis.getTravelById("id", { succeeded = true }, { failed = true })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<DocumentSnapshot>>()
    verify(mockTask).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(mockTask)

    assert(succeeded)
    assertFalse(failed)
  }

  @Test
  fun getTravelByIdTestFailsIfInvalidDocument() {

    // setting the document
    whenever(mockDocumentSnapshot.id).thenReturn(null)
    whenever(mockDocumentSnapshot.getString("title")).thenReturn("Test Title")
    whenever(mockDocumentSnapshot.getString("description")).thenReturn("Test Description")
    whenever(mockDocumentSnapshot.getTimestamp("startTime")).thenReturn(Timestamp.now())
    whenever(mockDocumentSnapshot.getTimestamp("endTime"))
        .thenReturn(Timestamp(Timestamp.now().seconds + 1000, 0))
    val attachmentsMap = mapOf("Test Key item" to "Test Value item")
    whenever(mockDocumentSnapshot.get("allAttachments")).thenReturn(attachmentsMap)

    val participantsMap = mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER)
    whenever(mockDocumentSnapshot.get("allParticipants"))
        .thenReturn(participantsMap.map { (key, value) -> key.fsUid to value.name }.toMap())

    val locationMap =
        mapOf(
            "latitude" to null,
            "longitude" to 20.0,
            "name" to "Test Location",
            "insertTime" to Timestamp.now())
    whenever(mockDocumentSnapshot.get("location")).thenReturn(locationMap)
    whenever(mockDocumentSnapshot.get("listParticipant")).thenReturn(listOf("uid"))
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)

    val mockFirestoreBis: FirebaseFirestore = mock()
    val travelRepositoryFirestoreBis = TravelRepositoryFirestore(mockFirestoreBis)
    val mockCollectionReference: CollectionReference = mock()
    val documentReference: DocumentReference = mock()

    val mockTask: Task<DocumentSnapshot> = mock()
    whenever(mockTask.isSuccessful).thenReturn(true)
    whenever(mockTask.result).thenReturn(mockDocumentSnapshot)

    whenever(mockFirestoreBis.collection(anyOrNull())).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.document(anyOrNull())).thenReturn(documentReference)
    whenever(documentReference.get()).thenReturn(mockTask)

    var succeeded = false
    var failed = false

    travelRepositoryFirestoreBis.getTravelById("id", { succeeded = true }, { failed = true })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<DocumentSnapshot>>()
    verify(mockTask).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(mockTask)

    assert(failed)
    assertFalse(succeeded)
  }

  @Test
  fun getTravelByIdTestFailsIfInexistantDocument() {

    // setting the document
    whenever(mockDocumentSnapshot.exists()).thenReturn(false)

    val mockFirestoreBis: FirebaseFirestore = mock()
    val travelRepositoryFirestoreBis = TravelRepositoryFirestore(mockFirestoreBis)
    val mockCollectionReference: CollectionReference = mock()
    val documentReference: DocumentReference = mock()

    val mockTask: Task<DocumentSnapshot> = mock()
    whenever(mockTask.isSuccessful).thenReturn(true)
    whenever(mockTask.result).thenReturn(mockDocumentSnapshot)

    whenever(mockFirestoreBis.collection(anyOrNull())).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.document(anyOrNull())).thenReturn(documentReference)
    whenever(documentReference.get()).thenReturn(mockTask)

    var succeeded = false
    var failed = false

    travelRepositoryFirestoreBis.getTravelById("id", { succeeded = true }, { failed = true })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<DocumentSnapshot>>()
    verify(mockTask).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(mockTask)

    assert(failed)
    assertFalse(succeeded)
  }

  @Test
  fun getTravelByIdTestFailsIfException() {

    // setting the document

    val mockFirestoreBis: FirebaseFirestore = mock()
    val travelRepositoryFirestoreBis = TravelRepositoryFirestore(mockFirestoreBis)
    val mockCollectionReference: CollectionReference = mock()
    val documentReference: DocumentReference = mock()

    val mockTask: Task<DocumentSnapshot> = mock()
    whenever(mockTask.isSuccessful).thenReturn(false)
    whenever(mockTask.exception).thenReturn(Exception("message"))

    whenever(mockFirestoreBis.collection(anyOrNull())).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.document(anyOrNull())).thenReturn(documentReference)
    whenever(documentReference.get()).thenReturn(mockTask)

    var succeeded = false
    var failed = false

    travelRepositoryFirestoreBis.getTravelById("id", { succeeded = true }, { failed = true })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<DocumentSnapshot>>()
    verify(mockTask).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(mockTask)

    assert(failed)
    assertFalse(succeeded)
  }
}
