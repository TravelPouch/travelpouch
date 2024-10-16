package com.github.se.travelpouch.ui.documents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
fun DocumentListItem(documentContainer: DocumentContainer) {
    Card(
        modifier =
        Modifier.testTag("documentListItem")
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = {}),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            // Date and Title Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text =
                    SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
                        .format(documentContainer.addedAt.toDate()),
                    style = MaterialTheme.typography.bodySmall)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = documentContainer.fileFormat.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(text = documentContainer.title, style = MaterialTheme.typography.bodyMedium)

//            // Location Name
//            Text(
//                text = travelContainer.location.name,
//                style = MaterialTheme.typography.bodySmall,
//                color = Color.Gray)
        }
    }
}