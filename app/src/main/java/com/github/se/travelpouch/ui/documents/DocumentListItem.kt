package com.github.se.travelpouch.ui.documents

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentFileFormat
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.rememberVerticalPdfReaderState
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
  var previewUri by remember { mutableStateOf("") }
  LaunchedEffect(documentContainer) { documentViewModel.getDownloadUrl(documentContainer) }
  previewUri = documentViewModel.downloadUrls[documentContainer.ref.id] ?: ""

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
            if (previewUri.isNotEmpty()) {
              DocumentPreviewBox(previewUri, documentContainer.fileFormat)
            } else {
              // Display a placeholder
            }
          }

          Text(
              text = documentContainer.title,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.testTag("DocumentTitle"))
        }
  }
}

@Composable
fun DocumentPreviewBox(previewUri: String, fileFormat: DocumentFileFormat) {
  if (fileFormat == DocumentFileFormat.PDF) {
    val pdfState =
        rememberVerticalPdfReaderState(
            resource = ResourceType.Remote(previewUri), isZoomEnable = false)
    VerticalPDFReader(
        state = pdfState, modifier = Modifier.fillMaxSize().background(color = Color.Gray))
  } else {
    AsyncImage(
        model = previewUri,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize(),
    )
  }
}
