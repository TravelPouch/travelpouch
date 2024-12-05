package com.github.se.travelpouch.model.profile

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

  override fun getFsUidByEmail(
      email: String,
      onSuccess: (String?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
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

    profilePath = "qwertzuiopasdfghjklyxcvbnm12"
    val profileFetched: Profile? = profileCollection[profilePath]

    if (profileFetched != null) {
      onSuccess(profileFetched)
    } else {
      val profile =
          Profile(profilePath, "username", "emailtest1@gmail.com", emptyList(), "name", emptyList())
      profileCollection[profilePath] = profile
      onSuccess(profile)
    }
  }

  override fun addFriend(
      email: String,
      userProfile: Profile,
      onSuccess: (Profile) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

    override fun addNotificationTokenToProfile(
        token: String,
        user: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}
