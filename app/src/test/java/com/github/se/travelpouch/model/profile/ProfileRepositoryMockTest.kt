package com.github.se.travelpouch.model.profile

import com.github.se.travelpouch.profileCollection
import org.junit.Before
import org.junit.Test

class ProfileRepositoryMockTest {

  val profile =
      Profile(
          "qwertzuiopasdfghjklyxcvbnm12",
          "username",
          "emailtest1@gmail.com",
          null,
          "name",
          emptyList())

  val newProfile =
      Profile(
          "qwertzuiopasdfghjklyxcvbnm12",
          "username - modified",
          "emailtest1@gmail.com",
          null,
          "name",
          emptyList())

  val profileMockRepository = ProfileRepositoryMock()

  @Before
  fun setup() {
    profileMockRepository.profilePath = profile.fsUid
  }

  @Test
  fun verifiesThatGettingWorks() {
    var succeeded = false
    var failed = false

    val noProfile = profileCollection[profile.fsUid]
    assert(noProfile == null)
    profileMockRepository.getProfileElements({ succeeded = true }, { failed = true })
    assert(!succeeded)
    assert(failed)

    succeeded = false
    failed = false

    profileCollection[profile.fsUid] = profile
    profileMockRepository.getProfileElements({ succeeded = true }, { failed = true })

    assert(succeeded)
    assert(!failed)

    profileCollection.clear()
  }

  @Test
  fun verifiesThatUpdatingWorks() {
    var succeeded = false
    var failed = false

    profileCollection[profile.fsUid] = profile
    profileMockRepository.updateProfile(newProfile, { succeeded = true }, { failed = true })

    assert(succeeded)
    assert(!failed)
    assert(profileCollection[profile.fsUid] == newProfile)

    profileCollection.clear()
  }
}
