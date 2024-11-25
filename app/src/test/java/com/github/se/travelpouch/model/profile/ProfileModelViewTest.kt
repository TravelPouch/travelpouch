package com.github.se.travelpouch.model.profile

import android.content.Context
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
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
          "qwertzuiopasdfghjklyxcvbnm12",
          "username",
          "email@test.ch",
          emptyList(),
          "name",
          emptyList())

  val newProfile =
      Profile(
          "qwertzuiopasdfghjklyxcvbnm12",
          "username",
          "email@test.ch",
          listOf("test@test.ch"),
          "name",
          emptyList())

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

  @Test
  fun updatingFriendListTest() {
    val privateFunc =
        profileViewModel.javaClass.getDeclaredMethod(
            "updatingFriendList", Profile::class.java, String::class.java)
    privateFunc.isAccessible = true
    val parameters = arrayOfNulls<Any>(2)
    parameters[0] = profile
    parameters[1] = "test@test.ch"
    val result = privateFunc.invoke(profileViewModel, *parameters)
    assertThat(result, `is`(newProfile))
  }
}
