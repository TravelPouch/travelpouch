package com.github.se.travelpouch.ui.documents

import android.content.Context
import android.provider.DocumentsContract
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.github.se.travelpouch.di.AppModule
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentFileFormat
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.documents.DocumentVisibility
import com.github.se.travelpouch.model.documents.DocumentsManager
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import java.io.File
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.spy

@HiltAndroidTest
@UninstallModules(AppModule::class)
class DocumentPreviewTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var mockDocumentRepository: DocumentRepository
  private lateinit var mockDocumentViewModel: DocumentViewModel
  private lateinit var mockDocumentReference: DocumentReference
  private lateinit var mockDocumentsManager: DocumentsManager
  private lateinit var document: DocumentContainer
  private lateinit var mockDataStore: DataStore<Preferences>
  private lateinit var file: File

  private lateinit var mockActivityRepository: ActivityRepository
  private lateinit var mockActivityViewModel: ActivityViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    mockActivityRepository = mock()
    mockActivityViewModel = ActivityViewModel(mockActivityRepository)

    mockDocumentReference = mock(DocumentReference::class.java)
    `when`(mockDocumentReference.id).thenReturn("ref_id")

    document =
        DocumentContainer(
            mockDocumentReference,
            mockDocumentReference,
            mockDocumentReference,
            "title",
            DocumentFileFormat.PNG,
            0,
            "email",
            mockDocumentReference,
            Timestamp(0, 0),
            DocumentVisibility.ME)
    navigationActions = mock(NavigationActions::class.java)
    mockDocumentRepository = mock(DocumentRepository::class.java)
    mockDocumentsManager = mock(DocumentsManager::class.java)
    mockDataStore = mock()
    val context = getInstrumentation().context
    val folder = context.getDir("documentPreviewTest", Context.MODE_PRIVATE)
    `when`(mockDataStore.data)
        .thenReturn(
            flowOf(
                preferencesOf(
                    stringPreferencesKey("save_document_folder") to
                        DocumentsContract.buildTreeDocumentUri(
                                "com.github.se.travelpouch", folder.toUri().path!!)
                            .toString())))
    mockDocumentViewModel =
        spy(DocumentViewModel(mockDocumentRepository, mockDocumentsManager, mockDataStore))

    mockDocumentViewModel.selectDocument(document)
    file = File.createTempFile("mountain", ".png")
    context.resources.openRawResource(com.github.se.travelpouch.test.R.drawable.mountain).use {
      file.outputStream().use { output -> it.copyTo(output) }
    }
    `when`(mockDocumentViewModel.documentUri).thenReturn(mutableStateOf(file.toUri()))
  }

  @After
  fun tearDown() {
    FirebaseApp.clearInstancesForTest()

    val cacheDir = getInstrumentation().context.getDir("documentPreviewTest", Context.MODE_PRIVATE)
    cacheDir.deleteRecursively()
    file.delete()
  }

  @Test
  fun testsEverythingIsDisplayed() {
    composeTestRule.setContent {
      DocumentPreview(mockDocumentViewModel, navigationActions, mockActivityViewModel)
    }

    composeTestRule.onNodeWithTag("documentPreviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("documentTitleTopBarApp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("documentTitleTopBarApp").assertTextContains(document.title)

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteButton").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("documentTitle", useUnmergedTree = true)
        .assertTextContains("Document ID: ref_id")
    composeTestRule.waitUntil(1000) { composeTestRule.onNodeWithTag("document").isDisplayed() }
  }
}
