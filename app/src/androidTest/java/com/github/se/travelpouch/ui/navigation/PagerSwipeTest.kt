package com.github.se.travelpouch.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class PagerSwipeTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockActivityRepository: ActivityRepository
  private lateinit var mockNavigationActions: NavigationActions

  private lateinit var activityViewModel: ActivityViewModel
  private lateinit var calendarViewModel: CalendarViewModel

  @Before
  fun setUp() {
    mockActivityRepository = mock()
    mockNavigationActions = mock()

    activityViewModel = ActivityViewModel(mockActivityRepository)
    calendarViewModel = CalendarViewModel(activityViewModel)
  }

  private fun assertTopBarIsDisplayedCorrectly(composeTestRule: ComposeTestRule, title: String) {
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelTitle").assertTextEquals(title)
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("eventTimelineButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("documentListButton").assertIsDisplayed()
  }

  private fun assertBottomBarIsDisplayedCorrectly(composeTestRule: ComposeTestRule) {
    composeTestRule.onNodeWithTag("navigationBarTravelList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Activities").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Activities").assertTextEquals("Activities")
    composeTestRule.onNodeWithTag("Calendar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Calendar").assertTextEquals("Calendar")
    composeTestRule.onNodeWithTag("Map").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Map").assertTextEquals("Map")
  }

  @Test
  fun verifiesSwipeWorks() {
    composeTestRule.setContent {
      SwipePager(mockNavigationActions, activityViewModel, calendarViewModel)
    }

    composeTestRule.onNodeWithTag("pagerSwipe").assertIsDisplayed()

    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Activities")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned for this trip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("pagerSwipe").performTouchInput { swipeRight() }
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Activities")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned for this trip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("pagerSwipe").performTouchInput { swipeLeft() }

    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Calendar")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned for this trip").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("pagerSwipe").performTouchInput { swipeLeft() }

    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Calendar")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned for this trip").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("pagerSwipe").performTouchInput { swipeRight() }
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Activities")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned for this trip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsDisplayed()
  }

  @Test
  fun verifiesBottomBarWorks() {
    composeTestRule.setContent {
      SwipePager(mockNavigationActions, activityViewModel, calendarViewModel)
    }

    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Activities")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned for this trip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsDisplayed()

    composeTestRule.onNodeWithText("Calendar").performClick()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Calendar")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)

    composeTestRule.onNodeWithText("Activities").performClick()
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Activities")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned for this trip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsDisplayed()
  }
}
