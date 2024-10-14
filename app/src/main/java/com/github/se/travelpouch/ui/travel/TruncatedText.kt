import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun TruncatedText(
    text: String,
    maxLength: Int,
    fontWeight: FontWeight? = null,
    modifier: Modifier = Modifier
) {
  val displayText =
      if (text.length > maxLength) {
        text.take(maxLength) + "..."
      } else {
        text
      }
  Text(
      text = displayText,
      overflow = TextOverflow.Ellipsis,
      fontWeight = fontWeight,
      modifier = modifier)
}
