package com.github.se.travelpouch.ui.documents

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.se.travelpouch.helper.FileDownloader
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentFileFormat
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.documents.DocumentVisibility
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

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
  fun testsEverythingIsDisplayed() {
    composeTestRule.setContent { DocumentPreview(mockDocumentViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("documentListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("documentTitleTopBarApp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("documentTitleTopBarApp").assertTextContains(document.title)

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("downloadButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteButton").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("documentTitle", useUnmergedTree = true)
        .assertTextContains(document.title)
  }
}
