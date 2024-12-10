package com.github.se.travelpouch.helper

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.nio.file.Files
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.never

@RunWith(MockitoJUnitRunner::class)
class DocumentsManagerTest {

  private lateinit var contentResolver: ContentResolver
  private lateinit var mockDocumentFile: DocumentFile
  private lateinit var mockUri: Uri
  private lateinit var mockDestinationFolder: DocumentFile
  private lateinit var mockFirebaseStorage: FirebaseStorage

  @Before
  fun setUp() {
    mockDocumentFile = mock(DocumentFile::class.java)
    mockUri = mock(Uri::class.java)
    mockDestinationFolder = mock(DocumentFile::class.java)
    mockFirebaseStorage = mock(FirebaseStorage::class.java)
    val context = ApplicationProvider.getApplicationContext<Context>()
    contentResolver = context.contentResolver
    FirebaseApp.initializeApp(context)
  }

  @Test
  fun assertUnableToCreateFile() {
    val documentsManager = DocumentsManager(contentResolver, mockFirebaseStorage)
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.executeShellCommand("logcat -c")
    `when`(mockDestinationFolder.createFile(any(), any())).thenReturn(null)
    documentsManager.downloadFile(
        "image/jpeg", "mountain.jpg", "hWwbmtbnfwX5yRhAwL3o", mockDestinationFolder)
    verify(mockFirebaseStorage, never()).reference
    val logs = device.executeShellCommand("logcat -d")
    assert(logs.contains("Failed to create document file in specified directory"))
  }

  @Test
  fun assertFileDownload() {
    val SIZE = 1024
    val SEED = 7112024L

    val tempFile = Files.createTempFile("testDownload", ".tmp").toFile()
    val uri = Uri.fromFile(tempFile)

    `when`(mockDestinationFolder.createFile(any(), any())).thenReturn(mockDocumentFile)
    `when`(mockDocumentFile.uri).thenReturn(uri)

    val storage =
        FirebaseStorage.getInstance("gs://travelpouch-7d692.appspot.com").apply {
          useEmulator("10.0.2.2", 9199)
        }
    val data = ByteArray(SIZE)
    Random(SEED).nextBytes(data)
    val auth = Firebase.auth.apply { useEmulator("10.0.2.2", 9099) }

    runBlocking {
      val taskSnapshot = storage.reference.child("hWwbmtbnfwX5yRhAwL3o").putBytes(data)
      auth.signInAnonymously().await()
      taskSnapshot.await()
      taskSnapshot.result

      DocumentsManager(contentResolver, storage)
          .downloadFile("image/jpeg", "mountain.jpg", "hWwbmtbnfwX5yRhAwL3o", mockDestinationFolder)
          .join()
    }
    val downloadedData = Files.readAllBytes(tempFile.toPath())
    assert(downloadedData.contentEquals(data))
  }
}
