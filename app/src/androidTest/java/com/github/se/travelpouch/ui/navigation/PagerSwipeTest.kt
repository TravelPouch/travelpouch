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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.documents.DocumentsManager
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.TravelRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class PagerSwipeTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockActivityRepository: ActivityRepository
  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var mockTravelRepository: TravelRepository
  private lateinit var mockDocumentRepository: DocumentRepository
  private lateinit var mockDocumentsManager: DocumentsManager
  private lateinit var mockDataStore: DataStore<Preferences>

  private lateinit var activityViewModel: ActivityViewModel
  private lateinit var calendarViewModel: CalendarViewModel
  private lateinit var documentViewModel: DocumentViewModel
  private lateinit var listTravelViewModel: ListTravelViewModel

  @Before
  fun setUp() {
    mockActivityRepository = mock()
    mockNavigationActions = mock()
    mockTravelRepository = mock()
    mockDocumentRepository = mock()
    mockDocumentsManager = mock()
    mockDataStore = mock()

    activityViewModel = ActivityViewModel(mockActivityRepository)
    calendarViewModel = CalendarViewModel(activityViewModel)
    documentViewModel =
        DocumentViewModel(mockDocumentRepository, mockDocumentsManager, mockDataStore)
    listTravelViewModel = ListTravelViewModel(mockTravelRepository)
  }

  private fun assertTopBarIsDisplayedCorrectly(composeTestRule: ComposeTestRule, title: String) {
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelTitle").assertTextEquals(title)
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("eventTimelineButton").assertIsDisplayed()
  }

  private fun assertBottomBarIsDisplayedCorrectly(composeTestRule: ComposeTestRule) {
    composeTestRule.onNodeWithTag("navigationBarTravelList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Activities").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Activities").assertTextEquals("Activities")
    composeTestRule.onNodeWithTag("Calendar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Calendar").assertTextEquals("Calendar")
    composeTestRule.onNodeWithTag("Map").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Map").assertTextEquals("Map")
    composeTestRule.onNodeWithTag("Documents").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Documents").assertTextEquals("Documents")
  }

  @Test
  fun verifiesSwipeWorks() {
    composeTestRule.setContent {
      SwipePager(
          mockNavigationActions,
          activityViewModel,
          calendarViewModel,
          documentViewModel,
          listTravelViewModel,
          {})
    }

    composeTestRule.onNodeWithTag("pagerSwipe").assertIsDisplayed()

    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("documentListScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Activities")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned").assertIsDisplayed()
    composeTestRule.onNodeWithText("for this trip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("pagerSwipe").performTouchInput { swipeRight() }
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("documentListScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Activities")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned").assertIsDisplayed()
    composeTestRule.onNodeWithText("for this trip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("pagerSwipe").performTouchInput { swipeLeft() }

    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("documentListScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Calendar")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned").assertIsNotDisplayed()
    composeTestRule.onNodeWithText(" for this trip").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("pagerSwipe").performTouchInput { swipeLeft() }

    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("documentListScreen").assertIsDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Documents")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned").assertIsNotDisplayed()
    composeTestRule.onNodeWithText(" for this trip").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("pagerSwipe").performTouchInput { swipeLeft() }

    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("documentListScreen").assertIsDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Documents")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned").assertIsNotDisplayed()
    composeTestRule.onNodeWithText(" for this trip").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("pagerSwipe").performTouchInput { swipeRight() }

    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("documentListScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Calendar")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned").assertIsNotDisplayed()
    composeTestRule.onNodeWithText(" for this trip").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("pagerSwipe").performTouchInput { swipeRight() }
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("documentListScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Activities")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned").assertIsDisplayed()
    composeTestRule.onNodeWithText("for this trip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsDisplayed()
  }

  @Test
  fun verifiesBottomBarWorks() {
    composeTestRule.setContent {
      SwipePager(
          mockNavigationActions,
          activityViewModel,
          calendarViewModel,
          documentViewModel,
          listTravelViewModel,
          {})
    }

    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("documentListScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Activities")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned").assertIsDisplayed()
    composeTestRule.onNodeWithText("for this trip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsDisplayed()

    composeTestRule.onNodeWithText("Calendar").performClick()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("documentListScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Calendar")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)

    composeTestRule.onNodeWithText("Documents").performClick()
    composeTestRule.onNodeWithTag("documentListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Documents")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)

    composeTestRule.onNodeWithText("Activities").performClick()
    composeTestRule.onNodeWithTag("travelActivitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calenmdarScreen").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("documentListScreen").assertIsNotDisplayed()
    assertTopBarIsDisplayedCorrectly(composeTestRule, "Activities")
    assertBottomBarIsDisplayedCorrectly(composeTestRule)
    composeTestRule.onNodeWithText("No activities planned").assertIsDisplayed()
    composeTestRule.onNodeWithText("for this trip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addActivityButton").assertIsDisplayed()
  }
}
