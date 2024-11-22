package com.github.se.travelpouch.di

import android.content.Context
import com.github.se.travelpouch.helper.FileDownloader
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityRepositoryFirebase
import com.github.se.travelpouch.model.authentication.AuthenticationService
import com.github.se.travelpouch.model.authentication.FirebaseAuthenticationService
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentRepositoryFirestore
import com.github.se.travelpouch.model.events.EventRepository
import com.github.se.travelpouch.model.events.EventRepositoryFirebase
import com.github.se.travelpouch.model.notifications.NotificationRepository
import com.github.se.travelpouch.model.notifications.NotificationRepositoryFirestore
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.profile.ProfileRepositoryFirebase
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.model.travels.TravelRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

  @Provides
  @Singleton
  fun provideFirebaseAuth(): AuthenticationService {
    return FirebaseAuthenticationService(Firebase.auth)
  }

  @Provides
  @Singleton
  fun provideFirebaseFirestore(): FirebaseFirestore {
    return Firebase.firestore
  }

  @Provides
  @Singleton
  fun providesActivityRepository(db: FirebaseFirestore): ActivityRepository {
    return ActivityRepositoryFirebase(db)
  }

  @Provides
  @Singleton
  fun providesDocumentRepository(db: FirebaseFirestore): DocumentRepository {
    return DocumentRepositoryFirestore(db)
  }

  @Provides
  @Singleton
  fun providesEventRepository(db: FirebaseFirestore): EventRepository {
    return EventRepositoryFirebase(db)
  }

  @Provides
  @Singleton
  fun providesFileDownloader(@ApplicationContext context: Context): FileDownloader {
    return FileDownloader(context.contentResolver)
  }

  @Provides
  @Singleton
  fun providesNotificationRepository(db: FirebaseFirestore): NotificationRepository {
    return NotificationRepositoryFirestore(db)
  }

  @Provides
  @Singleton
  fun providesProfileRepository(db: FirebaseFirestore): ProfileRepository {
    return ProfileRepositoryFirebase(db)
  }

  @Provides
  @Singleton
  fun providesTravelRepository(db: FirebaseFirestore): TravelRepository {
    return TravelRepositoryFirestore(db)
  }
}
