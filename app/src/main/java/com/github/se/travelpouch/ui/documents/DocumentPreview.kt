package com.github.se.travelpouch.ui.documents

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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions

/**
 * Composable function for previewing a document.
 *
 * @param documentViewModel the document view model with the current document set as selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentPreview(documentViewModel: DocumentViewModel, navigationActions: NavigationActions) {
  val documentContainer: DocumentContainer =
      documentViewModel.selectedDocument.collectAsState().value!!

  Scaffold(
      modifier = Modifier.testTag("documentListScreen"),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  documentContainer.title,
                  modifier = Modifier.semantics { testTag = "documentTitle" })
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
      Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.inversePrimary)) {
        Text(
            text = documentContainer.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(8.dp))
      }
    }
  }
}
