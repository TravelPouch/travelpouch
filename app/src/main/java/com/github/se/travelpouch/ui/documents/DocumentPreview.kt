// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.documents

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.documentfile.provider.DocumentFile
import coil.compose.rememberAsyncImagePainter
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentFileFormat
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.rememberVerticalPdfReaderState
import java.util.Calendar
import java.util.GregorianCalendar
import kotlinx.coroutines.launch

/**
 * Composable function for previewing a document.
 *
 * @param documentViewModel the document view model with the current document set as selected.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentPreview(
    documentViewModel: DocumentViewModel,
    navigationActions: NavigationActions,
    activityViewModel: ActivityViewModel
) {
  var openDialog by remember { mutableStateOf(false) }

  val documentContainer: DocumentContainer =
      documentViewModel.selectedDocument.collectAsState().value!!
  val uri = documentViewModel.documentUri.value
  val context = LocalContext.current
  val openDirectoryLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) {
        if (it != null) {
          val flagsPermission: Int =
              Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
          try {
            context.contentResolver.takePersistableUriPermission(it, flagsPermission)
          } catch (e: Exception) {
            Toast.makeText(context, "Failed to access directory", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
          }
          val documentFile = DocumentFile.fromTreeUri(context, it)
          if (documentFile != null) {
            documentViewModel.setSaveDocumentFolder(it)
            documentViewModel.getSelectedDocument(documentFile)
          }
        }
      }
  LaunchedEffect(documentContainer) {
    val documentFileUri = documentViewModel.getSaveDocumentFolder().await()
    if (documentFileUri == null) {
      openDirectoryLauncher.launch(null)
      return@LaunchedEffect
    }
    val documentFile = DocumentFile.fromTreeUri(context, documentFileUri)
    if (documentFile != null) {
      documentViewModel.getSelectedDocument(documentFile)
    }
  }

  Scaffold(
      modifier = Modifier.testTag("documentPreviewScreen"),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  documentContainer.title,
                  modifier = Modifier.semantics { testTag = "documentTitleTopBarApp" })
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton") // Tag for back button
                  ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            },
            actions = {
              IconButton(
                  onClick = {
                    val activitiesLinkedToDocument =
                        activityViewModel.activities.value.filter {
                          it.documentsNeeded.contains(documentContainer)
                        }
                    documentViewModel.deleteDocumentById(
                        documentContainer, activitiesLinkedToDocument)
                    navigationActions.goBack()
                  },
                  modifier = Modifier.testTag("deleteButton")) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Document")
                  }

              IconButton(
                  onClick = {
                    if (activityViewModel.activities.value.isEmpty()) {
                      Toast.makeText(
                              context,
                              "Cannot link image if there are no activities",
                              Toast.LENGTH_LONG)
                          .show()
                    } else {
                      openDialog = true
                    }
                  },
                  modifier = Modifier.testTag("linkingButton")) {
                    Icon(imageVector = Icons.Default.AddLink, contentDescription = null)
                  }
            })
      },
  ) { paddingValue ->
    Column(modifier = Modifier.fillMaxWidth().padding(paddingValue)) {
      Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxWidth()) {
          if (uri != null) {
            if (documentContainer.fileFormat == DocumentFileFormat.PDF) {
              val pdfState =
                  rememberVerticalPdfReaderState(
                      resource = ResourceType.Local(uri), isZoomEnable = true)
              VerticalPDFReader(
                  state = pdfState,
                  modifier = Modifier.fillMaxSize().background(color = Color.Gray))
            } else {
              Image(
                  painter = rememberAsyncImagePainter(uri),
                  contentDescription = null,
                  contentScale = ContentScale.Fit,
                  modifier = Modifier.fillMaxSize().testTag("document"))
            }
          }
        }
      }
    }

    if (openDialog) {
      Dialog(
          onDismissRequest = { openDialog = false },
      ) {
        Column(
            modifier =
                Modifier.fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("activitiesDialog"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly) {
              Text(
                  "Activity to link the image to",
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(8.dp))

              LazyColumn(
                  modifier =
                      Modifier.fillMaxWidth(1f)
                          .padding(horizontal = 8.dp)
                          .testTag("activitiesList"),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                  contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(activityViewModel.activities.value.size) { i ->
                      val activity = activityViewModel.activities.value[i]
                      val calendar = GregorianCalendar().apply { time = activity.date.toDate() }

                      Card(
                          modifier = Modifier.testTag("activityItem_${activity.uid}").fillMaxSize(),
                          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                          onClick = {
                            if (!activity.documentsNeeded.contains(documentContainer)) {
                              val documentNeeded = activity.documentsNeeded.toMutableList()
                              documentNeeded.add(documentContainer)
                              val newActivity =
                                  activity.copy(documentsNeeded = documentNeeded.toList())
                              activityViewModel.updateActivity(newActivity, context)
                              openDialog = false
                            } else {
                              Toast.makeText(
                                      context,
                                      "You already linked this document to this activity",
                                      Toast.LENGTH_LONG)
                                  .show()
                            }
                          }) {
                            Column(modifier = Modifier.padding(8.dp)) {
                              Text(
                                  activity.title,
                                  fontWeight = FontWeight.SemiBold,
                                  style = MaterialTheme.typography.bodyLarge)
                              Text(
                                  activity.location.name,
                                  style = MaterialTheme.typography.bodyMedium)
                              Text(
                                  "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(
                                          Calendar.YEAR)}",
                                  style = MaterialTheme.typography.bodyMedium,
                                  fontWeight = FontWeight.Light)
                            }
                          }
                    }
                  }
            }
      }
    }
  }
}
