package com.github.se.travelpouch.model.profile

class ProfileRepositoryMock : ProfileRepository {

  var profilePath: String = ""

  override fun getProfileElements(onSuccess: (Profile) -> Unit, onFailure: (Exception) -> Unit) {}

  override fun updateProfile(
      newProfile: Profile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override suspend fun initAfterLogin(onSuccess: (Profile) -> Unit) {
    TODO("Not yet implemented")
  }
}
