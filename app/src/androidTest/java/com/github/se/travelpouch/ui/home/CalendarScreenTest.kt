package com.github.se.travelpouch.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
import com.github.se.travelpouch.ui.dashboard.CalendarScreen
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.util.Calendar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class CalendarScreenTest {

  private lateinit var calendarViewModel: CalendarViewModel
  private lateinit var mockActivityRepositoryFirebase: ActivityRepository
  private lateinit var mockActivityViewModel: ActivityViewModel

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mockActivityRepositoryFirebase = mock(ActivityRepository::class.java)
    mockActivityViewModel = ActivityViewModel(mockActivityRepositoryFirebase)
    calendarViewModel = CalendarViewModel(activityViewModel = mockActivityViewModel)
  }

  @Test
  fun hasRequiredComponents() {
    // Act
    composeTestRule.setContent {
      CalendarScreen(calendarViewModel = calendarViewModel, navigationActions)
    }
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule.onNodeWithTag("calendarTopAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calendarScreenColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("androidCalendarView").assertIsDisplayed()
    composeTestRule.onNodeWithTag("activityList").assertIsDisplayed()
  }

  @Test
  fun testMockActivityAndClickOnAnotherDay() {
    // Arrange
    val today = Calendar.getInstance().time

    // Mock an activity for today and another day
    val activityToday =
        Activity(
            uid = "uid",
            title = "Mock Activity Today",
            date = Timestamp(today),
            description = "This is a mock activity for today.",
            location = Location(0.0, 0.0, Timestamp(0, 0), "location"),
            documentsNeeded = mapOf())

    val listOfActivities = listOf(activityToday)

    // Mock getAllActivities to return the list of activities
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Activity>) -> Unit>(0)
          onSuccess(listOfActivities)
          null
        }
        .whenever(mockActivityRepositoryFirebase)
        .getAllActivities(anyOrNull(), anyOrNull())

    // Call getAllActivities to populate activities in ViewModel
    mockActivityViewModel.getAllActivities()

    // Act
    composeTestRule.setContent {
      CalendarScreen(calendarViewModel = calendarViewModel, navigationActions = navigationActions)
    }
    composeTestRule.waitForIdle()

    // Assert activity for today is displayed
    composeTestRule.onNodeWithTag("activityCard").assertIsDisplayed()

    // Click on another day in the calendar
    composeTestRule.onNodeWithTag("androidCalendarView").performClick()

    // Click on the back icon to test navigation
    composeTestRule.onNodeWithTag("goBackIcon").performClick()
  }

  @Test
  fun testNavigationGoBack() {
    // Act
    composeTestRule.setContent {
      CalendarScreen(calendarViewModel = calendarViewModel, navigationActions)
    }
    composeTestRule.waitForIdle()

    // Click on the back icon to test navigation
    composeTestRule.onNodeWithTag("goBackIcon").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun testNavigationBottomBar() {
    // Act
    composeTestRule.setContent {
      CalendarScreen(calendarViewModel = calendarViewModel, navigationActions)
    }
    composeTestRule.waitForIdle()

    // Click on the Map icon to test navigation
    composeTestRule.onNodeWithTag("Travels").performClick()
    composeTestRule.waitForIdle()
  }
}
