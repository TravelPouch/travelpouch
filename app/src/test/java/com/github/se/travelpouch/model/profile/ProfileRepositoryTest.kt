// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.profile

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.fail
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ProfileRepositoryTest {
  private lateinit var mockFirebaseUser: FirebaseUser
  private lateinit var mockFirebaseAuth: FirebaseAuth
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockDocumentSnapshotError: DocumentSnapshot
  @Mock private lateinit var mockFunction: (Profile) -> Unit

  private lateinit var profileRepositoryFirestore: ProfileRepositoryFirebase

  private lateinit var mockTaskDocumentSnapshot: Task<DocumentSnapshot>

  val profile =
      Profile(
          "qwertzuiopasdfghjklyxcvbnm12",
          "usernameTest",
          "email@test.ch",
          emptyMap(),
          "nameTest",
          emptyList(),
          true)

  val newProfile =
      Profile(
          "qwertzuiopasdfghjklyxcvbnm12",
          "usernameTest",
          "email@test.ch",
          mapOf("test@test.ch" to "qwertzuiopasdfghjklyxcvbnm12"),
          "nameTest",
          emptyList(),
          true)

  val function: (Profile) -> Unit = {}

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    profileRepositoryFirestore = ProfileRepositoryFirebase(mockFirestore)
    mockDocumentSnapshot = mock(DocumentSnapshot::class.java)
    mockDocumentSnapshotError = mock(DocumentSnapshot::class.java)
    mockTaskDocumentSnapshot = mock()
    mockFirebaseAuth = mock(FirebaseAuth::class.java)
    mockFirebaseUser = mock(FirebaseUser::class.java)
    mockFunction = mock()

    `when`(mockDocumentSnapshot.id).thenReturn("qwertzuiopasdfghjklyxcvbnm12")
    `when`(mockDocumentSnapshot.getString("email")).thenReturn("email@test.ch")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("nameTest")
    `when`(mockDocumentSnapshot.getString("username")).thenReturn("usernameTest")
    `when`(mockDocumentSnapshot.get("userTravelList")).thenReturn(emptyList<String>())
    `when`(mockDocumentSnapshot.get("friends")).thenReturn(null)
    `when`(mockDocumentSnapshot.getBoolean("needsOnboarding")).thenReturn(true)

    `when`(mockDocumentSnapshotError.id).thenReturn("qwertzuiopasdfghjklyxcvbnm12")
    `when`(mockDocumentSnapshotError.getString("email")).thenReturn(null)
    `when`(mockDocumentSnapshotError.getString("name")).thenReturn("nameTest")
    `when`(mockDocumentSnapshotError.getString("username")).thenReturn("usernameTest")
    `when`(mockDocumentSnapshotError.get("userTravelList")).thenReturn(emptyList<String>())
    `when`(mockDocumentSnapshotError.get("friends")).thenReturn(null)
  }

  @Test
  fun getProfile_callsDocuments() {
    // Ensure that mockToDoQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.document(anyOrNull())).thenReturn(mockDocumentReference)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    // Call the method under test
    profileRepositoryFirestore.getProfileElements(
        onSuccess = {

          // Do nothing; we just want to verify that the 'documents' field was accessed
        },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockTaskDocumentSnapshot) }
  }

  @Test
  fun updateProfile_shouldCallFirestoreCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null)) // Simulate success

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    // This test verifies that when we add a new event, the Firestore `collection()` method is
    // called.
    profileRepositoryFirestore.updateProfile(profile, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    // Ensure Firestore collection method was called to reference the "events" collection
    verify(mockDocumentReference).set(any())
  }

  @Test
  fun addProfile_shouldCallFirestoreCollection() {

    val privateFunc =
        profileRepositoryFirestore.javaClass.getDeclaredMethod(
            "addProfile", String::class.java, String::class.java, Function1::class.java)
    privateFunc.isAccessible = true
    val parameters = arrayOfNulls<Any>(3)
    parameters[0] = "email@test.ch"
    parameters[1] = "qwertzuiopasdfghjklyxcvbnm12"
    parameters[2] = function

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null)) // Simulate success

    // This test verifies that when we add a new event, the Firestore `collection()` method is
    // called.
    privateFunc.invoke(profileRepositoryFirestore, *parameters)

    shadowOf(Looper.getMainLooper()).idle()

    // Ensure Firestore collection method was called to reference the "events" collection
    verify(mockDocumentReference).set(any())
  }

  @Test
  fun documentToProfileTest() {
    val result = ProfileRepositoryConvert.documentToProfile(mockDocumentSnapshot)
    assertThat(result, `is`(profile))
  }

  @Test
  fun addingUserWhenDoesNotExistTest() {
    val privateFunc =
        profileRepositoryFirestore.javaClass.getDeclaredMethod(
            "addingUserIfNotRegistered",
            FirebaseUser::class.java,
            DocumentSnapshot::class.java,
            (Function1::class.java))
    privateFunc.isAccessible = true
    val parameters = arrayOfNulls<Any>(3)
    parameters[0] = mockFirebaseUser
    parameters[1] = mockDocumentSnapshot
    parameters[2] = function

    `when`(mockFirebaseUser.uid).thenReturn("qwertzuiopasdfghjklyxcvbnm12")
    `when`(mockFirebaseUser.email).thenReturn("email@test.ch")

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentReference.set(anyOrNull())).thenReturn(Tasks.forResult(null))

    `when`(mockDocumentSnapshot.exists()).thenReturn(false)
    privateFunc.invoke(profileRepositoryFirestore, *parameters)

    verify(mockDocumentReference).set(anyOrNull())
  }

  @Test
  fun addingUserTest() {
    val privateFunc =
        profileRepositoryFirestore.javaClass.getDeclaredMethod(
            "addingUserIfNotRegistered",
            FirebaseUser::class.java,
            DocumentSnapshot::class.java,
            (Function1::class.java))
    privateFunc.isAccessible = true
    val parameters = arrayOfNulls<Any>(3)

    parameters[0] = mockFirebaseUser
    parameters[1] = mockDocumentSnapshot
    parameters[2] = function

    `when`(mockFirebaseUser.uid).thenReturn("qwertzuiopasdfghjklyxcvbnm12")
    `when`(mockFirebaseUser.email).thenReturn("email@test.ch")

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentReference.set(anyOrNull())).thenReturn(Tasks.forResult(null))

    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
    privateFunc.invoke(profileRepositoryFirestore, *parameters)

    verify(mockDocumentReference, never()).set(anyOrNull())
  }

  @Test
  fun gettingProfileUserCallsTheDocumentReference() = runTest {
    `when`(mockFirebaseUser.uid).thenReturn("qwertzuiopasdfghjklyxcvbnm12")
    `when`(mockFirebaseUser.email).thenReturn("email@test.ch")

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(null))

    profileRepositoryFirestore.gettingUserProfile(mockFirebaseUser, {})
    verify(timeout(1000)) { (mockDocumentReference) }
  }

  @Test
  fun testReturnsErrorProfileIfEmailNull() {
    val result = ProfileRepositoryConvert.documentToProfile(mockDocumentSnapshotError)
    assertThat(result, `is`(ErrorProfile.errorProfile))
  }

  @Test
  fun gettingProfileUserCallsCreatingProfile() = runTest {
    val mockTask: Task<DocumentSnapshot> = mock()
    `when`(mockTask.isComplete).thenReturn(true)
    `when`(mockTask.isSuccessful).thenReturn(true)
    `when`(mockTask.result).thenReturn(mockDocumentSnapshot)

    `when`(mockFirebaseUser.uid).thenReturn("uid")
    `when`(mockFirebaseUser.email).thenReturn("email")

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(mockTask)
    `when`(mockTask.await()).thenReturn(mockDocumentSnapshot)

    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
    profileRepositoryFirestore.gettingUserProfile(mockFirebaseUser, {})
    verify(mockDocumentSnapshot).exists()
  }

  @Test
  fun getFsUidByEmailTestSucceedsWithEmptyList() {
    val mockQuery: Query = mock()
    val mockTask: Task<QuerySnapshot> = mock()
    val mockQuerySnapshot: QuerySnapshot = mock()

    `when`(mockTask.isComplete).thenReturn(true)
    `when`(mockTask.isSuccessful).thenReturn(true)
    `when`(mockTask.result).thenReturn(mockQuerySnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(emptyList())

    `when`(mockFirestore.collection(anyOrNull())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.whereEqualTo(eq("email"), anyOrNull())).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(anyOrNull())).thenReturn(mockTask) // Allow chaining

    var successCalled = false
    var failed = false
    var idGot: String? = null

    profileRepositoryFirestore.getFsUidByEmail(
        "email",
        {
          successCalled = true
          idGot = it
        },
        { failed = true })

    val onCompleteListenerCaptor = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
    verify(mockTask).addOnSuccessListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onSuccess(mockQuerySnapshot)

    assert(successCalled)
    assertFalse(failed)
    assert(idGot == null)
  }

  @Test
  fun getFsUidByEmailTestSucceedsWithNonEmptyList() {
    val mockQuery: Query = mock()
    val mockTask: Task<QuerySnapshot> = mock()
    val mockQuerySnapshot: QuerySnapshot = mock()

    `when`(mockTask.isComplete).thenReturn(true)
    `when`(mockTask.isSuccessful).thenReturn(true)
    `when`(mockTask.result).thenReturn(mockQuerySnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    `when`(mockFirestore.collection(anyOrNull())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.whereEqualTo(eq("email"), anyOrNull())).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(anyOrNull())).thenReturn(mockTask) // Allow chaining

    var idGot: String? = null
    var successCalled = false
    var failed = false
    profileRepositoryFirestore.getFsUidByEmail(
        "email",
        {
          successCalled = true
          idGot = it
        },
        { failed = true })

    val onCompleteListenerCaptor = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
    verify(mockTask).addOnSuccessListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onSuccess(mockQuerySnapshot)

    assert(successCalled)
    assertFalse(failed)
    assert(idGot == profile.fsUid)
  }

  @Test
  fun getFsUidByEmailTestFails() {
    val mockQuery: Query = mock()
    val mockTask: Task<QuerySnapshot> = mock()
    val mockQuerySnapshot: QuerySnapshot = mock()

    `when`(mockTask.isComplete).thenReturn(true)
    `when`(mockTask.isSuccessful).thenReturn(false)

    `when`(mockFirestore.collection(anyOrNull())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.whereEqualTo(eq("email"), anyOrNull())).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(anyOrNull())).thenReturn(mockTask) // Allow chaining

    var idGot: String? = null
    var successCalled = false
    var failed = false
    profileRepositoryFirestore.getFsUidByEmail(
        "email",
        {
          successCalled = true
          idGot = profile.fsUid
        },
        { failed = true })

    val onCompleteListenerCaptor = argumentCaptor<OnFailureListener>()
    verify(mockTask).addOnFailureListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onFailure(Exception("message"))

    assert(failed)
    assertFalse(successCalled)
    assert(idGot == null)
  }

  @Test
  fun addFriendTest() {

    var userProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm12",
            "usernameTest",
            "email@test.ch",
            emptyMap(),
            "nameTest",
            emptyList())

    var friendProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm13",
            "usernameTestFriend",
            "email_friend@test.ch",
            emptyMap(),
            "nameTestFriend",
            emptyList())

    val mockDocumentSnapshotFriend: DocumentSnapshot = mock()
    val mockDocumentReferenceFriend: DocumentReference = mock()
    val mockDocumentReferenceUser: DocumentReference = mock()

    val transactionMock: Transaction = mock()

    whenever(transactionMock.update(any(), eq("friends"), anyOrNull())).thenReturn(transactionMock)

    `when`(mockDocumentSnapshotFriend.id).thenReturn(friendProfile.fsUid)
    `when`(mockDocumentSnapshotFriend.getString("email")).thenReturn(friendProfile.email)
    `when`(mockDocumentSnapshotFriend.getString("name")).thenReturn(friendProfile.name)
    `when`(mockDocumentSnapshotFriend.getString("username")).thenReturn(friendProfile.username)
    `when`(mockDocumentSnapshotFriend.get("userTravelList"))
        .thenReturn(friendProfile.userTravelList)
    `when`(mockDocumentSnapshotFriend.get("friends")).thenReturn(friendProfile.friends)
    `when`(mockDocumentSnapshotFriend.exists()).thenReturn(true)

    val firestoreMock: FirebaseFirestore = mock()
    val profileRepository: ProfileRepositoryFirebase = ProfileRepositoryFirebase(firestoreMock)

    val privateField = profileRepository.javaClass.getDeclaredField("documentReference")
    privateField.isAccessible = true
    privateField.set(profileRepository, mockDocumentReferenceUser)

    val collectionReference: CollectionReference = mock()
    val query: Query = mock()
    val taskFirstLayerMock: Task<DocumentSnapshot> = mock()
    val taskSecondLayerMock: Task<Void> = mock()
    val querySnapshot: QuerySnapshot = mock()

    whenever(firestoreMock.collection(anyOrNull())).thenReturn(collectionReference)
    whenever(collectionReference.document(anyOrNull())).thenReturn(mockDocumentReferenceFriend)
    whenever(mockDocumentReferenceFriend.get()).thenReturn(taskFirstLayerMock)
    whenever(taskFirstLayerMock.addOnSuccessListener(anyOrNull())).thenReturn(taskFirstLayerMock)
    whenever(taskFirstLayerMock.addOnFailureListener(anyOrNull())).thenReturn(taskFirstLayerMock)

    whenever(taskFirstLayerMock.isSuccessful).thenReturn(true)
    whenever(taskFirstLayerMock.result).thenReturn(mockDocumentSnapshotFriend)
    whenever(mockDocumentSnapshotFriend.reference).thenReturn(mockDocumentReferenceFriend)

    whenever(firestoreMock.runTransaction<Void>(anyOrNull())).thenReturn(taskSecondLayerMock)
    whenever(taskSecondLayerMock.isSuccessful).thenReturn(true)
    whenever(taskSecondLayerMock.addOnSuccessListener(anyOrNull())).thenReturn(taskSecondLayerMock)
    whenever(taskSecondLayerMock.addOnFailureListener(anyOrNull())).thenReturn(taskSecondLayerMock)

    var succeeded = false
    var failed = false

    profileRepository.addFriend(
        friendProfile.fsUid,
        userProfile,
        {
          userProfile = it
          succeeded = true
        },
        { failed = true })

    val onCompleteListenerCaptor1 = argumentCaptor<OnSuccessListener<DocumentSnapshot>>()
    verify(taskFirstLayerMock).addOnSuccessListener(onCompleteListenerCaptor1.capture())
    onCompleteListenerCaptor1.firstValue.onSuccess(mockDocumentSnapshotFriend)

    val transactionCaptor = argumentCaptor<Transaction.Function<Void>>()
    verify(firestoreMock).runTransaction(transactionCaptor.capture())
    transactionCaptor.firstValue.apply(transactionMock)

    verify(transactionMock, times(2)).update(anyOrNull(), eq("friends"), anyOrNull())

    val onCompleteListenerCaptor2 = argumentCaptor<OnSuccessListener<Void>>()
    verify(taskSecondLayerMock).addOnSuccessListener(onCompleteListenerCaptor2.capture())
    onCompleteListenerCaptor2.firstValue.onSuccess(null)

    assert(succeeded)
    assertFalse(failed)

    assert(userProfile.friends.contains(friendProfile.email))
  }

  @Test
  fun updatingFriendListTest() {
    val privateFunc =
        profileRepositoryFirestore.javaClass.getDeclaredMethod(
            "updatingFriendList", Profile::class.java, String::class.java, String::class.java)
    privateFunc.isAccessible = true
    val parameters = arrayOfNulls<Any>(3)
    parameters[0] = profile
    parameters[1] = "test@test.ch"
    parameters[2] = "qwertzuiopasdfghjklyxcvbnm12"
    val result = privateFunc.invoke(profileRepositoryFirestore, *parameters)
    assertThat(result, `is`(newProfile))
  }

  @Test
  fun removingFriendSucceedingWorks() {
    val userProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm12",
            "usernameTest",
            "email@test.ch",
            emptyMap(),
            "nameTest",
            emptyList())

    val friendProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm13",
            "usernameTestFriend",
            "emailfriend@test.ch",
            emptyMap(),
            "nameTestFriend",
            emptyList())

    val mockDatabase: FirebaseFirestore = mock()
    val mockCollectionReference: CollectionReference = mock()
    val mockDocumentReference: DocumentReference = mock()
    val mockTaskDocumentSnapshot: Task<DocumentSnapshot> = mock()
    val mockDocumentSnapshot: DocumentSnapshot = mock()
    val mockTransaction: Task<Void> = mock()

    whenever(mockDatabase.collection(anyOrNull())).thenReturn(mockCollectionReference)
    whenever(mockDatabase.runTransaction<Void>(anyOrNull())).thenReturn(mockTransaction)
    whenever(mockCollectionReference.document(anyOrNull())).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTaskDocumentSnapshot)
    whenever(mockTaskDocumentSnapshot.result).thenReturn(mockDocumentSnapshot)
    whenever(mockTaskDocumentSnapshot.isSuccessful).thenReturn(true)
    whenever(mockTaskDocumentSnapshot.addOnSuccessListener(anyOrNull()))
        .thenReturn(mockTaskDocumentSnapshot)
    whenever(mockTaskDocumentSnapshot.addOnFailureListener(anyOrNull()))
        .thenReturn(mockTaskDocumentSnapshot)

    whenever(mockDocumentSnapshot.id).thenReturn(friendProfile.fsUid)
    whenever(mockDocumentSnapshot.getString("name")).thenReturn(friendProfile.name)
    whenever(mockDocumentSnapshot.getString("username")).thenReturn(friendProfile.username)
    whenever(mockDocumentSnapshot.getString("email")).thenReturn(friendProfile.email)
    whenever(mockDocumentSnapshot.get("friends")).thenReturn(friendProfile.friends)
    whenever(mockDocumentSnapshot.get("userTravelList")).thenReturn(friendProfile.userTravelList)

    whenever(mockTransaction.isSuccessful).thenReturn(true)
    whenever(mockTransaction.addOnSuccessListener(anyOrNull())).thenReturn(mockTransaction)
    whenever(mockTransaction.addOnFailureListener(anyOrNull())).thenReturn(mockTransaction)

    var succeeded = false
    var failed = false
    val profileRepository = ProfileRepositoryFirebase(mockDatabase)
    profileRepository.removeFriend(
        friendProfile.fsUid, userProfile, { succeeded = true }, { failed = true })

    val onCompleteListenerCaptor1 = argumentCaptor<OnSuccessListener<DocumentSnapshot>>()
    verify(mockTaskDocumentSnapshot).addOnSuccessListener(onCompleteListenerCaptor1.capture())
    onCompleteListenerCaptor1.firstValue.onSuccess(mockDocumentSnapshot)

    val onCompleteListenerCaptor2 = argumentCaptor<OnSuccessListener<Void>>()
    verify(mockTransaction).addOnSuccessListener(onCompleteListenerCaptor2.capture())
    onCompleteListenerCaptor2.firstValue.onSuccess(null)

    assert(succeeded)
    assertFalse(failed)
  }

  @Test
  fun sendFriendNotificationWorks() {
    var friendProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm13",
            "usernameTestFriend",
            "email_friend@test.ch",
            emptyMap(),
            "nameTestFriend",
            emptyList())

    val mockFirebase: FirebaseFirestore = mock()
    val mockCollectionReference: CollectionReference = mock()
    val mockQuery: Query = mock()
    val mockTask: Task<QuerySnapshot> = mock()
    val mockQuerySnapshot: QuerySnapshot = mock()
    val mockDocumentSnapshot: DocumentSnapshot = mock()

    val mockProfileRepository = ProfileRepositoryFirebase(mockFirebase)
    whenever(mockFirebase.collection(anyOrNull())).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.whereEqualTo(eq("email"), anyOrNull())).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)

    whenever(mockTask.addOnSuccessListener(anyOrNull())).thenReturn(mockTask)
    whenever(mockTask.addOnFailureListener(anyOrNull())).thenReturn(mockTask)

    whenever(mockQuerySnapshot.isEmpty).thenReturn(false)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    whenever(mockTask.isSuccessful).thenReturn(true)
    whenever(mockTask.result).thenReturn(mockQuerySnapshot)

    `when`(mockDocumentSnapshot.id).thenReturn(friendProfile.fsUid)
    `when`(mockDocumentSnapshot.getString("email")).thenReturn(friendProfile.email)
    `when`(mockDocumentSnapshot.getString("name")).thenReturn(friendProfile.name)
    `when`(mockDocumentSnapshot.getString("username")).thenReturn(friendProfile.username)
    `when`(mockDocumentSnapshot.get("userTravelList")).thenReturn(friendProfile.userTravelList)
    `when`(mockDocumentSnapshot.get("friends")).thenReturn(friendProfile.friends)

    var succeeded = false
    var failed = false
    mockProfileRepository.sendFriendNotification(
        friendProfile.email, { succeeded = true }, { failed = true })

    val onCompleteListenerCaptor2 = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
    verify(mockTask).addOnSuccessListener(onCompleteListenerCaptor2.capture())
    onCompleteListenerCaptor2.firstValue.onSuccess(mockQuerySnapshot)

    assert(succeeded)
    assertFalse(failed)
  }

  @Test
  fun sendFriendNotificationFailsForFailedTask() {
    val mockFirebase: FirebaseFirestore = mock()
    val mockCollectionReference: CollectionReference = mock()
    val mockQuery: Query = mock()
    val mockTask: Task<QuerySnapshot> = mock()
    val mockQuerySnapshot: QuerySnapshot = mock()
    val mockDocumentSnapshot: DocumentSnapshot = mock()

    val mockProfileRepository = ProfileRepositoryFirebase(mockFirebase)
    whenever(mockFirebase.collection(anyOrNull())).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.whereEqualTo(eq("email"), anyOrNull())).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)

    whenever(mockTask.addOnSuccessListener(anyOrNull())).thenReturn(mockTask)
    whenever(mockTask.addOnFailureListener(anyOrNull())).thenReturn(mockTask)

    whenever(mockQuerySnapshot.isEmpty).thenReturn(false)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    whenever(mockTask.isSuccessful).thenReturn(false)

    var succeeded = false
    var failed = false
    mockProfileRepository.sendFriendNotification(
        "someemail@random.com", { succeeded = true }, { failed = true })

    val onCompleteListenerCaptor2 = argumentCaptor<OnFailureListener>()
    verify(mockTask).addOnFailureListener(onCompleteListenerCaptor2.capture())
    onCompleteListenerCaptor2.firstValue.onFailure(Exception("message"))

    assert(failed)
    assertFalse(succeeded)
  }

  @Test
  fun removingFriendFailsWhenTransactionFails() {
    val userProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm12",
            "usernameTest",
            "email@test.ch",
            emptyMap(),
            "nameTest",
            emptyList())

    val friendProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm13",
            "usernameTestFriend",
            "emailfriend@test.ch",
            emptyMap(),
            "nameTestFriend",
            emptyList())

    val mockDatabase: FirebaseFirestore = mock()
    val mockCollectionReference: CollectionReference = mock()
    val mockDocumentReference: DocumentReference = mock()
    val mockTaskDocumentSnapshot: Task<DocumentSnapshot> = mock()
    val mockDocumentSnapshot: DocumentSnapshot = mock()
    val mockTransaction: Task<Void> = mock()

    whenever(mockDatabase.collection(anyOrNull())).thenReturn(mockCollectionReference)
    whenever(mockDatabase.runTransaction<Void>(anyOrNull())).thenReturn(mockTransaction)
    whenever(mockCollectionReference.document(anyOrNull())).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTaskDocumentSnapshot)
    whenever(mockTaskDocumentSnapshot.result).thenReturn(mockDocumentSnapshot)
    whenever(mockTaskDocumentSnapshot.isSuccessful).thenReturn(true)
    whenever(mockTaskDocumentSnapshot.addOnSuccessListener(anyOrNull()))
        .thenReturn(mockTaskDocumentSnapshot)
    whenever(mockTaskDocumentSnapshot.addOnFailureListener(anyOrNull()))
        .thenReturn(mockTaskDocumentSnapshot)

    whenever(mockDocumentSnapshot.id).thenReturn(friendProfile.fsUid)
    whenever(mockDocumentSnapshot.getString("name")).thenReturn(friendProfile.name)
    whenever(mockDocumentSnapshot.getString("username")).thenReturn(friendProfile.username)
    whenever(mockDocumentSnapshot.getString("email")).thenReturn(friendProfile.email)
    whenever(mockDocumentSnapshot.get("friends")).thenReturn(friendProfile.friends)
    whenever(mockDocumentSnapshot.get("userTravelList")).thenReturn(friendProfile.userTravelList)

    whenever(mockTransaction.isSuccessful).thenReturn(false)
    whenever(mockTransaction.addOnSuccessListener(anyOrNull())).thenReturn(mockTransaction)
    whenever(mockTransaction.addOnFailureListener(anyOrNull())).thenReturn(mockTransaction)

    var succeeded = false
    var failed = false
    val profileRepository = ProfileRepositoryFirebase(mockDatabase)
    profileRepository.removeFriend(
        friendProfile.fsUid, userProfile, { succeeded = true }, { failed = true })

    val onCompleteListenerCaptor1 = argumentCaptor<OnSuccessListener<DocumentSnapshot>>()
    verify(mockTaskDocumentSnapshot).addOnSuccessListener(onCompleteListenerCaptor1.capture())
    onCompleteListenerCaptor1.firstValue.onSuccess(mockDocumentSnapshot)

    val onCompleteListenerCaptor2 = argumentCaptor<OnFailureListener>()
    verify(mockTransaction).addOnFailureListener(onCompleteListenerCaptor2.capture())
    onCompleteListenerCaptor2.firstValue.onFailure(Exception("failed"))

    assert(failed)
    assertFalse(succeeded)
  }

  @Test
  fun removingFriendFailsWhenFirstLayerTaskFails() {
    val userProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm12",
            "usernameTest",
            "email@test.ch",
            emptyMap(),
            "nameTest",
            emptyList())

    val friendProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm13",
            "usernameTestFriend",
            "emailfriend@test.ch",
            emptyMap(),
            "nameTestFriend",
            emptyList())

    val mockDatabase: FirebaseFirestore = mock()
    val mockCollectionReference: CollectionReference = mock()
    val mockDocumentReference: DocumentReference = mock()
    val mockTaskDocumentSnapshot: Task<DocumentSnapshot> = mock()
    val mockDocumentSnapshot: DocumentSnapshot = mock()

    whenever(mockDatabase.collection(anyOrNull())).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.document(anyOrNull())).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTaskDocumentSnapshot)
    whenever(mockTaskDocumentSnapshot.result).thenReturn(mockDocumentSnapshot)
    whenever(mockTaskDocumentSnapshot.isSuccessful).thenReturn(false)
    whenever(mockTaskDocumentSnapshot.addOnSuccessListener(anyOrNull()))
        .thenReturn(mockTaskDocumentSnapshot)
    whenever(mockTaskDocumentSnapshot.addOnFailureListener(anyOrNull()))
        .thenReturn(mockTaskDocumentSnapshot)

    var succeeded = false
    var failed = false
    val profileRepository = ProfileRepositoryFirebase(mockDatabase)
    profileRepository.removeFriend(
        friendProfile.fsUid, userProfile, { succeeded = true }, { failed = true })

    val onCompleteListenerCaptor1 = argumentCaptor<OnFailureListener>()
    verify(mockTaskDocumentSnapshot).addOnFailureListener(onCompleteListenerCaptor1.capture())
    onCompleteListenerCaptor1.firstValue.onFailure(Exception("failed"))

    assert(failed)
    assertFalse(succeeded)
  }
}
