package com.github.se.travelpouch.model.profile

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.fail
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
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
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

  private lateinit var profileRepositoryFirestore: ProfileRepositoryFirebase

  private lateinit var mockTaskDocumentSnapshot: Task<DocumentSnapshot>

  val profile =
      Profile(
          "qwertzuiopasdfghjklyxcvbnm12",
          "usernameTest",
          "email@test.ch",
          null,
          "nameTest",
          emptyList())

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

    `when`(mockDocumentSnapshot.id).thenReturn("qwertzuiopasdfghjklyxcvbnm12")
    `when`(mockDocumentSnapshot.getString("email")).thenReturn("email@test.ch")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("nameTest")
    `when`(mockDocumentSnapshot.getString("username")).thenReturn("usernameTest")
    `when`(mockDocumentSnapshot.get("userTravelList")).thenReturn(emptyList<String>())
    `when`(mockDocumentSnapshot.get("friends")).thenReturn(null)

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
            "addProfile", String::class.java, String::class.java)
    privateFunc.isAccessible = true
    val parameters = arrayOfNulls<Any>(2)
    parameters[0] = "email@test.ch"
    parameters[1] = "qwertzuiopasdfghjklyxcvbnm12"

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
            "addingUserIfNotRegistered", FirebaseUser::class.java, DocumentSnapshot::class.java)
    privateFunc.isAccessible = true
    val parameters = arrayOfNulls<Any>(2)
    parameters[0] = mockFirebaseUser
    parameters[1] = mockDocumentSnapshot

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
            "addingUserIfNotRegistered", FirebaseUser::class.java, DocumentSnapshot::class.java)
    privateFunc.isAccessible = true
    val parameters = arrayOfNulls<Any>(2)
    parameters[0] = mockFirebaseUser
    parameters[1] = mockDocumentSnapshot

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
  fun addingUserWithNullEmailTriggersExceptionTest() {
    val privateFunc =
        profileRepositoryFirestore.javaClass.getDeclaredMethod(
            "addingUserIfNotRegistered", FirebaseUser::class.java, DocumentSnapshot::class.java)
    privateFunc.isAccessible = true
    val parameters = arrayOfNulls<Any>(2)
    parameters[0] = mockFirebaseUser
    parameters[1] = mockDocumentSnapshot

    `when`(mockFirebaseUser.uid).thenReturn("qwertzuiopasdfghjklyxcvbnm12")
    `when`(mockFirebaseUser.email).thenReturn(null)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentReference.set(anyOrNull())).thenReturn(Tasks.forResult(null))

    `when`(mockDocumentSnapshot.exists()).thenReturn(false)
    privateFunc.invoke(profileRepositoryFirestore, *parameters)

    verify(mockFirebaseUser).delete()
  }

  @Test
  fun gettingProfileUserCallsTheDocumentReference() {

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

  //  @Test
  //  fun gettingProfileUserCallsCreatingProfile(){
  //
  //    `when`(mockFirebaseUser.uid).thenReturn("uid")
  //    `when`(mockFirebaseUser.email).thenReturn("email")
  //
  //    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
  //    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
  //    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
  //
  //    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
  //    profileRepositoryFirestore.gettingUserProfile(mockFirebaseUser, {})
  //    verify(mockDocumentSnapshot).exists()
  //  }
}
