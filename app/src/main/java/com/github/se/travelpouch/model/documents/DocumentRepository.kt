package com.github.se.travelpouch.model.documents

import android.util.Log
import com.github.se.travelpouch.model.FirebasePaths
import com.google.android.gms.common.util.Base64Utils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage

/** Interface for the DocumentRepository. */
interface DocumentRepository {
  fun setIdTravel(onSuccess: () -> Unit, travelId: String)

  fun getDocuments(onSuccess: (List<DocumentContainer>) -> Unit, onFailure: (Exception) -> Unit)

  fun deleteDocumentById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getDownloadUrl(
      document: DocumentContainer,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun uploadDocument(
      travelId: String,
      bytes: ByteArray,
      format: DocumentFileFormat,
      onSuccess: () -> Unit,
      onFailure: () -> Int
  )
}

/**
 * Firestore implementation of the DocumentRepository.
 *
 * @property db The Firestore database instance.
 * @property firebaseAuth The Firebase authentication instance.
 */
class DocumentRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth,
    private val functions: FirebaseFunctions
) : DocumentRepository {
  private var collectionPath = ""

  override fun setIdTravel(onSuccess: () -> Unit, travelId: String) {
    val p1 = FirebasePaths.TravelsSuperCollection
    val p2 = FirebasePaths.documents
    collectionPath = FirebasePaths.constructPath(p1, travelId, p2)
    onSuccess()
  }

  /**
   * Fetches all documents from the Firestore database.
   *
   * @param onSuccess Callback function to be called when the documents are fetched successfully.
   * @param onFailure Callback function to be called when an error occurs.
   */
  override fun getDocuments(
      onSuccess: (List<DocumentContainer>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val documents =
            task.result?.documents?.mapNotNull { document -> fromSnapshot(document) } ?: emptyList()
        onSuccess(documents.sortedByDescending { it.addedAt })
      } else {
        task.exception?.let { e ->
          Log.e("DocumentRepositoryFirestore", "Error getting documents", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Deletes a document from the Firestore database.
   *
   * @param id The id of the document to be deleted.
   * @param onSuccess Callback function to be called when the document is deleted successfully.
   * @param onFailure Callback function to be called when an error occurs.
   */
  override fun deleteDocumentById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).document(id).delete().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        onSuccess()
      } else {
        task.exception?.let { e ->
          Log.e("DocumentRepositoryFirestore", "Error deleting document", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Fetches the download URL of a document from the Firestore database.
   *
   * @param document The document to fetch the download URL for.
   * @param onSuccess Callback function to be called when the download URL is fetched successfully.
   * @param onFailure Callback function to be called when an error occurs.
   */
  override fun getDownloadUrl(
      document: DocumentContainer,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    storage
        .getReference(document.ref.id)
        .downloadUrl
        .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
        .addOnFailureListener(onFailure)
  }

  private fun generateThumbnail(
      document: DocumentContainer,
      width: Int,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    functions
        .getHttpsCallable("generateThumbnailCall")
        .call(
            mapOf(
                "travelId" to document.travelRef.id,
                "documentId" to document.ref.id,
                "width" to width))
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            onSuccess()
          } else {
            Log.e(
                "DocumentRepositoryFirestore",
                "Error generating thumbnail for document id=${document.ref.id},width=$width",
                task.exception)
            onFailure(task.exception!!)
          }
        }
  }

  override fun uploadDocument(
      travelId: String,
      bytes: ByteArray,
      format: DocumentFileFormat,
      onSuccess: () -> Unit,
      onFailure: () -> Int
  ) {
    val bytes64 = Base64Utils.encodeUrlSafe(bytes)
    val scanTimestamp = Timestamp.now().seconds
    functions
        .getHttpsCallable("storeDocument")
        .call(
            mapOf(
                "content" to bytes64,
                "fileFormat" to format.mimeType,
                "title" to "Scan $scanTimestamp",
                "travelId" to travelId,
                "fileSize" to bytes.size,
                "visibility" to DocumentVisibility.PARTICIPANTS.toString()))
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            onSuccess()
          } else {
            Log.e("DocumentRepositoryFirestore", "Error uploading document", task.exception)
            onFailure()
          }
        }
  }

  /**
   * Converts a DocumentSnapshot representing a TravelPouch Document to a DocumentContainer.
   *
   * @param document The firebase document to be added.
   * @throws IllegalArgumentException if the document is not formatted properly.
   */
  private fun fromSnapshot(document: DocumentSnapshot): DocumentContainer {
    try {
      return DocumentContainer(
          ref = document.reference,
          travelRef = requireNotNull(document.getDocumentReference("travelRef")),
          activityRef = document.getDocumentReference("activityRef"),
          title = requireNotNull(document.getString("title")),
          fileFormat =
              requireNotNull(
                  DocumentFileFormat.fromMimeType(
                      requireNotNull(document.getString("fileFormat")))),
          fileSize = requireNotNull(document.getLong("fileSize")),
          addedByEmail = document.getString("addedByEmail"),
          addedByUser = document.getDocumentReference("addedByUser"),
          addedAt = requireNotNull(document.getTimestamp("addedAt")),
          visibility = enumValueOf(requireNotNull(document.getString("visibility"))))
    } catch (e: Exception) {
      throw IllegalArgumentException("Invalid document format", e)
    }
  }
}
