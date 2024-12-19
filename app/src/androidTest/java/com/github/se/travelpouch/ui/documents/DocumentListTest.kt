package com.github.se.travelpouch.ui.documents

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentFileFormat
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.documents.DocumentVisibility
import com.github.se.travelpouch.model.documents.DocumentsManager
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy

class DocumentListTest {
  @Composable
  fun withActivityResultRegistry(
      activityResultRegistry: ActivityResultRegistry,
      content: @Composable () -> Unit
  ) {
    val activityResultRegistryOwner =
        object : ActivityResultRegistryOwner {
          override val activityResultRegistry = activityResultRegistry
        }
    CompositionLocalProvider(
        LocalActivityResultRegistryOwner provides activityResultRegistryOwner) {
          content()
        }
  }

  private lateinit var navigationActions: NavigationActions
  private lateinit var mockDocumentRepository: DocumentRepository
  private lateinit var mockDocumentViewModel: DocumentViewModel
  private lateinit var mockDocumentReference1: DocumentReference
  private lateinit var mockDocumentReference2: DocumentReference
  private lateinit var mockListTravelViewModel: ListTravelViewModel
  private lateinit var mockDocumentsManager: DocumentsManager
  private lateinit var list_documents: List<DocumentContainer>
  private lateinit var travelContainer: TravelContainer
  private lateinit var mockDataStore: DataStore<Preferences>

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    mockDocumentReference1 = mock(DocumentReference::class.java)
    `when`(mockDocumentReference1.id).thenReturn("ref_id1")
    mockDocumentReference2 = mock(DocumentReference::class.java)
    `when`(mockDocumentReference2.id).thenReturn("ref_id2")
    list_documents =
        listOf(
            DocumentContainer(
                mockDocumentReference1,
                mockDocumentReference1,
                mockDocumentReference1,
                "title 1",
                DocumentFileFormat.PDF,
                0,
                "email 1",
                mockDocumentReference1,
                Timestamp(0, 0),
                DocumentVisibility.ME),
            DocumentContainer(
                mockDocumentReference2,
                mockDocumentReference2,
                mockDocumentReference2,
                "title 2",
                DocumentFileFormat.PDF,
                0,
                "email 2",
                mockDocumentReference2,
                Timestamp(0, 0),
                DocumentVisibility.ORGANIZERS),
        )
    val participants: MutableMap<Participant, Role> = HashMap()
    participants[Participant("rythwEmprFhOOgsANXnv12345678")] = Role.OWNER
    travelContainer =
        TravelContainer(
            "rythwEmprFhOOgsANXnv",
            "Title",
            "Description",
            Timestamp(0, 0),
            Timestamp.now(),
            Location(40.4114, 40.43321, Timestamp.now(), "Here"),
            HashMap(),
            participants,
            emptyList())
    navigationActions = mock(NavigationActions::class.java)
    mockDocumentsManager = mock(DocumentsManager::class.java)
    mockListTravelViewModel = mock(ListTravelViewModel::class.java)
    mockDocumentRepository = mock(DocumentRepository::class.java)
    mockDataStore = mock()
    val documentViewModel =
        DocumentViewModel(mockDocumentRepository, mockDocumentsManager, mockDataStore)
    `when`(mockDocumentsManager.getThumbnail(anyString(), anyString(), anyInt()))
      .thenReturn(CompletableDeferred())
    mockDocumentViewModel = spy(documentViewModel)
  }

  @Test
  fun testsEverythingIsDisplayed() {
    `when`(mockDocumentRepository.getDocuments(any(), any())).then {
      it.getArgument<(List<DocumentContainer>) -> Unit>(0)(list_documents)
    }
    `when`(mockListTravelViewModel.selectedTravel).then {
      MutableStateFlow<TravelContainer?>(travelContainer)
    }

    composeTestRule.setContent {
      DocumentListScreen(mockDocumentViewModel, mockListTravelViewModel, navigationActions, {})
    }

    composeTestRule.onNodeWithTag("documentListScreen").assertIsDisplayed()

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
    // make spinner appear
    val isLoadingField = DocumentViewModel::class.java.getDeclaredField("_isLoading")
    isLoadingField.isAccessible = true
    val loadingFlow = isLoadingField.get(mockDocumentViewModel) as MutableStateFlow<Boolean>
    loadingFlow.value = true
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("loadingSpinner").assertIsDisplayed()
  }

  @Test
  fun assertMessageWhenNoFileSelectedImportDocument() {
    val testRegistry =
        object : ActivityResultRegistry() {
          override fun <I, O> onLaunch(
              requestCode: Int,
              contract: ActivityResultContract<I, O>,
              input: I,
              options: ActivityOptionsCompat?
          ) {
            dispatchResult(requestCode, null)
          }
        }

    `when`(mockListTravelViewModel.selectedTravel).then {
      MutableStateFlow<TravelContainer?>(travelContainer)
    }

    composeTestRule.setContent {
      withActivityResultRegistry(testRegistry) {
        DocumentListScreen(mockDocumentViewModel, mockListTravelViewModel, navigationActions, {})
      }
    }

    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.executeShellCommand("logcat -c")

    composeTestRule.onNodeWithTag("plusButton").performClick()
    composeTestRule.onNodeWithTag("importLocalFileButton").performClick()

    val logs = device.executeShellCommand("logcat -d travelpouch:D")
    assert(logs.contains("No file selected"))
  }

  @Test
  fun assertUploadFileTriggersWhenUriNotNull() {
    val testRegistry =
        object : ActivityResultRegistry() {
          override fun <I, O> onLaunch(
              requestCode: Int,
              contract: ActivityResultContract<I, O>,
              input: I,
              options: ActivityOptionsCompat?
          ) {
            dispatchResult(requestCode, File.createTempFile("test", ".pdf").toUri())
          }
        }

    `when`(mockListTravelViewModel.selectedTravel).then { MutableStateFlow<TravelContainer?>(null) }

    composeTestRule.setContent {
      withActivityResultRegistry(testRegistry) {
        DocumentListScreen(mockDocumentViewModel, mockListTravelViewModel, navigationActions, {})
      }
    }

    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.executeShellCommand("logcat -c")

    composeTestRule.onNodeWithTag("plusButton").performClick()
    composeTestRule.onNodeWithTag("importLocalFileButton").performClick()

    val logs = device.executeShellCommand("logcat -d travelpouch:D")
    assert(logs.contains("No travel selected"))
  }
}
