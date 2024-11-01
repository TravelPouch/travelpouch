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
import com.github.se.travelpouch.ui.documents.StoreDocumentButton
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class StoreDocumentButtonTest {

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

    // Set the content for the test
    composeTestRule.setContent {
      withActivityResultRegistry(testRegistry) {
        StoreDocumentButton(
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

    // Set the content for the test
    composeTestRule.setContent {
      withActivityResultRegistry(testRegistry) {
        StoreDocumentButton(
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
}
