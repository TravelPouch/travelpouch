package com.github.se.travelpouch.model.activity

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.github.se.travelpouch.model.Location
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
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
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ActivityRepositoryTest {
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockToDoQuerySnapshot: QuerySnapshot

  private lateinit var activityRepositoryFirestore: ActivityRepositoryFirebase

  val activity =
      Activity(
          "activityUid",
          "activityTitle",
          "activityDescription",
          Location(0.0, 0.0, Timestamp(0, 0), "location"),
          Timestamp(0, 0),
          null)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    activityRepositoryFirestore = ActivityRepositoryFirebase(mockFirestore)
    mockDocumentSnapshot = mock(DocumentSnapshot::class.java)

    `when`(mockDocumentSnapshot.id).thenReturn("activityUid")
    `when`(mockDocumentSnapshot.getString("title")).thenReturn("activityTitle")
    `when`(mockDocumentSnapshot.getString("description")).thenReturn("activityDescription")
    `when`(mockDocumentSnapshot.getTimestamp("date")).thenReturn(Timestamp(0, 0))
    `when`(mockDocumentSnapshot.get("location"))
        .thenReturn(
            mapOf(
                "latitude" to activity.location.latitude,
                "longitude" to activity.location.longitude,
                "name" to activity.location.name,
                "insertTime" to activity.location.insertTime))
    `when`(mockDocumentSnapshot.get("documentsNeeded")).thenReturn(null)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = activityRepositoryFirestore.getNewUid()
    assert(uid == "1")
  }

  @Test
  fun getActivity_callsDocuments() {
    // Ensure that mockToDoQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockToDoQuerySnapshot))

    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockToDoQuerySnapshot.documents).thenReturn(listOf())

    // Call the method under test
    activityRepositoryFirestore.getActivity(
        onSuccess = {

          // Do nothing; we just want to verify that the 'documents' field was accessed
        },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockToDoQuerySnapshot).documents }
  }

  @Test
  fun addActivity_shouldCallFirestoreCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null)) // Simulate success

    // This test verifies that when we add a new event, the Firestore `collection()` method is
    // called.
    activityRepositoryFirestore.addActivity(activity, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    // Ensure Firestore collection method was called to reference the "events" collection
    verify(mockDocumentReference).set(any())
  }

  @Test
  fun deleteToDoById_shouldCallDocumentReferenceDelete() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    activityRepositoryFirestore.deleteActivityById("activityUid", onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    verify(mockDocumentReference).delete()
  }

  @Test
  fun documentToActivity() {
    val privateFunc =
        activityRepositoryFirestore.javaClass.getDeclaredMethod(
            "documentToActivity", DocumentSnapshot::class.java)
    privateFunc.isAccessible = true
    val parameters = arrayOfNulls<Any>(1)
    parameters[0] = mockDocumentSnapshot
    val result = privateFunc.invoke(activityRepositoryFirestore, *parameters)
    assertThat(result, `is`(activity))
  }
}
