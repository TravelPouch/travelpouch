package com.github.se.travelpouch.model.documents

import android.content.Context
import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.documentfile.provider.DocumentFile
import androidx.test.core.app.ApplicationProvider
import com.github.se.travelpouch.helper.FileDownloader
import com.github.se.travelpouch.ui.documents.DocumentPreview
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class DocumentPreviewTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var mockDocumentRepository: DocumentRepository
  private lateinit var mockDocumentViewModel: DocumentViewModel
  private lateinit var mockDocumentReference: DocumentReference
  private lateinit var mockFileDownloader: FileDownloader
  private lateinit var document: DocumentContainer

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    mockDocumentReference = mock(DocumentReference::class.java)
    document =
        DocumentContainer(
            mockDocumentReference,
            mockDocumentReference,
            mockDocumentReference,
            "title",
            DocumentFileFormat.PDF,
            0,
            "email",
            mockDocumentReference,
            Timestamp(0, 0),
            DocumentVisibility.ME)
    navigationActions = mock(NavigationActions::class.java)
    mockDocumentRepository = mock(DocumentRepository::class.java)
    mockFileDownloader = mock(FileDownloader::class.java)
    mockDocumentViewModel = DocumentViewModel(mockDocumentRepository, mockFileDownloader)

    mockDocumentViewModel.selectDocument(document)
  }

  @Test
  fun assertToastIsShownWhenDocumentIsDownloaded() {
    val documentViewModel = mock(DocumentViewModel::class.java)
    val navigationActions = mock(NavigationActions::class.java)
    val documentFile = mock(DocumentFile::class.java)
    val uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADownload")
    `when`(documentFile.uri).thenReturn(uri)
    `when`(documentViewModel.selectedDocument).thenReturn(MutableStateFlow(document))
    `when`(documentViewModel.saveDocumentFolder).thenReturn(MutableStateFlow(uri))
    `when`(documentViewModel.storeSelectedDocument(any())).thenReturn(Job().apply { complete() })

    composeTestRule.setContent { DocumentPreview(documentViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("downloadButton").performClick()

    ShadowLooper.idleMainLooper(1)
    assertEquals("Document downloaded in primary:Download", ShadowToast.getTextOfLatestToast())
  }
}
