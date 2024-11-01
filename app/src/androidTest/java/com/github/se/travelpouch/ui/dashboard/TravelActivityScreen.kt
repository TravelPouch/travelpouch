package com.github.se.travelpouch.ui.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class TravelActivityScreen {
  private lateinit var mockActivityRepositoryFirebase: ActivityRepository
  private lateinit var mockActivityModelView: ActivityViewModel
  private lateinit var navigationActions: NavigationActions

  val activites_test =
      listOf(
          Activity(
              "1",
              "title1",
              "description1",
              Location(0.0, 0.0, Timestamp(0, 0), "lcoation1"),
              Timestamp(0, 0),
              mapOf<String, Int>()),
          Activity(
              "2",
              "title2",
              "description2",
              Location(0.0, 0.0, Timestamp(0, 0), "lcoation2"),
              Timestamp(0, 0),
              mapOf<String, Int>()))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mockActivityRepositoryFirebase = mock(ActivityRepository::class.java)
    mockActivityModelView = ActivityViewModel(mockActivityRepositoryFirebase)
  }

  @Test
  fun verifiesEverythingIsDisplayed() {
    composeTestRule.setContent {
      TravelActivitiesScreen(navigationActions, activityModelView = mockActivityModelView)
    }

    `when`(mockActivityRepositoryFirebase.getAllActivities(any(), any())).then {
      it.getArgument<(List<Activity>) -> Unit>(0)(activites_test)
    }

    composeTestRule.onNodeWithTag("travelActivitiesScreen").isDisplayed()
    composeTestRule.onNodeWithTag("travelTitle").isDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").isDisplayed()
    composeTestRule.onNodeWithTag("settingsButton").isDisplayed()
    composeTestRule.onNodeWithTag("eventTimelineButton").isDisplayed()
    composeTestRule.onNodeWithTag("navigationBarTravel").isDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").isDisplayed()
  }

  @Test
  fun verifiesEmptyPromptWhenEmptyList() {
    composeTestRule.setContent {
      TravelActivitiesScreen(navigationActions, activityModelView = mockActivityModelView)
    }

    `when`(mockActivityRepositoryFirebase.getAllActivities(any(), any())).then {
      it.getArgument<(List<Activity>) -> Unit>(0)(listOf())
    }

    composeTestRule.onNodeWithTag("emptyTravel").isDisplayed()
    composeTestRule
        .onNodeWithTag("emptyTravel")
        .assertTextEquals("No activities planned for this trip")
  }

  @Test
  fun verifyActivityCardWorksCorrectly() {
    val activity = activites_test[0]

    composeTestRule.setContent { ActivityItem(activity) }

    composeTestRule.onNodeWithTag("activityItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("activityItem").assertTextContains(activity.title)
    composeTestRule.onNodeWithTag("activityItem").assertTextContains(activity.location.name)
    composeTestRule.onNodeWithTag("activityItem").assertTextContains("1/1/1970")
  }

  @Test
  fun verifyBannerIsDisplayedCorrectly() {
    val nowSeconds = Timestamp.now().seconds
    val activitiesNow =
        listOf(
            Activity(
                "1",
                "title1",
                "description1",
                Location(0.0, 0.0, Timestamp(0, 0), "lcoation1"),
                Timestamp(nowSeconds + 3600L, 0),
                mapOf<String, Int>()),
            Activity(
                "2",
                "title2",
                "description2",
                Location(0.0, 0.0, Timestamp(0, 0), "lcoation2"),
                Timestamp(nowSeconds + 3600L, 0),
                mapOf<String, Int>()))

    composeTestRule.setContent { NextActivitiesBanner(activitiesNow, {}) }
    composeTestRule.onNodeWithTag("NextActivitiesBannerBox").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("NextActivitiesBannerBox")
        .assertTextContains("Next activities due: title1, title2 in the next 24 hours.")
    composeTestRule.onNodeWithTag("NextActivitiesBannerDismissButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("NextActivitiesBannerBox").performClick()
    composeTestRule.onNodeWithTag("NextActivitiesBannerDismissButton").performClick()
  }

  @Test
  fun verifyBannerIsNotDisplayedIfNoActivities() {
    composeTestRule.setContent { NextActivitiesBanner(emptyList(), {}) }
    composeTestRule.onNodeWithTag("NextActivitiesBannerBox").assertDoesNotExist()
  }

  @Test
  fun verifyBannerIsNotDisplayedIfNoDueActivities() {
    val nowSeconds = Timestamp.now().seconds
    val activitiesOutOfDate =
        listOf(
            Activity(
                "1",
                "title1",
                "description1",
                Location(0.0, 0.0, Timestamp(0, 0), "location1"),
                Timestamp(nowSeconds - 100_000L, 0),
                mapOf<String, Int>()),
            Activity(
                "2",
                "title2",
                "description2",
                Location(0.0, 0.0, Timestamp(0, 0), "location2"),
                Timestamp(nowSeconds + 100_000L, 0),
                mapOf<String, Int>()))

    composeTestRule.setContent { NextActivitiesBanner(activitiesOutOfDate, {}) }
    composeTestRule.onNodeWithTag("NextActivitiesBannerBox").assertDoesNotExist()
  }
}
