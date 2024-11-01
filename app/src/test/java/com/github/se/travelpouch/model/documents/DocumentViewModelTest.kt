package com.github.se.travelpouch.model.documents

import android.util.Log
import com.github.se.travelpouch.helper.FileDownloader
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class DocumentViewModelTest {

  private lateinit var documentRepository: DocumentRepository
  private lateinit var documentViewModel: DocumentViewModel

  private lateinit var documentContainer: DocumentContainer
  private lateinit var documentReference: DocumentReference
  private lateinit var fileDownloader: FileDownloader
  private lateinit var document: NewDocumentContainer

  @Before
  fun setUp() {
    documentRepository = mock(DocumentRepository::class.java)
    fileDownloader = mock(FileDownloader::class.java)
    documentViewModel = DocumentViewModel(documentRepository, fileDownloader)
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
  fun createDocument_successfulAdd_updatesDocuments() {
    val documentList = listOf(documentContainer)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(1) as () -> Unit
          onSuccess()
          null
        }
        .whenever(documentRepository)
        .createDocument(anyOrNull(), anyOrNull(), anyOrNull())

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<DocumentContainer>) -> Unit
          onSuccess(documentList)
          null
        }
        .whenever(documentRepository)
        .getDocuments(anyOrNull(), anyOrNull())

    documentViewModel.createDocument(document)

    assertThat(documentViewModel.documents.value, `is`(documentList))
  }

  @Test
  fun createDocument_failureAdd_doesNotUpdateDocuments() {
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(2) as (Exception) -> Unit
          onFailure(Exception("Add Document Failed Test"))
          null
        }
        .whenever(documentRepository)
        .createDocument(anyOrNull(), anyOrNull(), anyOrNull())

    // documentViewModel.createDocument(NewDocumentContainer("Test Document", "Test Content"))

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
  fun createDocument_logsErrorOnFailure() {
    val errorMessage = "Failed to create Document"
    val exception = Exception("Add Document Failed Test")
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(2) as (Exception) -> Unit
          onFailure(exception)
          null
        }
        .whenever(documentRepository)
        .createDocument(anyOrNull(), anyOrNull(), anyOrNull())

    mockStatic(Log::class.java).use { logMock: MockedStatic<Log> ->
      logMock.`when`<Int> { Log.e(anyString(), anyString(), any()) }.thenReturn(0)

      documentViewModel.createDocument(document)

      verify(documentRepository).createDocument(anyOrNull(), anyOrNull(), anyOrNull())
      logMock.verify { Log.e("DocumentsViewModel", errorMessage, exception) }
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
}
