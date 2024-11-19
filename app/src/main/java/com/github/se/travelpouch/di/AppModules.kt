package com.github.se.travelpouch.di

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
    return FirebaseAuthenticationService(Firebase.auth)
  }

  @Provides
  @Singleton
  fun provideFirebaseFirestore(): FirebaseFirestore {
    return Firebase.firestore
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
