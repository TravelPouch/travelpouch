package com.github.se.travelpouch.model.documents

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.travels.TravelContainer
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for managing documents and related operations.
 *
 * @property repository The repository used for accessing documents data.
 * @property documentsManager The file downloader to use when downloading files
 */
@HiltViewModel
open class DocumentViewModel
@Inject
constructor(
    private val repository: DocumentRepository,
    private val documentsManager: DocumentsManager,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
  private val _documents = MutableStateFlow<List<DocumentContainer>>(emptyList())
  val documents: StateFlow<List<DocumentContainer>> = _documents.asStateFlow()
  private val _selectedDocument = MutableStateFlow<DocumentContainer?>(null)
  var selectedDocument: StateFlow<DocumentContainer?> = _selectedDocument.asStateFlow()
  private val _documentUri = mutableStateOf<Uri?>(null)
  open val documentUri: State<Uri?>
    get() = _documentUri

  private val _thumbnailUris = mutableStateMapOf<String, Uri>()
  val thumbnailUris: Map<String, Uri>
    get() = _thumbnailUris

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
  @OptIn(ExperimentalCoroutinesApi::class)
  fun getSelectedDocument(documentFile: DocumentFile) {
    _documentUri.value = null
    val mimeType = selectedDocument.value?.fileFormat?.mimeType
    val title = selectedDocument.value?.title
    val ref = selectedDocument.value?.ref?.id

    if (mimeType == null || title == null || ref == null) {
      throw IllegalArgumentException("Some required fields are empty. Abort download")
    }

    val result = documentsManager.getDocument(mimeType, title, ref, documentFile)
    result.invokeOnCompletion {
      if (it != null) {
        Log.e("DocumentViewModel", "Failed to download document", it)
      } else {
        _documentUri.value = result.getCompleted()
        Log.d("DocumentViewModel", "Document retrieved as ${result.getCompleted()}")
      }
    }
  }

  /**
   * Deletes a Document by its ID.
   *
   * @param id The ID of the Document to be deleted.
   */
  fun deleteDocumentById(document: DocumentContainer, activityList: List<Activity>) {
    repository.deleteDocumentById(
        document,
        activityList,
        onSuccess = { getDocuments() },
        onFailure = { Log.e("DocumentsViewModel", "Failed to delete Document", it) })
  }

  /** Defines selected document for the preview */
  fun selectDocument(document: DocumentContainer) {
    _selectedDocument.value = document
  }

  fun setSaveDocumentFolder(uri: Uri?): Job {
    return CoroutineScope(Dispatchers.Default).launch {
      if (uri == null) {
        return@launch
      }
      dataStore.edit { preferences -> preferences[SAVE_DOCUMENT_FOLDER] = uri.toString() }
    }
  }

  fun getSaveDocumentFolder(): Deferred<Uri?> {
    return CoroutineScope(Dispatchers.Default).async {
      dataStore.data
          .map { parameters -> parameters[SAVE_DOCUMENT_FOLDER] }
          .first()
          ?.let { Uri.parse(it) }
    }
  }

  /**
   * If not already present, put the uri of a thumbnail of certain width in the active cache.
   *
   * @param document The document to get the thumbnail for.
   * @param width The width of the requested thumbnail.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  fun getDocumentThumbnail(document: DocumentContainer, width: Int = 300) {
    if (_thumbnailUris.containsKey("${document.ref.id}-$width")) {
      return
    }
    val deferredUri = documentsManager.getThumbnail(document.travelRef.id, document.ref.id, width)
    deferredUri.invokeOnCompletion {
      if (it != null) {
        Log.e("DocumentsViewModel", "Failed to get thumbnail", it)
      } else {
        _thumbnailUris["${document.ref.id}-$width"] = deferredUri.getCompleted()
      }
    }
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

  companion object {
    private val SAVE_DOCUMENT_FOLDER = stringPreferencesKey("save_document_folder")
  }
}
