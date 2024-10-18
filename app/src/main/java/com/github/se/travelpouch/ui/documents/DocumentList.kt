package com.github.se.travelpouch.ui.documents

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
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions

/**
 * Composable function for displaying a list of documents.
 *
 * @param documentViewModel the document view model to use.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    documentViewModel: DocumentViewModel = viewModel(factory = DocumentViewModel.Factory),
    navigationActions: NavigationActions,
    onNavigateToDocumentPreview: () -> Unit
) {
  val documents = documentViewModel.documents.collectAsState()
  documentViewModel.getDocuments()

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
                  onClick = {},
                  modifier = Modifier.testTag("importLocalFileButton"))

              Spacer(modifier = Modifier.height(8.dp))

              ExtendedFloatingActionButton(
                  text = {
                    Text("Scan with camera", modifier = Modifier.testTag("scanCamButtonText"))
                  },
                  icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Add Document") },
                  onClick = {},
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
