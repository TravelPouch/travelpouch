package com.github.se.travelpouch.helper

import android.content.ContentResolver
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Helper class to download files from Firbase storage to local files */
open class FileDownloader(
    private val contentResolver: ContentResolver,
    private val storage: FirebaseStorage
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
  ): Job {
    val file = destinationFolder.createFile(sourceMimeType, sourceTitle)

    if (file == null) {
      Log.e("DocumentViewModel", "Failed to create document file in specified directory")
      return Job().apply { complete() }
    }

    val storageRef = storage.reference
    val documentRef = storageRef.child(sourceRef)
    val downloadTask = documentRef.stream
    val job = Job()
    val coroutine = CoroutineScope(Dispatchers.IO + job)
    downloadTask
        .addOnCompleteListener { taskSnapshot ->
          coroutine.launch {
            contentResolver.openOutputStream(file.uri)?.use { outputStream ->
              taskSnapshot.result.stream.use { it.copyTo(outputStream) }
            }
                ?: run {
                  Log.e("FileDownloader", "Failed to open output stream for URI: ${file.uri}")
                }
            job.complete()
          }
        }
        .addOnFailureListener { exception ->
          Log.e("FileDownloader", "Failed to download document", exception)
          job.completeExceptionally(exception)
        }
    return job
  }
}
