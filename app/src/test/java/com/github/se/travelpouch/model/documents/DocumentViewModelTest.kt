package com.github.se.travelpouch.model.documents

import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class DocumentViewModelTest {

  private lateinit var documentRepository: DocumentRepository
  private lateinit var documentViewModel: DocumentViewModel

  private lateinit var documentContainer: DocumentContainer
  private lateinit var documentReference: DocumentReference
  private lateinit var documentsManager: DocumentsManager
  private lateinit var document: NewDocumentContainer
  private lateinit var selectedTravel: TravelContainer
  private lateinit var dataStore: DataStore<Preferences>

  @Before
  fun setUp() {
    documentRepository = mock(DocumentRepository::class.java)
    documentsManager = mock(DocumentsManager::class.java)
    dataStore = mock()
    documentViewModel = DocumentViewModel(documentRepository, documentsManager, dataStore)
    documentReference = mock(DocumentReference::class.java)

    documentContainer =
        DocumentContainer(
            documentReference, // ref
            documentReference, // travelRef
            documentReference, // activityRef
            "Title", // title
            DocumentFileFormat.PDF, // fileFormat
            0, // fileSize
            "email", // addedByEmail
            documentReference, // addedByUser
            Timestamp(0, 0), // addedAt
            DocumentVisibility.ME // visibility
            )

    document =
        NewDocumentContainer(
            title = "My Travel Document",
            travelRef = documentReference, // Remplacez par une instance de DocumentReference
            fileFormat = DocumentFileFormat.PDF, // Remplacez par une instance de DocumentFileFormat
            fileSize = 2048, // Taille en octets
            addedAt = Timestamp.now(),
            visibility = DocumentVisibility.ME // Remplacez par une instance de DocumentVisibility
            )
    selectedTravel =
        TravelContainer(
            "test1234test1234test",
            "Test Travel",
            "Test Description",
            Timestamp(0, 0),
            Timestamp.now(),
            Location(40.4114, 40.43321, Timestamp.now(), "Here"),
            mapOf(),
            mapOf(Participant("rythwEmprFhOOgsANXnv12345678") to Role.OWNER),
            listOf())
    ShadowLog.clear()
  }

  @Test
  fun getDocuments_successfulFetch_updatesDocuments() {
    val documentList = listOf(documentContainer)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<DocumentContainer>) -> Unit
          onSuccess(documentList)
          null
        }
        .whenever(documentRepository)
        .getDocuments(anyOrNull(), anyOrNull())

    documentViewModel.getDocuments()

    verify(documentRepository).getDocuments(anyOrNull(), anyOrNull())
    assertThat(documentViewModel.documents.value, `is`(documentList))
  }

  @Test
  fun getDocuments_failureFetch_doesNotUpdateDocuments() {
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(1) as (Exception) -> Unit
          onFailure(Exception("Get Documents Failed Test"))
          null
        }
        .whenever(documentRepository)
        .getDocuments(anyOrNull(), anyOrNull())

    documentViewModel.getDocuments()

    assertThat(documentViewModel.documents.value, `is`(emptyList()))
  }

  @Test
  fun deleteDocumentById_successfulDelete_updatesDocuments() {
    val emptyDocumentList = emptyList<DocumentContainer>()
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(1) as () -> Unit
          onSuccess()
          null
        }
        .whenever(documentRepository)
        .deleteDocumentById(anyOrNull(), anyOrNull(), anyOrNull())

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<DocumentContainer>) -> Unit
          onSuccess(emptyDocumentList)
          null
        }
        .whenever(documentRepository)
        .getDocuments(anyOrNull(), anyOrNull())

    documentViewModel.deleteDocumentById("1")
    assertThat(documentViewModel.documents.value, `is`(emptyDocumentList))
  }

  @Test
  fun selectDocument_updatesSelectedDocument() {
    documentViewModel.selectDocument(documentContainer)
    assertThat(documentViewModel.selectedDocument.value, `is`(documentContainer))
  }

  @Test
  fun getDocuments_logsErrorOnFailure() {
    val errorMessage = "Failed to get Documents"
    val exception = Exception("Get Documents Failed Test")
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(1) as (Exception) -> Unit
          onFailure(exception)
          null
        }
        .whenever(documentRepository)
        .getDocuments(anyOrNull(), anyOrNull())

    mockStatic(Log::class.java).use { logMock: MockedStatic<Log> ->
      logMock.`when`<Int> { Log.e(anyString(), anyString(), any()) }.thenReturn(0)

      documentViewModel.getDocuments()

      verify(documentRepository).getDocuments(anyOrNull(), anyOrNull())
      logMock.verify { Log.e("DocumentsViewModel", errorMessage, exception) }
    }
  }

  @Test
  fun setSaveDocumentFolder_nullUri() = runTest {
    whenever(dataStore.edit { any() }).thenAnswer { fail() }
    documentViewModel.setSaveDocumentFolder(null)
  }

  @Test
  fun setSaveDocumentFolder_notNullUri() = runBlocking {
    documentViewModel.setSaveDocumentFolder(Uri.EMPTY).join()
    org.mockito.kotlin.verifyBlocking(dataStore) {
      edit(argumentCaptor<suspend (Preferences) -> Unit>().capture())
    }
  }

  @Test
  fun deleteDocumentById_logsErrorOnFailure() {
    val errorMessage = "Failed to delete Document"
    val exception = Exception("Delete Document Failed Test")
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(2) as (Exception) -> Unit
          onFailure(exception)
          null
        }
        .whenever(documentRepository)
        .deleteDocumentById(anyOrNull(), anyOrNull(), anyOrNull())

    mockStatic(Log::class.java).use { logMock: MockedStatic<Log> ->
      logMock.`when`<Int> { Log.e(anyString(), anyString(), any()) }.thenReturn(0)

      documentViewModel.deleteDocumentById("1")

      verify(documentRepository).deleteDocumentById(anyOrNull(), anyOrNull(), anyOrNull())
      logMock.verify { Log.e("DocumentsViewModel", errorMessage, exception) }
    }
  }

  private fun testUploadFileLog(
      documentViewModel: DocumentViewModel,
      inputStream: InputStream?,
      selectedTravel: TravelContainer?,
      mimeType: String?,
      expected: String
  ): Boolean {
    ShadowLog.clear()
    documentViewModel.uploadFile(inputStream, selectedTravel, mimeType)
    val logs = ShadowLog.getLogs()
    return logs.find {
      it.type == Log.ERROR && it.tag == "DocumentViewModel" && it.msg == expected
    } != null
  }

  @Test
  fun assertUploadFileAnyInvalid() {
    val documentViewModel = DocumentViewModel(documentRepository, documentsManager, mock())
    val mockInputStream = mock(InputStream::class.java)

    assert(
        testUploadFileLog(documentViewModel, null, selectedTravel, "image/jpeg", "No input stream"))
    assert(
        testUploadFileLog(
            documentViewModel, mockInputStream, null, "image/jpeg", "No travel selected"))
    assert(
        testUploadFileLog(
            documentViewModel, mockInputStream, selectedTravel, null, "No or invalid mime type"))
    assert(
        testUploadFileLog(
            documentViewModel,
            mockInputStream,
            selectedTravel,
            "application/json",
            "No or invalid mime type"))
  }

  @Test
  fun assertUploadFileValid() {
    val SIZE = 1024
    val SEED = 123L
    val spyDocumentViewModel = spy(documentViewModel)

    val data = ByteArray(SIZE)
    Random(SEED).nextBytes(data)
    val inputStream: InputStream = ByteArrayInputStream(data)
    spyDocumentViewModel.uploadFile(inputStream, selectedTravel, "image/jpeg")
    verify(spyDocumentViewModel)
        .uploadDocument(anyString(), org.mockito.kotlin.any(), org.mockito.kotlin.any())
  }
}
