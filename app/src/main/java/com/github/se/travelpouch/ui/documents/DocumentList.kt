package com.github.se.travelpouch.ui.documents

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.android.gms.common.util.Base64Utils
import com.google.firebase.Timestamp
import com.google.firebase.functions.FirebaseFunctions
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
fun DocumentList(
    documentViewModel: DocumentViewModel = viewModel(factory = DocumentViewModel.Factory),
    navigationActions: NavigationActions,
    onNavigateToDocumentPreview: () -> Unit
) {
  val documents = documentViewModel.documents.collectAsState()
  documentViewModel.getDocuments()

  val functions = FirebaseFunctions.getInstance("europe-west9")
  val context = LocalContext.current
  val scannerOptions =
      GmsDocumentScannerOptions.Builder()
          .setScannerMode(SCANNER_MODE_FULL)
          .setGalleryImportAllowed(true)
          .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
          .build()
  val scanner = GmsDocumentScanning.getClient(scannerOptions)
  val scannerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
              val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(it.data)
              Log.d("DocumentList", "Scanning result: $scanningResult")
              scanningResult?.pdf?.let { pdf ->
                val bytes = pdf.uri.toFile().readBytes()
                val bytes64 = Base64Utils.encodeUrlSafe(bytes)
                val scanTimestamp = Timestamp.now().seconds
                functions
                    .getHttpsCallable("storeDocument")
                    .call(
                        mapOf(
                            "content" to bytes64,
                            "fileFormat" to "application/pdf",
                            "title" to "Scan ${scanTimestamp}.pdf",
                            "travelId" to "$scanTimestamp",
                            "fileSize" to bytes.size,
                            "visibility" to "PARTICIPANTS"))
                    .continueWith { task ->
                      val result = task.result?.data
                      Log.d("DocumentList", "Function storeDocument result: $result")
                      documentViewModel.getDocuments()
                    }
              }
            }
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
                  text = { Text("Import from local files") },
                  icon = { Icon(Icons.Default.UploadFile, contentDescription = "Add Document") },
                  onClick = {})

              Spacer(modifier = Modifier.height(8.dp))

              ExtendedFloatingActionButton(
                  text = { Text("Scan with camera") },
                  icon = {
                    Icon(Icons.Default.DocumentScanner, contentDescription = "Add Document")
                  },
                  onClick = {
                    scanner.getStartScanIntent(context.findActivity()!!).addOnSuccessListener {
                        intentSender ->
                      scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                    }
                  })

              Spacer(modifier = Modifier.height(8.dp))

              FloatingActionButton(
                  onClick = { toggled = !toggled },
              ) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Add Document")
              }
            }
          } else {
            FloatingActionButton(
                onClick = { toggled = !toggled },
            ) {
              Icon(Icons.Default.Add, contentDescription = "Add Document")
            }
          }
        }
      }) { paddingValue ->
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
                      onClick = {
                        documentViewModel.selectDocument(documents.value[index])
                        onNavigateToDocumentPreview()
                      })
                }
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
