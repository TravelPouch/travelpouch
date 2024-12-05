package com.github.se.travelpouch.model.profile

import com.github.se.travelpouch.di.profileCollection
import junit.framework.TestCase.assertFalse
import org.junit.Before
import org.junit.Test

class ProfileRepositoryMockTest {

  val profile =
      Profile(
          "qwertzuiopasdfghjklyxcvbnm12",
          "username",
          "emailtest1@gmail.com",
          emptyMap(),
          "name",
          emptyList())

  val newProfile =
      Profile(
          "qwertzuiopasdfghjklyxcvbnm12",
          "username - modified",
          "emailtest1@gmail.com",
          emptyMap(),
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

    val noProfile = com.github.se.travelpouch.di.profileCollection[profile.fsUid]
    assert(noProfile == null)
    profileMockRepository.getProfileElements({ succeeded = true }, { failed = true })
    assertFalse(succeeded)
    assert(failed)

    succeeded = false
    failed = false

    com.github.se.travelpouch.di.profileCollection[profile.fsUid] = profile
    profileMockRepository.getProfileElements({ succeeded = true }, { failed = true })

    assert(succeeded)
    assertFalse(failed)

    com.github.se.travelpouch.di.profileCollection.clear()
  }

  @Test
  fun verifiesThatUpdatingWorks() {
    var succeeded = false
    var failed = false

    com.github.se.travelpouch.di.profileCollection[profile.fsUid] = profile
    profileMockRepository.updateProfile(newProfile, { succeeded = true }, { failed = true })

    assert(succeeded)
    assertFalse(failed)
    assert(com.github.se.travelpouch.di.profileCollection[profile.fsUid] == newProfile)

    com.github.se.travelpouch.di.profileCollection.clear()
  }
}
