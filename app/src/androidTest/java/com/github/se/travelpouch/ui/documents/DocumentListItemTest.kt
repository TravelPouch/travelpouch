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
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class DocumentListItemTest {

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
            Timestamp(LocalDate.EPOCH.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            DocumentVisibility.ME)
    mockFileDownloader = mock(FileDownloader::class.java)
    navigationActions = mock(NavigationActions::class.java)
    mockDocumentRepository = mock(DocumentRepository::class.java)
    mockDocumentViewModel = DocumentViewModel(mockDocumentRepository, mockFileDownloader)

    mockDocumentViewModel.selectDocument(document)
  }

  @Test
  fun testsEverythingIsDisplayed() {
    composeTestRule.setContent { DocumentListItem(document, mockDocumentViewModel) {} }

    composeTestRule.onNodeWithTag("documentListItem", useUnmergedTree = true).assertIsDisplayed()

    composeTestRule.onNodeWithTag("dateDocumentText", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("dateDocumentText", useUnmergedTree = true)
        .assertTextContains("01/01/1970 12:00:00")

    composeTestRule.onNodeWithTag("fileFormatText", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("fileFormatText", useUnmergedTree = true)
        .assertTextContains("PDF")

    composeTestRule.onNodeWithTag("DocumentTitle", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("DocumentTitle", useUnmergedTree = true)
        .assertTextContains(document.title)
  }
}
