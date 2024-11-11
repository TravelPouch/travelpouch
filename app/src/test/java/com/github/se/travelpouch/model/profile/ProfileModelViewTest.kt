package com.github.se.travelpouch.model.profile

import android.content.Context
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull

class ProfileModelViewTest {
  private lateinit var repository: ProfileRepository
  private lateinit var profileViewModel: ProfileModelView
  private lateinit var context: Context

  val profile =
      Profile(
          "qwertzuiopasdfghjklyxcvbnm12", "username", "email@test.ch", null, "name", emptyList())

  @Before
  fun setUp() {
    repository = mock(ProfileRepository::class.java)
    profileViewModel = ProfileModelView(repository)
    context = mock(Context::class.java)
  }

  @Test
  fun getProfileTest() {
    profileViewModel.getProfile()
    verify(repository).getProfileElements(anyOrNull(), anyOrNull())
  }

  @Test
  fun updateProfileTest() {
    profileViewModel.updateProfile(profile, context)
    verify(repository).updateProfile(anyOrNull(), anyOrNull(), anyOrNull())
  }
}
