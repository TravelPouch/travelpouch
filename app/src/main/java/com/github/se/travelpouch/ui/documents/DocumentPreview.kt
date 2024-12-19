package com.github.se.travelpouch.ui.documents

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import coil.compose.rememberAsyncImagePainter
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentFileFormat
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.rememberVerticalPdfReaderState

/**
 * Composable function for previewing a document.
 *
 * @param documentViewModel the document view model with the current document set as selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentPreview(documentViewModel: DocumentViewModel, navigationActions: NavigationActions) {
  documentViewModel.resetNeedReload()
  val documentContainer: DocumentContainer =
      documentViewModel.selectedDocument.collectAsState().value!!
  val needReload = documentViewModel.needReload.collectAsState().value
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
  LaunchedEffect(needReload) {
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
                    documentViewModel.deleteDocumentById(documentContainer.ref.id)
                    navigationActions.goBack()
                  },
                  modifier = Modifier.testTag("deleteButton")) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Document")
                  }
            })
      },
  ) { paddingValue ->
    Column(modifier = Modifier.fillMaxWidth().padding(paddingValue)) {
      Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
              text = "Document ID: ${documentContainer.ref.id}",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.padding(8.dp).testTag("documentTitle"))

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
  }
}
