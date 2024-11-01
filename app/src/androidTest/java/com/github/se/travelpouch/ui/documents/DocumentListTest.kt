package com.github.se.travelpouch.ui.documents

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.helper.FileDownloader
import com.github.se.travelpouch.model.ListTravelViewModel
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
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class DocumentListTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var mockDocumentRepository: DocumentRepository
  private lateinit var mockDocumentViewModel: DocumentViewModel
  private lateinit var mockDocumentReference: DocumentReference
  private lateinit var mockFileDownloader: FileDownloader
  private lateinit var mockListTravelViewModel: ListTravelViewModel
  private lateinit var list_documents: List<DocumentContainer>

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    mockDocumentReference = mock(DocumentReference::class.java)
    list_documents =
        listOf(
            DocumentContainer(
                mockDocumentReference,
                mockDocumentReference,
                mockDocumentReference,
                "title 1",
                DocumentFileFormat.PDF,
                0,
                "email 1",
                mockDocumentReference,
                Timestamp(0, 0),
                DocumentVisibility.ME),
            DocumentContainer(
                mockDocumentReference,
                mockDocumentReference,
                mockDocumentReference,
                "title 2",
                DocumentFileFormat.PDF,
                0,
                "email 2",
                mockDocumentReference,
                Timestamp(0, 0),
                DocumentVisibility.ORGANIZERS),
        )
    navigationActions = mock(NavigationActions::class.java)
    mockDocumentRepository = mock(DocumentRepository::class.java)
    mockDocumentViewModel = DocumentViewModel(mockDocumentRepository, mockFileDownloader)
  }

  @Test
  fun testsEverythingIsDisplayed() {
    `when`(mockDocumentRepository.getDocuments(any(), any())).then {
      it.getArgument<(List<DocumentContainer>) -> Unit>(0)(list_documents)
    }

    composeTestRule.setContent { DocumentListScreen(mockDocumentViewModel, navigationActions, {}) }

    composeTestRule.onNodeWithTag("documentListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("documentListTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("documentListTitle").assertTextEquals("Travel's documents")

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("plusButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("plusButton").performClick()

    composeTestRule.onNodeWithTag("importLocalFileButton").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("importLocalFileButtonText", useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("importLocalFileButtonText", useUnmergedTree = true)
        .assertTextEquals("Import from local files")

    composeTestRule.onNodeWithTag("scanCamButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scanCamButtonText", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("scanCamButtonText", useUnmergedTree = true)
        .assertTextEquals("Scan with camera")

    composeTestRule.onNodeWithTag("dropDownButton").assertIsDisplayed()
  }
}
