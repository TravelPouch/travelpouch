// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.documents

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.documentfile.provider.DocumentFile
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StreamDownloadTask
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/** Helper class to download files from Firbase storage to local files */
open class DocumentsManager(
    private val contentResolver: ContentResolver,
    private val storage: FirebaseStorage,
    private val functions: FirebaseFunctions,
    private val dataStore: DataStore<Preferences>,
    private val thumbsDirectory: File
) {
  private val tag = DocumentsManager::class.java.simpleName

  /**
   * Return a Uri pointing to the file in the local storage.The file is firstly downloaded from
   * Firebase storage if not already present.
   *
   * @param sourceMimeType The mimeType of the source file
   * @param sourceTitle The title of the source file
   * @param sourceRef The reference of the source file
   * @param destinationFolder A pointer to the folder in which to store the file
   */
  fun getDocument(
      sourceMimeType: String,
      sourceTitle: String,
      sourceRef: String,
      destinationFolder: DocumentFile
  ): Deferred<Uri> {
    return CoroutineScope(Dispatchers.IO).async {
      val existingFile = documentInCacheOrNull(sourceRef)
      if (existingFile != null) {
        Log.d(tag, "File already downloaded as $existingFile")
        return@async existingFile
      }

      val file = destinationFolder.createFile(sourceMimeType, sourceTitle)

      if (file == null) {
        Log.e("DocumentViewModel", "Failed to create document file in specified directory")
        throw Exception("Failed to create document file in specified directory")
      }

      Log.d(tag, "Downloading file to ${file.uri}")

      val storageRef = storage.reference
      val documentRef = storageRef.child(sourceRef)
      val taskSnapshot = documentRef.stream.await()
      contentResolver.openOutputStream(file.uri)?.use { outputStream ->
        taskSnapshot.stream.use { it.copyTo(outputStream) }
      }
          ?: run {
            Log.e(tag, "Failed to open output stream for URI: ${file.uri}")
            throw Exception("Failed to open output stream for URI: ${file.uri}")
          }
      addDocumentRefToCache(sourceRef, file.uri.toString())
      return@async file.uri
    }
  }

  /**
   * Return a Uri pointing to the file in the local storage if it is already present.
   *
   * @param sourceRef The reference of the source file
   * @return The Uri pointing to the file in the local storage or null if the file is not present.
   */
  private suspend fun documentInCacheOrNull(sourceRef: String): Uri? {
    val documentUid = stringPreferencesKey(sourceRef)
    val pathFlow: Flow<String?> = dataStore.data.map { preferences -> preferences[documentUid] }
    return try {
      pathFlow.first()?.let {
        Uri.parse(it).takeIf {
          val afd = contentResolver.openAssetFileDescriptor(it, "r")
          afd?.close()
          afd != null
        }
      }
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Add an entry in the cache.
   *
   * @param sourceRef The reference of the source file
   * @param path The path of the file in the local storage
   */
  private suspend fun addDocumentRefToCache(sourceRef: String, path: String) {
    val documentUid = stringPreferencesKey(sourceRef)
    dataStore.edit { preferences -> preferences[documentUid] = path }
  }

  /**
   * Return a Uri pointing to the thumbnail in the local storage. The thumbnail is firstly
   * downloaded from Firebase storage if not already present.
   *
   * @param sourceRef The reference of the source file
   * @param size The width of the thumbnail
   */
  open fun getThumbnail(travelRef: String, sourceRef: String, size: Int): Deferred<Uri> {
    return CoroutineScope(Dispatchers.IO).async {
      val file = File(thumbsDirectory, "$sourceRef-$size")
      if (file.exists()) {
        Log.d(tag, "Thumbnail already downloaded as ${file.toUri()}")
        return@async file.toUri()
      }

      Log.d(tag, "Downloading thumbnail to ${file.toUri()}")

      val taskSnapshot =
          thumbnailInputStream(travelRef, sourceRef, size)
              ?: throw Exception("Failed to download thumbnail")
      file.outputStream().use { taskSnapshot.stream.copyTo(it) }
      return@async file.toUri()
    }
  }

  private suspend fun thumbnailInputStream(
      travelRef: String,
      sourceRef: String,
      size: Int
  ): StreamDownloadTask.TaskSnapshot? {
    val storageRef = storage.reference
    val thumbRef = storageRef.child("$sourceRef-thumb-$size")
    return try {
      thumbRef.stream.await()
    } catch (e: Exception) {
      Log.d(tag, "First download try failed, generating thumbnail")
      if (generateThumbnail(travelRef, sourceRef, size)) {
        try {
          thumbRef.stream.await()
        } catch (e: Exception) {
          Log.e(tag, "Second download try failed. Abort", e)
          null
        }
      } else {
        null
      }
    }
  }

  private suspend fun generateThumbnail(travelRef: String, sourceRef: String, width: Int): Boolean {
    return try {
      functions
          .getHttpsCallable("generateThumbnailCall")
          .call(mapOf("travelId" to travelRef, "documentId" to sourceRef, "width" to width))
          .await()
      true
    } catch (e: Exception) {
      Log.e(tag, "Error generating thumbnail for document id=${sourceRef},width=$width", e)
      false
    }
  }
}
