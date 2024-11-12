package com.github.se.travelpouch.model.profile

import android.util.Log
import com.github.se.travelpouch.profileCollection

class ProfileRepositoryMock : ProfileRepository {

  var profilePath: String = ""

  override fun getProfileElements(onSuccess: (Profile) -> Unit, onFailure: (Exception) -> Unit) {
    onSuccess(profileCollection[profilePath]!!)
  }

  override fun updateProfile(
      newProfile: Profile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    profileCollection[profilePath] = newProfile
    onSuccess()
  }

  override suspend fun initAfterLogin(onSuccess: (Profile) -> Unit) {
    Log.d("ENDTOEND-FINAL", "in the mock profile repo")

    profilePath = "qwertzuiopasdfghjklyxcvbnm12"

    if (profileCollection.containsKey(profilePath)) {
      onSuccess(profileCollection[profilePath]!!)
    } else {
      val profile =
          Profile(profilePath, "username", "emailtest1@gmail.com", null, "name", emptyList())
      profileCollection[profilePath] = profile
      onSuccess(profile)
    }
  }
}
