package com.github.se.travelpouch.model.documents

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import com.github.se.travelpouch.helper.FileDownloader
import com.github.se.travelpouch.model.travels.TravelContainer
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing documents and related operations.
 *
 * @property repository The repository used for accessing documents data.
 * @property fileDownloader The file downloader to use when downloading files
 */
@HiltViewModel
open class DocumentViewModel
@Inject
constructor(
    private val repository: DocumentRepository,
    private val fileDownloader: FileDownloader
) : ViewModel() {
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
  private val _documents = MutableStateFlow<List<DocumentContainer>>(emptyList())
  val documents: StateFlow<List<DocumentContainer>> = _documents.asStateFlow()
  private val _selectedDocument = MutableStateFlow<DocumentContainer?>(null)
  var selectedDocument: StateFlow<DocumentContainer?> = _selectedDocument.asStateFlow()
  private val _saveDocumentFolder = MutableStateFlow<Uri>(Uri.EMPTY)
  val saveDocumentFolder: StateFlow<Uri> = _saveDocumentFolder.asStateFlow()
  private val _downloadUrls = mutableStateMapOf<String, String>()
  val downloadUrls: Map<String, String>
    get() = _downloadUrls

  private val _thumbnailUrls = mutableStateMapOf<String, String>()
  val thumbnailUrls: Map<String, String>
    get() = _thumbnailUrls

  fun setIdTravel(travelId: String) {
    repository.setIdTravel({ getDocuments() }, travelId)
  }

  /** Gets all Documents. */
  fun getDocuments() {
    repository.getDocuments(
        onSuccess = { _documents.value = it },
        onFailure = { Log.e("DocumentsViewModel", "Failed to get Documents", it) })
  }

  /**
   * Downloads a Document from Firebase store adn store it in the folder pointed by documentFile
   *
   * @param documentFile The folder in which to create the file
   */
  fun storeSelectedDocument(documentFile: DocumentFile): Job {
    val mimeType = selectedDocument.value?.fileFormat?.mimeType
    val title = selectedDocument.value?.title
    val ref = selectedDocument.value?.ref?.id

    if (mimeType == null || title == null || ref == null) {
      return Job().apply {
        completeExceptionally(
            IllegalArgumentException("Some required fields are empty. Abort download"))
      }
    }

    return fileDownloader.downloadFile(mimeType, title, ref, documentFile)
  }

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

  fun setSaveDocumentFolder(uri: Uri) {
    _saveDocumentFolder.value = uri
  }

  fun getDownloadUrl(document: DocumentContainer) {
    if (_downloadUrls.containsKey(document.ref.id)) {
      return
    }
    repository.getDownloadUrl(
        document,
        onSuccess = { _downloadUrls[document.ref.id] = it },
        onFailure = { Log.e("DocumentsViewModel", "Failed to get thumbnail uri", it) })
  }

  fun getDocumentThumbnail(document: DocumentContainer, width: Int = 300) {
    if (_thumbnailUrls.containsKey("${document.ref.id}-thumb-$width")) {
      return
    }
    repository.getThumbnailUrl(
        document,
        width,
        onSuccess = { _thumbnailUrls["${document.ref.id}-thumb-$width"] = it },
        onFailure = { Log.e("DocumentsViewModel", "Failed to get thumbnail uri", it) })
  }

  fun uploadDocument(travelId: String, bytes: ByteArray, format: DocumentFileFormat) {
    _isLoading.value = true // set as loading for spinner
    repository.uploadDocument(
        travelId,
        bytes,
        format,
        onSuccess = {
          getDocuments()
          _isLoading.value = false // set as not loading
        },
        onFailure = {
          _isLoading.value = false // set as not loading
          Log.e("DocumentsViewModel", "Failed to upload Document")
        })
  }

  /**
   * Uploads a file to the selected travel.
   *
   * @param inputStream The input stream of the file to upload.
   * @param selectedTravel The travel to which the file should be uploaded.
   * @param mimeType The mime type of the file.
   */
  fun uploadFile(inputStream: InputStream?, selectedTravel: TravelContainer?, mimeType: String?) {
    if (inputStream == null) {
      Log.e("DocumentViewModel", "No input stream")
      return
    }
    if (selectedTravel == null) {
      Log.e("DocumentViewModel", "No travel selected")
      return
    }
    val format = DocumentFileFormat.fromMimeType(mimeType)
    if (format == null) {
      Log.e("DocumentViewModel", "No or invalid mime type")
      return
    }
    val travelId = selectedTravel.fsUid
    val byteArrayOutputStream = ByteArrayOutputStream()
    inputStream.copyTo(byteArrayOutputStream)
    val bytes: ByteArray = byteArrayOutputStream.toByteArray()

    uploadDocument(travelId, bytes, format)
  }
}
