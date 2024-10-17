import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/**
 * A composable function that displays a text string, truncating it if it exceeds a specified
 * length.
 *
 * @param text The text to display.
 * @param maxLength The maximum length of the text before truncation.
 * @param fontWeight The font weight to apply to the text. Default is null.
 * @param modifier The modifier to apply to the text composable. Default is Modifier.
 */
@Composable
fun TruncatedText(
    text: String,
    maxLength: Int,
    fontWeight: FontWeight? = null,
    modifier: Modifier = Modifier
) {
  // Determine the text to display, truncating if it exceeds maxLength
  val displayText =
      if (text.length > maxLength) {
        text.take(maxLength) + "..."
      } else {
        text
      }
  // Display the text using the Text composable with ellipsis overflow
  Text(
      text = displayText,
      overflow = TextOverflow.Ellipsis,
      fontWeight = fontWeight,
      modifier = modifier)
}
