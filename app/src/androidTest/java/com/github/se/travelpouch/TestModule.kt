package com.github.se.travelpouch

import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.profile.ProfileRepositoryMock
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.model.travels.TravelRepositoryMock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TestModule {

  @Provides
  @Singleton
  fun provideFirebaseAuth(): AuthenticationService {
    return MockFirebaseAuthenticationService()
  }

  @Provides
  @Singleton
  fun providesProfileRepository(): ProfileRepository {
    return ProfileRepositoryMock()
  }

  @Provides
  @Singleton
  fun providesTravelRepository(): TravelRepository {
    return TravelRepositoryMock()
  }
}
