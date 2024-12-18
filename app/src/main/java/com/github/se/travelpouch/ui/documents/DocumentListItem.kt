// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.documents

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.se.travelpouch.R
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentViewModel
import java.lang.Integer.getInteger
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable function for displaying a document item.
 *
 * @param documentContainer The document container to display.
 */
@Composable
fun DocumentListItem(
    documentContainer: DocumentContainer,
    documentViewModel: DocumentViewModel,
    onClick: () -> Unit
) {
  var thumbnailUri by remember { mutableStateOf<Uri?>(null) }
  val context = LocalContext.current
  val width = context.resources.getInteger(R.integer.thumbnail_documents_list_width)
  LaunchedEffect(documentContainer) {
    documentViewModel.getDocumentThumbnail(documentContainer, width)
  }
  thumbnailUri = documentViewModel.thumbnailUris["${documentContainer.ref.id}-${width}"]

  Card(
      modifier =
          Modifier.testTag("documentListItem")
              .fillMaxSize()
              .padding(4.dp)
              .clickable(onClick = onClick),
  ) {
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
                            .format(documentContainer.addedAt.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("dateDocumentText"))

                Text(
                    text = documentContainer.fileFormat.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("fileFormatText"))
              }

          Box(modifier = Modifier.height(200.dp).width(150.dp)) {
            if (thumbnailUri != null) {
              AsyncImage(
                  model = thumbnailUri,
                  contentDescription = null,
                  contentScale = ContentScale.Fit,
                  modifier =
                      Modifier.fillMaxSize().testTag("thumbnail-${documentContainer.ref.id}"),
              )
            } else {
              CircularProgressIndicator(
                  modifier =
                      Modifier.align(Alignment.Center)
                          .testTag("loadingSpinner-${documentContainer.ref.id}"),
                  color = MaterialTheme.colorScheme.primary,
                  strokeWidth = 5.dp)
            }
          }

          Text(
              text = documentContainer.title,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.testTag("DocumentTitle"))
        }
  }
}
