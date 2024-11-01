package com.github.se.travelpouch.model.documents

import android.content.ContentResolver
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.travelpouch.helper.FileDownloader
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing documents and related operations.
 *
 * @property repository The repository used for accessing documents data.
 * @property fileDownloader The file downloader to use when downloading files
 */
open class DocumentViewModel(
    private val repository: DocumentRepository,
    private val fileDownloader: FileDownloader
) : ViewModel() {
  private val _documents = MutableStateFlow<List<DocumentContainer>>(emptyList())
  val documents: StateFlow<List<DocumentContainer>> = _documents.asStateFlow()
  private val _selectedDocument = MutableStateFlow<DocumentContainer?>(null)
  var selectedDocument: StateFlow<DocumentContainer?> = _selectedDocument.asStateFlow()
  private val _downloadUrls = mutableStateMapOf<String, String>()
  val downloadUrls: Map<String, String>
    get() = _downloadUrls

  init {
    repository.init { getDocuments() }
  }

  // create factory
  companion object {
    fun Factory(contentResolver: ContentResolver): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DocumentViewModel(
                DocumentRepositoryFirestore(Firebase.firestore), FileDownloader(contentResolver))
                as T
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
  fun storeSelectedDocument(documentFile: DocumentFile) {
    val mimeType = selectedDocument.value?.fileFormat?.mimeType
    val title = selectedDocument.value?.title
    val ref = selectedDocument.value?.ref?.id

    if (mimeType == null || title == null || ref == null) {
      Log.i("DocumentViewModel", "Some required fields are empty. Abort download")
      return
    }

    fileDownloader.downloadFile(mimeType, title, ref, documentFile)
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

  fun getDownloadUrl(document: DocumentContainer) {
    if (_downloadUrls.containsKey(document.ref.id)) {
      return
    }
    repository.getDownloadUrl(
        document,
        onSuccess = { _downloadUrls[document.ref.id] = it },
        onFailure = { Log.e("DocumentPreview", "Failed to get image uri", it) })
  }

  fun uploadDocument(travelId: String, bytes: ByteArray, format: DocumentFileFormat) {
    repository.uploadDocument(
        travelId,
        bytes,
        format,
        onSuccess = { getDocuments() },
        onFailure = { Log.e("DocumentsViewModel", "Failed to upload Document") })
  }
}
