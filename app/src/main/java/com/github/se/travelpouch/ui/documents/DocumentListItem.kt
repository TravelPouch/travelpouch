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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.documents.DocumentContainer
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable function for displaying a document item.
 *
 * @param documentContainer The document container to display.
 */
@Composable
fun DocumentListItem(documentContainer: DocumentContainer, onClick: () -> Unit) {
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
                    style = MaterialTheme.typography.bodySmall)

                Text(
                    text = documentContainer.fileFormat.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold)
              }

          Box(
              modifier =
                  Modifier.height(200.dp)
                      .width(150.dp)
                      .background(MaterialTheme.colorScheme.onPrimary)) {}

          Text(text = documentContainer.title, style = MaterialTheme.typography.bodyMedium)
        }
  }
}
