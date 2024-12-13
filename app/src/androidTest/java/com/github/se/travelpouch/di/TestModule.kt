package com.github.se.travelpouch.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.github.se.travelpouch.R
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityRepositoryFirebase
import com.github.se.travelpouch.model.authentication.AuthenticationService
import com.github.se.travelpouch.model.authentication.FirebaseAuthenticationService
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentRepositoryFirestore
import com.github.se.travelpouch.model.documents.DocumentsManager
import com.github.se.travelpouch.model.events.EventRepository
import com.github.se.travelpouch.model.events.EventRepositoryFirebase
import com.github.se.travelpouch.model.notifications.NotificationRepository
import com.github.se.travelpouch.model.notifications.NotificationRepositoryFirestore
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.profile.ProfileRepositoryFirebase
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.model.travels.TravelRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val EMULATORS_ADDRESS = "10.0.2.2"

private const val AUTH_PORT = 9099
private const val FIRESTORE_PORT = 8080
private const val STORAGE_PORT = 9199
private const val FUNCTIONS_PORT = 5001

@InstallIn(SingletonComponent::class)
@Module
object TestModule {

  @Provides
  @Singleton
  fun provideFirebaseAuth(): FirebaseAuth {
    return Firebase.auth.apply { useEmulator(EMULATORS_ADDRESS, AUTH_PORT) }
  }

  @Provides
  @Singleton
  fun provideFirebaseFirestore(): FirebaseFirestore {
    return Firebase.firestore.apply { useEmulator(EMULATORS_ADDRESS, FIRESTORE_PORT) }
  }

  @Provides
  @Singleton
  fun provideFirebaseStorage(): FirebaseStorage {
    val storageBucket =
        FirebaseApp.getInstance().options.storageBucket
            ?: throw IllegalStateException(
                "Firebase storage bucket not set in google-services.json")
    return Firebase.storage("gs://$storageBucket").apply {
      useEmulator(EMULATORS_ADDRESS, STORAGE_PORT)
    }
  }

  @Provides
  @Singleton
  fun provideFirebaseFunctions(): FirebaseFunctions {
    return FirebaseFunctions.getInstance("europe-west9").apply {
      useEmulator(EMULATORS_ADDRESS, FUNCTIONS_PORT)
    }
  }

  @Provides
  @Singleton
  fun provideAuthenticationService(auth: FirebaseAuth): AuthenticationService {
    return FirebaseAuthenticationService(auth)
  }

  @Provides
  @Singleton
  fun provideActivityRepository(db: FirebaseFirestore): ActivityRepository {
    return ActivityRepositoryFirebase(db)
  }

  @Provides
  @Singleton
  fun provideDocumentRepository(
      db: FirebaseFirestore,
      storage: FirebaseStorage,
      auth: FirebaseAuth,
      functions: FirebaseFunctions
  ): DocumentRepository {
    return DocumentRepositoryFirestore(db, storage, auth, functions)
  }

  @Provides
  @Singleton
  fun provideEventRepository(db: FirebaseFirestore): EventRepository {
    return EventRepositoryFirebase(db)
  }

  @Provides
  @Singleton
  fun provideFileDownloader(
      @ApplicationContext context: Context,
      storage: FirebaseStorage,
      dataStore: DataStore<Preferences>
  ): DocumentsManager {
    return DocumentsManager(
      context.contentResolver,
      storage,
      dataStore,
      context.getDir(context.getString(R.string.thumbs_dir_name), Context.MODE_PRIVATE)
    )
  }

  @Provides
  @Singleton
  fun provideNotificationRepository(db: FirebaseFirestore): NotificationRepository {
    return NotificationRepositoryFirestore(db)
  }

  @Provides
  @Singleton
  fun provideProfileRepository(db: FirebaseFirestore): ProfileRepository {
    return ProfileRepositoryFirebase(db)
  }

  @Provides
  @Singleton
  fun provideTravelRepository(db: FirebaseFirestore): TravelRepository {
    return TravelRepositoryFirestore(db)
  }

  @Provides
  @Singleton
  fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
      produceFile = { context.preferencesDataStoreFile("documents") })
  }
}
