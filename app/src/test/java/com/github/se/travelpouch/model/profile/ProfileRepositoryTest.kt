package com.github.se.travelpouch.model.profile

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ProfileRepositoryTest {
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockToDoQuerySnapshot: QuerySnapshot

  private lateinit var profileRepositoryFirestore: ProfileRepositoryFirebase

  private lateinit var mockTaskDocumentSnapshot: Task<DocumentSnapshot>

  val profile = Profile("1", "usernameTest", "emailTest", null, "nameTest", emptyList())

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    profileRepositoryFirestore = ProfileRepositoryFirebase(mockFirestore)
    mockDocumentSnapshot = mock(DocumentSnapshot::class.java)
    mockTaskDocumentSnapshot = mock()

    `when`(mockDocumentSnapshot.id).thenReturn("1")
    `when`(mockDocumentSnapshot.getString("email")).thenReturn("emailTest")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("nameTest")
    `when`(mockDocumentSnapshot.getString("username")).thenReturn("usernameTest")
    `when`(mockDocumentSnapshot.get("userTravelList")).thenReturn(emptyList<String>())
    `when`(mockDocumentSnapshot.get("friends")).thenReturn(null)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getProfile_callsDocuments() {
    // Ensure that mockToDoQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.document(anyOrNull())).thenReturn(mockDocumentReference)

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
    parameters[0] = "email"
    parameters[1] = "uid"

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
}
