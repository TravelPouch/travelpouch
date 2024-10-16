package com.github.se.travelpouch.model.documents

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing documents and related operations.
 *
 * @property repository The repository used for accessing documents data.
 */
open class DocumentViewModel(private val repository: DocumentRepository) : ViewModel() {
    private val _documents = MutableStateFlow<List<DocumentContainer>>(emptyList())
    val documents: StateFlow<List<DocumentContainer>> = _documents.asStateFlow()

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
        repository.createDocument(document,
            onSuccess = { getDocuments() },
            onFailure = { Log.e("DocumentsViewModel", "Failed to create Document", it) })
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
//
//    /**
//     * Deletes a Document by its ID.
//     *
//     * @param id The ID of the Document to be deleted.
//     */
//    fun deleteDocumentById(id: String) {
//        repository.deleteDocumentById(id,
//            onSuccess = { getDocuments() },
//            onFailure = { Log.e("DocumentsViewModel", "Failed to delete Document", it) })
//    }

}
