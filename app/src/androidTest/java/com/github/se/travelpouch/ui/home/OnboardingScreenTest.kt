package com.github.se.travelpouch.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class OnboardingScreenTest {

  private lateinit var profileModelView: ProfileModelView
  private lateinit var navigationActions: NavigationActions
  private lateinit var profileRepository: ProfileRepository

  private val profile =
      Profile(
          fsUid = "qwertzuiopasdfghjklyxcvbnm12",
          email = "test@swent.ch",
          username = "test",
          friends = mapOf("email@email.com" to "uid1"),
          name = "name",
          needsOnboarding = true,
          userTravelList = emptyList())

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    profileRepository = mock(ProfileRepository::class.java)
    profileModelView = ProfileModelView(profileRepository)
  }

  @Test
  fun verifiesOnboardingScreenDisplaysCorrectly() {
    // Arrange
    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }
    profileModelView.getProfile()

    // Act
    composeTestRule.setContent {
      OnboardingScreen(navigationActions = navigationActions, profileModelView = profileModelView)
    }

    // Assert
    composeTestRule.onNodeWithTag("OnboardingScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("OnboardingPageContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("NavigationButtons").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SkipButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("NextButton").assertIsDisplayed()
  }

  @Test
  fun verifiesSkipButtonUpdatesProfileAndNavigates() {
    // Arrange
    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }
    profileModelView.getProfile()

    // Act
    composeTestRule.setContent {
      OnboardingScreen(navigationActions = navigationActions, profileModelView = profileModelView)
    }
    composeTestRule.onNodeWithTag("SkipButton").performClick()

    // Assert
    verify(profileRepository).updateProfile(anyOrNull(), anyOrNull(), anyOrNull())
    // You can also verify navigation
    verify(navigationActions).navigateTo(anyOrNull())
  }

  @Test
  fun verifiesNextButtonMovesToNextPage() {
    // Arrange
    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }
    profileModelView.getProfile()

    // Act
    composeTestRule.setContent {
      OnboardingScreen(navigationActions = navigationActions, profileModelView = profileModelView)
    }
    composeTestRule.onNodeWithTag("NextButton").performClick()

    // Assert
    composeTestRule.onNodeWithTag("OnboardingPageContent").assertIsDisplayed() // Verify new content
  }

  @Test
  fun verifiesGetStartedButtonCompletesOnboardingAndNavigates() {
    // Arrange
    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }
    profileModelView.getProfile()

    // Act
    composeTestRule.setContent {
      OnboardingScreen(navigationActions = navigationActions, profileModelView = profileModelView)
    }
    // Press the next button thrice to get through the onboarding
    composeTestRule.onNodeWithTag("NextButton").performClick()
    composeTestRule.onNodeWithTag("NextButton").performClick()
    composeTestRule.onNodeWithTag("NextButton").performClick()

    composeTestRule.onNodeWithTag("GetStartedButton").performClick()

    // Assert
    verify(profileRepository).updateProfile(anyOrNull(), anyOrNull(), anyOrNull())
    verify(navigationActions).navigateTo(anyOrNull())
  }
}
