package com.github.se.travelpouch.helper

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.documentfile.provider.DocumentFile
import com.google.firebase.storage.FirebaseStorage
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
    private val dataStore: DataStore<Preferences>
) {
  /**
   * Download a file described by source and store it in the folder pointed by destinationFolder.
   *
   * @param sourceMimeType The mimeType of the source file
   * @param sourceTitle The title of the source file
   * @param sourceRef The reference of the source file
   * @param destinationFolder A pointer to the folder in which to store the file
   */
  fun downloadFile(
      sourceMimeType: String,
      sourceTitle: String,
      sourceRef: String,
      destinationFolder: DocumentFile
  ): Deferred<Uri> {
    return CoroutineScope(Dispatchers.IO).async {
      val existingFile = fileInCacheOrNull(sourceRef)
      if (existingFile != null) {
        Log.d("FileDownloader", "File already downloaded as $existingFile")
        return@async existingFile
      }

      val file = destinationFolder.createFile(sourceMimeType, sourceTitle)

      if (file == null) {
        Log.e("DocumentViewModel", "Failed to create document file in specified directory")
        throw Exception("Failed to create document file in specified directory")
      }

      Log.d("FileDownloader", "Downloading file to ${file.uri}")

      val storageRef = storage.reference
      val documentRef = storageRef.child(sourceRef)
      val taskSnapshot = documentRef.stream.await()
      contentResolver.openOutputStream(file.uri)?.use { outputStream ->
        taskSnapshot.stream.use { it.copyTo(outputStream) }
      }
          ?: run {
            Log.e("FileDownloader", "Failed to open output stream for URI: ${file.uri}")
            throw Exception("Failed to open output stream for URI: ${file.uri}")
          }
      addRefToCache(sourceRef, file.uri.toString())
      return@async file.uri
    }
  }

  private suspend fun fileInCacheOrNull(sourceRef: String): Uri? {
    val documentUid = stringPreferencesKey(sourceRef)
    val pathFlow: Flow<String?> = dataStore.data.map { preferences -> preferences[documentUid] }
    return try {
      pathFlow.first()?.let {
        Uri.parse(it).takeIf { val afd = contentResolver.openAssetFileDescriptor(it, "r")
        afd?.close()
          afd != null
        }
      }
    } catch (_: NoSuchElementException) {
      null
    }
  }

  private suspend fun addRefToCache(sourceRef: String, path: String) {
    val documentUid = stringPreferencesKey(sourceRef)
    dataStore.edit { preferences -> preferences[documentUid] = path }
  }
}
