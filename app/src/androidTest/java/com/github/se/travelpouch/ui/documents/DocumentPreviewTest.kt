package com.github.se.travelpouch.ui.documents

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.se.travelpouch.di.AppModule
import com.github.se.travelpouch.helper.FileDownloader
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentFileFormat
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.documents.DocumentVisibility
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@HiltAndroidTest
@UninstallModules(AppModule::class)
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
    `when`(mockDocumentReference.id).thenReturn("ref_id")

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

  @After
  fun tearDown() {
    FirebaseApp.clearInstancesForTest()
  }

  @Test
  fun testsEverythingIsDisplayed() {
    composeTestRule.setContent { DocumentPreview(mockDocumentViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("documentPreviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("documentTitleTopBarApp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("documentTitleTopBarApp").assertTextContains(document.title)

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("downloadButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteButton").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("documentTitle", useUnmergedTree = true)
        .assertTextContains("Document ID: ref_id")
  }
}
