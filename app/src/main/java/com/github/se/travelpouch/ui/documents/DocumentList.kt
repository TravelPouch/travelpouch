package com.github.se.travelpouch.ui.documents

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.travelpouch.model.documents.DocumentFileFormat
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

/**
 * Composable function for displaying a list of documents.
 *
 * @param documentViewModel the document view model to use.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    documentViewModel: DocumentViewModel = viewModel(),
    listTravelViewModel: ListTravelViewModel = viewModel(factory = ListTravelViewModel.Factory),
    navigationActions: NavigationActions,
    onNavigateToDocumentPreview: () -> Unit
) {
  val isLoading = documentViewModel.isLoading.collectAsState()
  val documents = documentViewModel.documents.collectAsState()
  documentViewModel.getDocuments()
  val selectedTravel = listTravelViewModel.selectedTravel.collectAsState()

  val context = LocalContext.current
  val scannerOptions =
      GmsDocumentScannerOptions.Builder()
          .setScannerMode(SCANNER_MODE_FULL)
          .setGalleryImportAllowed(true)
          .setResultFormats(
              GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
              GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
          .build()
  val scanner = GmsDocumentScanning.getClient(scannerOptions)
  val scannerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
              val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(it.data)
              Log.d("DocumentList", "Scanning result: $scanningResult")
              scanningResult?.pages?.size?.let { size ->
                if (size == 1) {
                  val bytes = scanningResult.pages?.firstOrNull()?.imageUri?.toFile()?.readBytes()
                  if (bytes != null && selectedTravel.value != null) {
                    documentViewModel.uploadDocument(
                        selectedTravel.value!!.fsUid, bytes, DocumentFileFormat.JPEG)
                  }
                } else if (size > 1 && selectedTravel.value != null) {
                  scanningResult.pdf?.let { pdf ->
                    val bytes = pdf.uri.toFile().readBytes()
                    documentViewModel.uploadDocument(
                        selectedTravel.value!!.fsUid, bytes, DocumentFileFormat.PDF)
                  }
                }
              }
            }
          }

  // Create a launcher of the file picker to select a file
  val filePickerLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) {
          uri: Uri? ->
        if (uri != null)
            documentViewModel.uploadFile(
                context.contentResolver.openInputStream(uri),
                selectedTravel.value,
                context.contentResolver.getType(uri))
        else Log.d("DocumentList", "No file selected")
      }

  Scaffold(
      modifier = Modifier.testTag("documentListScreen"),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Travel's documents",
                  modifier = Modifier.semantics { testTag = "documentListTitle" })
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
            })
      },
      floatingActionButton = {
        var toggled by remember { mutableStateOf(false) }

        Column(horizontalAlignment = Alignment.End) {
          if (toggled) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {
              ExtendedFloatingActionButton(
                  text = {
                    Text(
                        "Import from local files",
                        modifier = Modifier.testTag("importLocalFileButtonText"))
                  },
                  icon = { Icon(Icons.Default.UploadFile, contentDescription = "Add Document") },
                  onClick = { filePickerLauncher.launch(DocumentFileFormat.ACCEPTED_MIME_TYPE) },
                  modifier = Modifier.testTag("importLocalFileButton"))

              Spacer(modifier = Modifier.height(8.dp))

              ExtendedFloatingActionButton(
                  text = {
                    Text("Scan with camera", modifier = Modifier.testTag("scanCamButtonText"))
                  },
                  icon = {
                    Icon(Icons.Default.DocumentScanner, contentDescription = "Add Document")
                  },
                  onClick = {
                    scanner.getStartScanIntent(context.findActivity()!!).addOnSuccessListener {
                        intentSender ->
                      scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                    }
                  },
                  modifier = Modifier.testTag("scanCamButton"))

              Spacer(modifier = Modifier.height(8.dp))

              FloatingActionButton(
                  onClick = { toggled = !toggled }, modifier = Modifier.testTag("dropDownButton")) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Add Document")
                  }
            }
          } else {
            FloatingActionButton(
                onClick = { toggled = !toggled }, modifier = Modifier.testTag("plusButton")) {
                  Icon(Icons.Default.Add, contentDescription = "Add Document")
                }
          }
        }
      }) { paddingValue ->
        Box(modifier = Modifier.fillMaxSize()) {
          Column(modifier = Modifier.fillMaxWidth().padding(paddingValue)) {
            var isRefreshing by remember { mutableStateOf(false) }
            val pullToRefreshState = rememberPullToRefreshState()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier =
                    Modifier.fillMaxWidth()
                        .pullToRefresh(
                            isRefreshing = isRefreshing,
                            state = pullToRefreshState,
                            onRefresh = {
                              isRefreshing = true
                              documentViewModel.getDocuments()
                              isRefreshing = false
                            })) {
                  items(documents.value.size) { index ->
                    DocumentListItem(
                        documents.value[index],
                        documentViewModel,
                        onClick = {
                          documentViewModel.selectDocument(documents.value[index])
                          onNavigateToDocumentPreview()
                        })
                  }
                }
          }

          // Update the CircularProgressIndicator appearance
          AnimatedVisibility(
              modifier = Modifier.align(Alignment.Center).fillMaxSize(0.2f),
              visible = isLoading.value,
              enter = fadeIn(animationSpec = tween(50)),
              exit = fadeOut(animationSpec = tween(300))) {
                CircularProgressIndicator(
                    modifier =
                        Modifier // Adjust spinner size fifth of screen height
                            .align(Alignment.Center) // Center the spinner
                            .testTag("loadingSpinner"),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp)
              }
        }
      }
}

/** Utility function to find the activity from a context. */
fun Context.findActivity(): ComponentActivity? =
    when (this) {
      is ComponentActivity -> this
      is ContextWrapper -> baseContext.findActivity()
      else -> null
    }
