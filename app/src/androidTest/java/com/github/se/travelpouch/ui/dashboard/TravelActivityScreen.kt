package com.github.se.travelpouch.ui.dashboard

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class TravelActivityScreenCopy {
  private lateinit var mockActivityRepositoryFirebase: ActivityRepository
  private lateinit var mockActivityModelView: ActivityViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var mockDocumentViewModel: DocumentViewModel
  private lateinit var mockDocumentRepository: DocumentRepository

  val activites_test =
      listOf(
          Activity(
              "1",
              "title1",
              "description1",
              Location(0.0, 0.0, Timestamp(0, 0), "lcoation1"),
              Timestamp(0, 0),
              emptyList()),
          Activity(
              "2",
              "title2",
              "description2",
              Location(0.0, 0.0, Timestamp(0, 0), "lcoation2"),
              Timestamp(0, 0),
              emptyList()))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mockActivityRepositoryFirebase = mock(ActivityRepository::class.java)
    mockActivityModelView = ActivityViewModel(mockActivityRepositoryFirebase)
    mockDocumentRepository = mock()
    mockDocumentViewModel = DocumentViewModel(mockDocumentRepository, mock(), mock())
  }

  @Test
  fun verifiesEverythingIsDisplayed() {
    composeTestRule.setContent {
      TravelActivitiesScreen(
          navigationActions,
          activityModelView = mockActivityModelView,
          documentViewModel = mockDocumentViewModel)
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
      TravelActivitiesScreen(
          navigationActions,
          activityModelView = mockActivityModelView,
          documentViewModel = mockDocumentViewModel)
    }

    `when`(mockActivityRepositoryFirebase.getAllActivities(any(), any())).then {
      it.getArgument<(List<Activity>) -> Unit>(0)(listOf())
    }

    composeTestRule.onNodeWithTag("emptyTravel").isDisplayed()
    composeTestRule.onNodeWithTag("emptyTravel1").assertTextEquals("No activities planned")
    composeTestRule.onNodeWithTag("emptyTravel2").assertTextEquals("for this trip")
  }

  @Test
  fun verifyActivityCardWorksCorrectly() {
    val activity = activites_test[0]

    composeTestRule.setContent {
      ActivityItem(activity, {}, LocalContext.current, mockDocumentViewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("activityItem").assertTextContains(activity.title)
    composeTestRule.onNodeWithTag("activityItem").assertTextContains(activity.location.name)
    composeTestRule.onNodeWithTag("activityItem").assertTextContains("1/1/1970")
    // composeTestRule.onNodeWithTag("extraDocumentButton").assertIsDisplayed().performClick()
  }

  //  @Test
  //  fun verify1ImageActivity() {
  //    val activity = activites_test[0]
  //    val images =
  //        listOf(
  //
  // "https://img.yumpu.com/30185842/1/500x640/afps-attestation-de-formation-aux-premiers-secours-programme-.jpg",
  //
  // "https://wallpapercrafter.com/desktop6/1606440-architecture-buildings-city-downtown-finance-financial.jpg",
  //
  // "https://assets.entrepreneur.com/content/3x2/2000/20151023204134-poker-game-gambling-gamble-cards-money-chips-game.jpeg")
  //    composeTestRule.setContent {
  //      ActivityItem(activity, {}, LocalContext.current, listOf(images[0]))
  //    }
  //    composeTestRule.waitForIdle()
  //  }
  //
  //  @Test
  //  fun verify2ImagesActivity() {
  //    val activity = activites_test[0]
  //    val images =
  //        listOf(
  //
  // "https://img.yumpu.com/30185842/1/500x640/afps-attestation-de-formation-aux-premiers-secours-programme-.jpg",
  //
  // "https://wallpapercrafter.com/desktop6/1606440-architecture-buildings-city-downtown-finance-financial.jpg",
  //
  // "https://assets.entrepreneur.com/content/3x2/2000/20151023204134-poker-game-gambling-gamble-cards-money-chips-game.jpeg")
  //    composeTestRule.setContent {
  //      ActivityItem(activity, {}, LocalContext.current, listOf(images[0], images[1]))
  //    }
  //    composeTestRule.waitForIdle()
  //  }

  @Test
  fun runDefaultErrorUIToCheckFailure() {
    composeTestRule.setContent { DefaultErrorUI() }
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
                emptyList()),
            Activity(
                "2",
                "title2",
                "description2",
                Location(0.0, 0.0, Timestamp(0, 0), "lcoation2"),
                Timestamp(nowSeconds + 3600L, 0),
                emptyList()))

    composeTestRule.setContent { NextActivitiesBanner(activitiesNow, {}) }
    composeTestRule.onNodeWithTag("NextActivitiesBannerBox").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("NextActivitiesBannerBox")
        .assertTextContains("Upcoming Activities in the next 24 hours")
    composeTestRule.onNodeWithTag("NextActivitiesBannerBox").assertTextContains("- title1")
    composeTestRule.onNodeWithTag("NextActivitiesBannerBox").assertTextContains("- title2")
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
                emptyList()),
            Activity(
                "2",
                "title2",
                "description2",
                Location(0.0, 0.0, Timestamp(0, 0), "location2"),
                Timestamp(nowSeconds + 100_000L, 0),
                emptyList()))

    composeTestRule.setContent { NextActivitiesBanner(activitiesOutOfDate, {}) }
    composeTestRule.onNodeWithTag("NextActivitiesBannerBox").assertDoesNotExist()
  }
}
