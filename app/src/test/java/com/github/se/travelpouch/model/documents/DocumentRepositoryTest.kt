package com.github.se.travelpouch.model.documents

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DocumentRepositoryTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockStorage: FirebaseStorage
  @Mock private lateinit var mockFunctions: FirebaseFunctions
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var documentContainer: DocumentContainer

  private lateinit var documentRepository: DocumentRepositoryFirestore

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    // Mock the required DocumentReference
    mockDocumentReference = mock(DocumentReference::class.java)

    // Set up the DocumentContainer using the mocked DocumentReference
    documentContainer =
        DocumentContainer(
            mockDocumentReference, // ref
            mockDocumentReference, // travelRef
            mockDocumentReference, // activityRef
            "Title", // title
            DocumentFileFormat.PDF, // fileFormat
            0, // fileSize
            "email", // addedByEmail
            mockDocumentReference, // addedByUser
            Timestamp(0, 0), // addedAt
            DocumentVisibility.ME // visibility
            )

    documentRepository =
        DocumentRepositoryFirestore(mockFirestore, mockStorage, mockAuth, mockFunctions)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    // Mock the collection and document references
    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.delete()).thenReturn(mock())
  }

  @Test
  fun initTest() {
    var flag = false
    documentRepository.setIdTravel({ flag = true }, "uid")
    assertEquals(true, flag)
  }

  @Test
  fun successfullyGetsDocuments() {
    val task: Task<QuerySnapshot> = mock()
    val querySnapshot: QuerySnapshot = mock()

    whenever(mockCollectionReference.get()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)
    whenever(task.result).thenReturn(querySnapshot)

    var successCalled = false
    documentRepository.getDocuments({ successCalled = true }, { fail("Should not call onFailure") })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(successCalled)
  }

  @Test
  fun failsToGetDocuments() {
    val task: Task<QuerySnapshot> = mock()
    val exception = Exception("Firestore error")

    whenever(mockCollectionReference.get()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(exception)

    mockStatic(Log::class.java).use { logMock ->
      var failureCalled = false
      documentRepository.getDocuments(
          { fail("Should not call onSuccess") }, { failureCalled = true })

      val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<QuerySnapshot>>()
      verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
      onCompleteListenerCaptor.firstValue.onComplete(task)

      assertTrue(failureCalled)

      logMock.verify { Log.e("DocumentRepositoryFirestore", "Error getting documents", exception) }
    }
  }

  @Test
  fun deleteDocumentByIdSuccessfully() {
    val task: Task<Void> = mock()
    whenever(mockCollectionReference.document(anyString()).delete()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(true)

    var successCalled = false
    documentRepository.deleteDocumentById(
        "documentId", { successCalled = true }, { fail("Should not call onFailure") })

    val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
    verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
    onCompleteListenerCaptor.firstValue.onComplete(task)

    assertTrue(successCalled)
  }

  @Test
  fun failsToDeleteDocumentById() {
    val task: Task<Void> = mock()
    val exception = Exception("Firestore error")

    whenever(mockCollectionReference.document(anyString()).delete()).thenReturn(task)
    whenever(task.isSuccessful).thenReturn(false)
    whenever(task.exception).thenReturn(exception)

    mockStatic(Log::class.java).use { logMock ->
      var failureCalled = false
      documentRepository.deleteDocumentById(
          "documentId", { fail("Should not call onSuccess") }, { failureCalled = true })

      val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
      verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
      onCompleteListenerCaptor.firstValue.onComplete(task)

      assertTrue(failureCalled)

      logMock.verify { Log.e("DocumentRepositoryFirestore", "Error deleting document", exception) }
    }
  }
}
