package com.github.se.travelpouch.model.events

import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertEquals
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
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventRepositoryUnitTest {
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockToDoQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockTravelDocumentCollectionReference: CollectionReference
  @Mock private lateinit var mockTravelDocumentReference: DocumentReference
  @Mock private lateinit var mockEventCollectionReference: CollectionReference
  @Mock private lateinit var mockEventDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockEventDocumentReference: DocumentReference

  private lateinit var eventRepositoryFirestore: EventRepositoryFirebase

  val event = Event("1", EventType.NEW_ACTIVITY, Timestamp(0, 0), "eventTitle", "eventDescription")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    eventRepositoryFirestore = EventRepositoryFirebase(mockFirestore)
    mockDocumentSnapshot = mock(DocumentSnapshot::class.java)

    `when`(mockDocumentSnapshot.id).thenReturn("1")
    `when`(mockDocumentSnapshot.getString("eventType")).thenReturn("NEW_ACTIVITY")
    `when`(mockDocumentSnapshot.getString("title")).thenReturn("eventTitle")
    `when`(mockDocumentSnapshot.getString("description")).thenReturn("eventDescription")
    `when`(mockDocumentSnapshot.getTimestamp("date")).thenReturn(Timestamp(0, 0))
  }

  @Test
  fun initTest() {
    var flag = false
    eventRepositoryFirestore.setIdTravel({ flag = true }, "uid")
    assertEquals(true, flag)
  }

  @Test
  fun getNewUid() {
    whenever(mockFirestore.collection(anyOrNull()))
        .thenReturn(mockTravelDocumentCollectionReference)
    whenever(mockTravelDocumentCollectionReference.document(anyOrNull()))
        .thenReturn(mockTravelDocumentReference)
    whenever(mockTravelDocumentReference.collection(anyOrNull()))
        .thenReturn(mockEventCollectionReference)
    whenever(mockEventCollectionReference.document()).thenReturn(mockEventDocumentReference)
    whenever(mockEventCollectionReference.document(any())).thenReturn(mockEventDocumentReference)
    whenever(mockEventDocumentReference.id).thenReturn("qwertz")

    val reference = eventRepositoryFirestore.getNewDocumentReferenceForNewTravel("qwertz")
    assert(reference == mockEventDocumentReference)
  }

  @Test
  fun getEvents_callsDocuments() {

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)

    // Ensure that mockToDoQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockToDoQuerySnapshot))

    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockToDoQuerySnapshot.documents).thenReturn(listOf())

    // Call the method under test
    eventRepositoryFirestore.getEvents(
        onSuccess = {

          // Do nothing; we just want to verify that the 'documents' field was accessed
        },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockToDoQuerySnapshot).documents }
  }

  @Test
  fun documentToEvent() {
    val privateFunc =
        eventRepositoryFirestore.javaClass.getDeclaredMethod(
            "documentToEvent", DocumentSnapshot::class.java)
    privateFunc.isAccessible = true
    val parameters = arrayOfNulls<Any>(1)
    parameters[0] = mockDocumentSnapshot
    val result = privateFunc.invoke(eventRepositoryFirestore, *parameters)
    assertThat(result, `is`(event))
  }

  @Test
  fun getNewDocumentReference() {
    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)

    whenever(mockDocumentReference.id).thenReturn("uid")
    assert(mockDocumentReference == eventRepositoryFirestore.getNewDocumentReference())
  }
}
