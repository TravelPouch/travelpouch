package com.github.se.travelpouch.model.documents

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

/**
 * Data class representing a document container.
 *
 * @property ref reference of this document
 * @property travelRef reference of the travel this document belongs to
 * @property activityRef optional, travel global if not specified
 * @property title user-defined or derived from original filename
 * @property fileFormat one of image/jpeg, image/png, application/pdf
 * @property fileSize in bytes, useful for offline storage later on
 * @property addedByEmail null or email of origin
 * @property addedByUser null or user who uploaded
 * @property addedAt timestamp of upload
 * @property visibility for later use, either: ME, ORGANIZERS, PARTICIPANTS
 */
data class DocumentContainer(
    val ref: DocumentReference,
    val travelRef: DocumentReference,
    val activityRef: DocumentReference?,
    val title: String,
    val fileFormat: DocumentFileFormat,
    val fileSize: Long,
    val addedByEmail: String?,
    val addedByUser: DocumentReference?,
    val addedAt: Timestamp,
    val visibility: DocumentVisibility
)

/**
 * Data class representing a new document container.
 *
 * @property title user-defined or derived from original filename
 * @property fileFormat one of image/jpeg, image/png, application/pdf
 * @property fileSize in bytes, useful for offline storage later on
 * @property addedAt timestamp of upload
 * @property visibility for later use, either: ME, ORGANIZERS, PARTICIPANTS
 */
data class NewDocumentContainer(
    val title: String,
    val travelRef: DocumentReference,
    val fileFormat: DocumentFileFormat,
    val fileSize: Long,
    val addedAt: Timestamp,
    val visibility: DocumentVisibility
)

/**
 * Enum class representing the file format of a document.
 *
 * @property JPEG image/jpeg
 * @property PNG image/png
 * @property PDF application/pdf
 */
enum class DocumentFileFormat(val mimeType: String) {
  JPEG("image/jpeg"),
  PNG("image/png"),
  PDF("application/pdf");

  companion object {
    fun fromMimeType(mimeType: String): DocumentFileFormat? {
      for (value in values()) {
        if (value.mimeType == mimeType) return value
      }
      return null
    }
  }
}

/**
 * Enum class representing the visibility of a document.
 *
 * @property ME only visible to the user who uploaded the document
 * @property ORGANIZERS only visible to the organizers of the travel
 * @property PARTICIPANTS visible to all participants of the travel
 */
enum class DocumentVisibility {
  ME,
  ORGANIZERS,
  PARTICIPANTS
}
