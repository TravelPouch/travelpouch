package com.github.se.travelpouch.ui.documents

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.se.travelpouch.model.documents.DocumentViewModel

@Composable
fun StoreDocumentButton(
    documentViewModel: DocumentViewModel,
    modifier: Modifier = Modifier,
    onDirectoryPicked: (Uri) -> Unit
) {
  val openDirectoryLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) {
          uri: Uri? ->
        if (uri != null) {
          documentViewModel.setSaveDocumentFolder(uri)
          onDirectoryPicked(uri)
        } else Log.i("OpenDocumentButton", "Access to directory denied")
      }

  IconButton(
      onClick = {
        if (documentViewModel.saveDocumentFolder.value == Uri.EMPTY)
            openDirectoryLauncher.launch(null)
        else onDirectoryPicked(documentViewModel.saveDocumentFolder.value)
      },
      modifier = modifier) {
        Icon(
            imageVector = Icons.Default.FileDownload,
            contentDescription = "Download Document",
            modifier = Modifier.testTag("storeDocumentButtonIcon"))
      }
}
