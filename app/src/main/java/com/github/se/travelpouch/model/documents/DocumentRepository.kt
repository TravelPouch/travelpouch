package com.github.se.travelpouch.model.documents

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

interface DocumentRepository {
    fun init(onSuccess: () -> Unit)
    fun getDocuments(onSuccess: (List<DocumentContainer>) -> Unit, onFailure: (Exception) -> Unit)
    fun createDocument(
        document: NewDocumentContainer,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

//    fun updateDocument(
//        document: DocumentContainer,
//        onSuccess: () -> Unit,
//        onFailure: (Exception) -> Unit
//    )
//
//    fun deleteDocumentById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}

class DocumentRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) : DocumentRepository {
    private val collectionPath = "documents"

    override fun init(onSuccess: () -> Unit) {
        firebaseAuth.addAuthStateListener {
            if (it.currentUser != null) {
                onSuccess()
            }
        }
    }

    override fun getDocuments(
        onSuccess: (List<DocumentContainer>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(collectionPath).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents =
                    task.result?.documents?.mapNotNull { document -> fromSnapshot(document) }
                        ?: emptyList()
                onSuccess(documents)
            } else {
                task.exception?.let { e ->
                    Log.e("DocumentRepositoryFirestore", "Error getting documents", e)
                    onFailure(e)
                }
            }
        }
    }

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

    private fun fileFormatFromSnapshot(snapshot: DocumentSnapshot): DocumentFileFormat? {
        return when (snapshot.getString("fileFormat")) {
            "image/jpeg" -> DocumentFileFormat.JPEG
            "image/png" -> DocumentFileFormat.PNG
            "application/pdf" -> DocumentFileFormat.PDF
            else -> null
        }
    }

    private fun fileFormatToString(fileFormat: DocumentFileFormat): String {
        return when (fileFormat) {
            DocumentFileFormat.JPEG -> "image/jpeg"
            DocumentFileFormat.PNG -> "image/png"
            DocumentFileFormat.PDF -> "application/pdf"
        }
    }

    private fun visibilityFromSnapshot(snapshot: DocumentSnapshot): DocumentVisibility? {
        return when (snapshot.getString("visibility")) {
            "ME" -> DocumentVisibility.ME
            "ORGANIZERS" -> DocumentVisibility.ORGANIZERS
            "PARTICIPANTS" -> DocumentVisibility.PARTICIPANTS
            else -> null
        }
    }

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
                fileFormat = requireNotNull(fileFormatFromSnapshot(document)),
                fileSize = requireNotNull(document.getLong("fileSize")),
                addedByEmail = document.getString("addedByEmail"),
                addedByUser = document.getDocumentReference("addedByUser"),
                addedAt = requireNotNull(document.getTimestamp("addedAt")),
                visibility = requireNotNull(visibilityFromSnapshot(document))
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid document format", e)
        }
    }

    private fun toMap(document: NewDocumentContainer): Map<String, Any?> {
        return mapOf(
            "title" to document.title,
            "travelRef" to document.travelRef,
            "fileFormat" to fileFormatToString(document.fileFormat),
            "fileSize" to document.fileSize,
            "addedAt" to document.addedAt,
            "visibility" to visibilityToString(document.visibility)
        )
    }
}
