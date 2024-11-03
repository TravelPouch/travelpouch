package com.github.se.travelpouch.model.documents

import android.util.Log
import com.google.android.gms.common.util.Base64Utils
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage

/** Interface for the DocumentRepository. */
interface DocumentRepository {
  fun init(onSuccess: () -> Unit)

  fun getDocuments(onSuccess: (List<DocumentContainer>) -> Unit, onFailure: (Exception) -> Unit)

  fun createDocument(
      document: NewDocumentContainer,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

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
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val firebaseAuth: FirebaseAuth = Firebase.auth,
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("europe-west9")
) : DocumentRepository {
  private val collectionPath = "documents"

  override fun init(onSuccess: () -> Unit) {
    firebaseAuth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
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
   * Adds a new document to the Firestore database using the simpler version of a DocumentContainer
   * without the ref/uid.
   *
   * @param document The document to be added.
   * @param onSuccess Callback function to be called when the document is added successfully.
   * @param onFailure Callback function to be called when an error occurs.
   */
  override fun createDocument(
      document: NewDocumentContainer,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).add(toMap(document)).addOnCompleteListener { task ->
      if (task.isSuccessful) {
        onSuccess()
      } else {
        task.exception?.let { e ->
          Log.e("DocumentRepositoryFirestore", "Error adding document", e)
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
        .continueWith { task ->
          if (task.isSuccessful) {
            onSuccess()
          } else {
            Log.e("DocumentRepositoryFirestore", "Error uploading document", task.exception)
            onFailure()
          }
        }
  }

  /**
   * Extracts the DocumentVisibility from a DocumentSnapshot representing a TravelPouch Document.
   *
   * @param snapshot The firebase DocumentSnapshot to be added.
   * @throws IllegalArgumentException if the document is not formatted properly.
   */
  private fun visibilityFromSnapshot(snapshot: DocumentSnapshot): DocumentVisibility? {
    return when (snapshot.getString("visibility")) {
      "ME" -> DocumentVisibility.ME
      "ORGANIZERS" -> DocumentVisibility.ORGANIZERS
      "PARTICIPANTS" -> DocumentVisibility.PARTICIPANTS
      else -> null
    }
  }

  /**
   * Converts a DocumentVisibility to a String for database storage.
   *
   * @param visibility The firebase document to be added.
   * @throws IllegalArgumentException if the document is not formatted properly.
   */
  private fun visibilityToString(visibility: DocumentVisibility): String {
    return when (visibility) {
      DocumentVisibility.ME -> "ME"
      DocumentVisibility.ORGANIZERS -> "ORGANIZERS"
      DocumentVisibility.PARTICIPANTS -> "PARTICIPANTS"
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
          visibility = requireNotNull(visibilityFromSnapshot(document)))
    } catch (e: Exception) {
      throw IllegalArgumentException("Invalid document format", e)
    }
  }

  /**
   * Converts a NewDocumentContainer to a Map for database storage.
   *
   * @param document The firebase document to be added.
   */
  private fun toMap(document: NewDocumentContainer): Map<String, Any?> {
    return mapOf(
        "title" to document.title,
        "travelRef" to document.travelRef,
        "fileFormat" to document.fileFormat.mimeType,
        "fileSize" to document.fileSize,
        "addedAt" to document.addedAt,
        "visibility" to visibilityToString(document.visibility))
  }
}
