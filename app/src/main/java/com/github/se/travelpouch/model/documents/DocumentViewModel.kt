package com.github.se.travelpouch.model.documents

import android.content.ContentResolver
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing documents and related operations.
 *
 * @property repository The repository used for accessing documents data.
 */
open class DocumentViewModel(private val repository: DocumentRepository) : ViewModel() {
  private val _documents = MutableStateFlow<List<DocumentContainer>>(emptyList())
  val documents: StateFlow<List<DocumentContainer>> = _documents.asStateFlow()
  private val _selectedDocument = MutableStateFlow<DocumentContainer?>(null)
  var selectedDocument: StateFlow<DocumentContainer?> = _selectedDocument.asStateFlow()

  init {
    repository.init { getDocuments() }
  }

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DocumentViewModel(DocumentRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

  /** Gets all Documents. */
  fun getDocuments() {
    repository.getDocuments(
        onSuccess = { _documents.value = it },
        onFailure = { Log.e("DocumentsViewModel", "Failed to get Documents", it) })
  }

  /**
   * Adds a Document.
   *
   * @param document The Document to be added.
   */
  fun createDocument(document: NewDocumentContainer) {
    repository.createDocument(
        document,
        onSuccess = { getDocuments() },
        onFailure = { Log.e("DocumentsViewModel", "Failed to create Document", it) })
  }

  /**
   * Downloads a Document from Firebase store adn store it in the folder pointed by documentFile
   *
   * @param documentFile The folder in which to create the file
   * @param contentResolver A content resolver to
   */
  fun storeSelectedDocument(documentFile: DocumentFile, contentResolver: ContentResolver) {
    val mimeType = selectedDocument.value?.fileFormat?.mimeType
    val title = selectedDocument.value?.title
    val ref = selectedDocument.value?.ref?.id

    if (mimeType == null || title == null || ref == null) {
      Log.i("DocumentViewModel", "Some fields are empty. Abort download")
      return
    }

    val file = documentFile.createFile(mimeType, title)

    if (file == null) {
      Log.e("DocumentViewModel", "Failed to create document file in specified directory")
      return
    }

    val storageRef = FirebaseStorage.getInstance("gs://travelpouch-7d692.appspot.com").reference
    val documentRef = storageRef.child(ref)

    val downloadTask = documentRef.stream
    downloadTask
        .addOnCompleteListener { taskSnapshot ->
          CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
              contentResolver.openOutputStream(file.uri)?.use { outputStream ->
                taskSnapshot.result.stream.use { it.copyTo(outputStream) }
              }
                  ?: run {
                    Log.e("DocumentViewModel", "Failed to open output stream for URI: ${file.uri}")
                  }
            }
          }
        }
        .addOnFailureListener { exception ->
          Log.e("DocumentViewModel", "Failed to download document", exception)
        }
  }

  //    /**
  //     * Updates a Document.
  //     *
  //     * @param document The Document to be updated.
  //     */
  //    fun updateDocument(document: DocumentContainer) {
  //        repository.updateDocument(document,
  //            onSuccess = { getDocuments() },
  //            onFailure = { Log.e("DocumentsViewModel", "Failed to update Document", it) })
  //    }

  /**
   * Deletes a Document by its ID.
   *
   * @param id The ID of the Document to be deleted.
   */
  fun deleteDocumentById(id: String) {
    repository.deleteDocumentById(
        id,
        onSuccess = { getDocuments() },
        onFailure = { Log.e("DocumentsViewModel", "Failed to delete Document", it) })
  }

  /** Defines selected document for the preview */
  fun selectDocument(document: DocumentContainer) {
    _selectedDocument.value = document
  }
}
