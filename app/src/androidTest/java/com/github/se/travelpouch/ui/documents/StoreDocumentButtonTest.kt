import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityOptionsCompat
import com.github.se.travelpouch.helper.DocumentsManager
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.ui.documents.StoreDocumentButton
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class StoreDocumentButtonTest {
  private lateinit var documentViewModel: DocumentViewModel

  @get:Rule val composeTestRule = createComposeRule()

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

  @Before
  fun setup() {
    val repository = mock(DocumentRepository::class.java)
    val documentsManager = mock(DocumentsManager::class.java)
    documentViewModel = spy(DocumentViewModel(repository, documentsManager))
  }

  @Test
  fun assertCallbackOnSuccess() {

    // Create a mock callback
    val mockOnDirectoryPicked: (Uri?) -> Unit = mock()

    // Create a test ActivityResultRegistry
    val testRegistry =
        object : ActivityResultRegistry() {
          override fun <I, O> onLaunch(
              requestCode: Int,
              contract: ActivityResultContract<I, O>,
              input: I,
              options: ActivityOptionsCompat?
          ) {
            dispatchResult(requestCode, Uri.EMPTY)
          }
        }

    documentViewModel.setSaveDocumentFolder(Uri.EMPTY)

    // Set the content for the test
    composeTestRule.setContent {
      withActivityResultRegistry(testRegistry) {
        StoreDocumentButton(
            documentViewModel,
            modifier = Modifier.testTag("clickMe"),
            onDirectoryPicked = mockOnDirectoryPicked // Pass the mock directly
            )
      }
    }

    // Perform the click on the button
    composeTestRule.onNodeWithTag("clickMe").performClick()

    // Verify that the mock callback was called once with null
    verify(mockOnDirectoryPicked, times(1)).invoke(eq(Uri.EMPTY))
  }

  @Test
  fun assertCallbackOnFailure() {
    // Create a mock callback
    val mockOnDirectoryPicked: (Uri?) -> Unit = mock()

    // Create a test ActivityResultRegistry
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

    documentViewModel.setSaveDocumentFolder(Uri.EMPTY)

    // Set the content for the test
    composeTestRule.setContent {
      withActivityResultRegistry(testRegistry) {
        StoreDocumentButton(
            documentViewModel,
            modifier = Modifier.testTag("clickMe"),
            onDirectoryPicked = mockOnDirectoryPicked // Pass the mock directly
            )
      }
    }

    // Perform the click on the button
    composeTestRule.onNodeWithTag("clickMe").performClick()

    // Verify that the mock callback was called once with null
    verify(mockOnDirectoryPicked, never()).invoke(any())
  }

  @Test
  fun assertNoAskIfPermissionExists() {
    // Create a mock callback
    val mockOnDirectoryPicked: (Uri?) -> Unit = mock()

    // Create a test ActivityResultRegistry
    val testRegistry =
        object : ActivityResultRegistry() {
          override fun <I, O> onLaunch(
              requestCode: Int,
              contract: ActivityResultContract<I, O>,
              input: I,
              options: ActivityOptionsCompat?
          ) {
            dispatchResult(requestCode, Uri.EMPTY)
            assert(false)
          }
        }

    documentViewModel.setSaveDocumentFolder(
        Uri.parse("content://com.android.externalstorage.documents/exists"))

    // Set the content for the test
    composeTestRule.setContent {
      withActivityResultRegistry(testRegistry) {
        StoreDocumentButton(
            documentViewModel,
            modifier = Modifier.testTag("clickMe"),
            onDirectoryPicked = mockOnDirectoryPicked // Pass the mock directly
            )
      }
    }

    // Perform the click on the button
    composeTestRule.onNodeWithTag("clickMe").performClick()

    // Verify that the mock callback was called once with null
    verify(mockOnDirectoryPicked, times(1))
        .invoke(Uri.parse("content://com.android.externalstorage.documents/exists"))
  }
}
