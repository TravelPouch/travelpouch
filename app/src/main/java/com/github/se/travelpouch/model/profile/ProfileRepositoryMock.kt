package com.github.se.travelpouch.model.profile

import android.util.Log
import com.github.se.travelpouch.di.profileCollection

class ProfileRepositoryMock : ProfileRepository {

  var profilePath: String = ""

  override fun getProfileElements(onSuccess: (Profile) -> Unit, onFailure: (Exception) -> Unit) {
    val profile: Profile? = profileCollection[profilePath]
    if (profile == null) {
      onFailure(Exception("error getting the profile"))
    } else {
      onSuccess(profile)
    }
  }

  override fun updateProfile(
      newProfile: Profile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    com.github.se.travelpouch.di.profileCollection[profilePath] = newProfile
    onSuccess()
  }

  override suspend fun initAfterLogin(onSuccess: (Profile) -> Unit) {
    Log.d("ENDTOEND-FINAL", "in the mock profile repo")

    profilePath = "qwertzuiopasdfghjklyxcvbnm12"
    val profileFetched: Profile? = com.github.se.travelpouch.di.profileCollection[profilePath]

    if (profileFetched != null) {
      onSuccess(profileFetched)
    } else {
      val profile =
          Profile(profilePath, "username", "emailtest1@gmail.com", null, "name", emptyList())
      com.github.se.travelpouch.di.profileCollection[profilePath] = profile
      onSuccess(profile)
    }
  }
}
