package com.github.se.travelpouch.ui.documents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentFileFormat
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.documents.DocumentVisibility
import com.github.se.travelpouch.model.documents.NewDocumentContainer
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable function for displaying a list of documents.
 *
 * @param documentContainer The document container to display.
 */
@Composable
fun DocumentList(
    documentViewModel: DocumentViewModel = viewModel(factory = DocumentViewModel.Factory),
    navigationActions: NavigationActions
) {
    val documentList = documentViewModel.documents.collectAsState().value
    documentViewModel.getDocuments()

    Scaffold(
        modifier = Modifier.testTag("documentListScreen"),
        floatingActionButton = {
            var toggled by remember { mutableStateOf(false) }

            Column(horizontalAlignment = Alignment.End) {

                if (toggled) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {
                        ExtendedFloatingActionButton(
                            text = { Text("Import from local files") },
                            icon = {
                                Icon(
                                    Icons.Default.UploadFile,
                                    contentDescription = "Add Document"
                                )
                            },
                            onClick = {}
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ExtendedFloatingActionButton(
                            text = { Text("Scan with camera") },
                            icon = {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Add Document"
                                )
                            },
                            onClick = {}
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FloatingActionButton(
                            onClick = {
                                toggled = !toggled
                            },
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Add Document"
                            )
                        }
                    }
                } else {
                    FloatingActionButton(
                        onClick = {
                            toggled = !toggled
                        },
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Document"
                        )
                    }
                }

            }
        }
    ) { paddingValue ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Date and Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    documentViewModel.createDocument(
                        NewDocumentContainer(
                            title = "New Document AAAABBBB",
                            travelRef = Firebase.firestore.document("travels/ujqUGbYn2A8NXdNGGJ0D"),
                            fileFormat = DocumentFileFormat.PDF,
                            fileSize = 0,
                            addedAt = Timestamp.now(),
                            visibility = DocumentVisibility.ME
                        )
                    )
                }) {
                    Text(text = "Create doc", style = MaterialTheme.typography.bodyMedium)
                }

                Button(onClick = {
                    documentViewModel.getDocuments()
                }) {
                    Text(text = "Load docs", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(documentList.size) { index ->
                    DocumentListItem(documentList[index])
                }
            }

//            // Location Name
//            Text(
//                text = travelContainer.location.name,
//                style = MaterialTheme.typography.bodySmall,
//                color = Color.Gray)
        }
    }


}