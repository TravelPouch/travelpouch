package com.github.se.travelpouch.di

import androidx.viewbinding.BuildConfig
import com.github.se.travelpouch.model.authentication.AuthenticationService
import com.github.se.travelpouch.model.authentication.FirebaseAuthenticationService
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
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

  @Provides
  @Singleton
  fun provideFirebaseAuth(): AuthenticationService {
    val auth = Firebase.auth
    if (BuildConfig.DEBUG)
      auth.useEmulator("10.0.2.2", 9099)
    return FirebaseAuthenticationService(auth)
  }

  @Provides
  @Singleton
  fun provideFirebaseFirestore(): FirebaseFirestore {
    val firestore = Firebase.firestore
    if (BuildConfig.DEBUG)
      firestore.useEmulator("10.0.2.2", 8080)
    return firestore
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
