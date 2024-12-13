package com.github.se.travelpouch.model.documents

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.documentfile.provider.DocumentFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import java.nio.file.Files
import kotlin.random.Random

@RunWith(MockitoJUnitRunner::class)
class DocumentsManagerTest {

  private lateinit var contentResolver: ContentResolver
  private lateinit var mockDataStore: DataStore<Preferences>
  private lateinit var mockDocumentFile: DocumentFile
  private lateinit var mockUri: Uri
  private lateinit var mockDestinationFolder: DocumentFile
  private lateinit var mockFirebaseStorage: FirebaseStorage

  @Before
  fun setUp() {
    mockDocumentFile = Mockito.mock(DocumentFile::class.java)
    mockUri = Mockito.mock(Uri::class.java)
    mockDestinationFolder = Mockito.mock(DocumentFile::class.java)
    mockFirebaseStorage = Mockito.mock(FirebaseStorage::class.java)
    val context = ApplicationProvider.getApplicationContext<Context>()
    contentResolver = context.contentResolver
    mockDataStore = mock()
    FirebaseApp.initializeApp(context)
  }

  @Test
  fun assertUnableToCreateFile() {

    `when`(mockDataStore.data).thenReturn(flowOf(preferencesOf()))
    val documentsManager = DocumentsManager(contentResolver, mockFirebaseStorage, mockDataStore, mock())
    `when`(mockDestinationFolder.createFile(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(null)
      documentsManager.getDocument(
        "image/jpeg", "mountain.jpg", "hWwbmtbnfwX5yRhAwL3o", mockDestinationFolder
      ).invokeOnCompletion {
        assert(it is Exception) {"Expected an exception but got $it"}
        assertEquals(it?.message, "Failed to create document file in specified directory")
        Mockito.verify(mockFirebaseStorage, never()).reference
      }
  }

  @Test
  fun assertFileDownload() {
    val SIZE = 1024
    val SEED = 7112024L

    val tempFile = Files.createTempFile("testDownload", ".tmp").toFile()
    val uri = Uri.fromFile(tempFile)

    `when`(mockDestinationFolder.createFile(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(mockDocumentFile)
    `when`(mockDocumentFile.uri).thenReturn(uri)
    `when`(mockDataStore.data).thenReturn(flowOf(preferencesOf()))

    val storage =
        FirebaseStorage.getInstance("gs://travelpouch-7d692.appspot.com").apply {
          useEmulator("10.0.2.2", 9199)
        }
    val data = ByteArray(SIZE)
    Random(SEED).nextBytes(data)
    val auth = Firebase.auth.apply { useEmulator("10.0.2.2", 9099) }

    runBlocking {
      auth.signInAnonymously().await()
      val taskSnapshot = storage.reference.child("hWwbmtbnfwX5yRhAwL3o").putBytes(data)
      taskSnapshot.await()
      taskSnapshot.result

      DocumentsManager(contentResolver, storage, mockDataStore, mock())
        .getDocument("image/jpeg", "mountain.jpg", "hWwbmtbnfwX5yRhAwL3o", mockDestinationFolder)
        .join()
    }
    val downloadedData = Files.readAllBytes(tempFile.toPath())
    assert(downloadedData.contentEquals(data))
  }
}