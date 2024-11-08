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
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.github.se.travelpouch.helper.FileDownloader
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentFileFormat
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.documents.DocumentVisibility
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.validateMockitoUsage
import org.mockito.kotlin.verify

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
  private lateinit var mockDocumentReference: DocumentReference
  private lateinit var mockListTravelViewModel: ListTravelViewModel
  private lateinit var mockFileDownloader: FileDownloader
  private lateinit var list_documents: List<DocumentContainer>
  private lateinit var travelContainer: TravelContainer

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
    mockFileDownloader = mock(FileDownloader::class.java)
    mockListTravelViewModel = mock(ListTravelViewModel::class.java)
    mockDocumentRepository = mock(DocumentRepository::class.java)
    val documentViewModel = DocumentViewModel(mockDocumentRepository, mockFileDownloader)
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

    `when`(mockListTravelViewModel.selectedTravel).then {
      MutableStateFlow<TravelContainer?>(null)
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
    assert(logs.contains("No travel selected"))
  }
}
