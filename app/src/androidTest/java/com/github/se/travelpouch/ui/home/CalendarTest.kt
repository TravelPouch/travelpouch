package com.github.se.travelpouch.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
import com.github.se.travelpouch.ui.dashboard.CalendarScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class CalendarScreenTest {

  private lateinit var calendarViewModel: CalendarViewModel
  private lateinit var mockActivityRepositoryFirebase: ActivityRepository
  private lateinit var mockActivityViewModel: ActivityViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    mockActivityRepositoryFirebase = mock(ActivityRepository::class.java)
    mockActivityViewModel = ActivityViewModel(mockActivityRepositoryFirebase)

    `when`(mockActivityViewModel.getNewUid()).thenReturn("uid")
    calendarViewModel = CalendarViewModel(activityViewModel = mockActivityViewModel)
  }

  @Test
  fun hasRequiredComponents() {
    // Act
    composeTestRule.setContent { CalendarScreen(calendarViewModel = calendarViewModel) }
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule.onNodeWithTag("calendarTopAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calendarScreenColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("androidCalendarView").assertIsDisplayed()
    composeTestRule.onNodeWithTag("activityList").assertIsDisplayed()
  }
}
