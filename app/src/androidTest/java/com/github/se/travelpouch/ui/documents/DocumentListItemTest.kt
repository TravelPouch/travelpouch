package com.github.se.travelpouch.ui.documents

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentFileFormat
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.documents.DocumentVisibility
import com.github.se.travelpouch.model.documents.DocumentsManager
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class DocumentListItemTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var mockDocumentRepository: DocumentRepository
  private lateinit var mockDocumentViewModel: DocumentViewModel
  private lateinit var mockDocumentReference: DocumentReference
  private lateinit var mockDocumentsManager: DocumentsManager
  private lateinit var mockDataStore: DataStore<Preferences>
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
            Timestamp(LocalDate.EPOCH.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            DocumentVisibility.ME)
    mockDocumentsManager = mock(DocumentsManager::class.java)
    navigationActions = mock(NavigationActions::class.java)
    mockDocumentRepository = mock(DocumentRepository::class.java)
    mockDataStore = mock()
    mockDocumentViewModel =
        DocumentViewModel(mockDocumentRepository, mockDocumentsManager, mockDataStore)

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

    @Test
    fun testsLoadingBehavior() {
      val deferred = CompletableDeferred<Uri>()
      `when`(mockDocumentsManager.getThumbnail(anyString(), anyString(), anyInt()))
        .thenReturn(deferred)

      composeTestRule.setContent { DocumentListItem(document, mockDocumentViewModel) {} }

      // Test that the loading spinner is displayed and the document list item is not displayed
      composeTestRule
          .onNodeWithTag("loadingSpinner-ref_id", useUnmergedTree = true)
          .assertIsDisplayed()
      composeTestRule.onNodeWithTag("thumbnail-ref_id", useUnmergedTree = true).assertIsNotDisplayed()

      // Add the thumbnail URI to the documentViewModel
      deferred.complete(Uri.EMPTY)
      composeTestRule.waitForIdle()

      // Test that the loading spinner is not displayed and the document list item is displayed
      composeTestRule
          .onNodeWithTag("loadingSpinner-ref_id", useUnmergedTree = true)
          .assertIsNotDisplayed()
      composeTestRule.onNodeWithTag("thumbnail-ref_id", useUnmergedTree =
   true).assertIsDisplayed()
    }
}
