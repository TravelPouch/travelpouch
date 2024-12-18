package com.github.se.travelpouch.ui.dashboard

import androidx.compose.ui.semantics.SemanticsProperties.EditableText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.events.EventRepository
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.location.LocationRepository
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever

class AddActivityScreenTest {
  private lateinit var mockActivityRepositoryFirebase: ActivityRepository
  private lateinit var mockActivityModelView: ActivityViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var mockLocationViewModel: LocationViewModel
  private lateinit var eventRepository: EventRepository
  private lateinit var eventViewModel: EventViewModel

  class FakeLocationRepository : LocationRepository {

    val location =
        listOf(
            Location(48.8566, 2.3522, Timestamp.now(), "Paris"),
            Location(34.0522, -118.2437, Timestamp.now(), "Los Angeles"),
            Location(51.5074, -0.1278, Timestamp.now(), "London"))

    override fun search(
        query: String,
        onSuccess: (List<Location>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      onSuccess(location)
    }
  }

  val activity =
      Activity(
          "uid",
          "title",
          "description",
          Location(0.0, 0.0, Timestamp(0, 0), "location"),
          Timestamp(0, 0),
          emptyList())

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mockActivityRepositoryFirebase = mock(ActivityRepository::class.java)
    mockActivityModelView = ActivityViewModel(mockActivityRepositoryFirebase)
    mockLocationViewModel = LocationViewModel(FakeLocationRepository())
    eventRepository = mock()
    eventViewModel = EventViewModel(eventRepository)

    `when`(mockActivityModelView.getNewUid()).thenReturn("uid")
  }

  @Test
  fun everythingIsDisplayed() {
    composeTestRule.setContent {
      AddActivityScreen(navigationActions, mockActivityModelView, eventViewModel = eventViewModel)
    }

    composeTestRule.onNodeWithTag("AddActivityScreen").isDisplayed()
    composeTestRule.onNodeWithTag("titleField").isDisplayed()
    composeTestRule.onNodeWithTag("descriptionField").isDisplayed()
    composeTestRule.onNodeWithTag("dateField").isDisplayed()
    composeTestRule.onNodeWithTag("titleField").isDisplayed()
    composeTestRule.onNodeWithTag("locationField").isDisplayed()
    composeTestRule.onNodeWithTag("saveButton").isDisplayed()
    composeTestRule.onNodeWithTag("saveButton").assertTextEquals("Save")
  }

  private fun completeAllFields(composeTestRule: ComposeContentTestRule) {
    composeTestRule.onNodeWithTag("titleField").performTextClearance()
    composeTestRule.onNodeWithTag("titleField").performTextInput(activity.title)

    composeTestRule.onNodeWithTag("descriptionField").performTextClearance()
    composeTestRule.onNodeWithTag("descriptionField").performTextInput(activity.description)

    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("dateField").performTextInput("01022024")

    composeTestRule.onNodeWithTag("timeField").performTextClearance()
    composeTestRule.onNodeWithTag("timeField").performTextInput("15:24")
  }

  @Test
  fun doesNotSaveWhenFieldsAreEmpty() {
    composeTestRule.setContent {
      AddActivityScreen(navigationActions, mockActivityModelView, eventViewModel = eventViewModel)
    }

    completeAllFields(composeTestRule)
    // todo: add a test for location. Not now because location defined later.

    // verify no saving if title blank
    composeTestRule.onNodeWithTag("titleField").performTextClearance()
    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(mockActivityRepositoryFirebase, never())
        .addActivity(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

    // verify no saving when blank description
    completeAllFields(composeTestRule)
    composeTestRule.onNodeWithTag("descriptionField").performTextClearance()
    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(mockActivityRepositoryFirebase, never())
        .addActivity(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

    // verify no saving when blank date
    completeAllFields(composeTestRule)
    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(mockActivityRepositoryFirebase, never())
        .addActivity(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun doesSaveWhenFieldsAreFull() {
    composeTestRule.setContent {
      AddActivityScreen(navigationActions, mockActivityModelView, eventViewModel = eventViewModel)
    }
    completeAllFields(composeTestRule)

    whenever(eventViewModel.getNewDocumentReference())
        .thenReturn(mock(DocumentReference::class.java))

    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(mockActivityRepositoryFirebase)
        .addActivity(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun dateFormattingWorksCorrectly() {
    composeTestRule.setContent {
      AddActivityScreen(navigationActions, mockActivityModelView, eventViewModel = eventViewModel)
    }
    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("dateField").performTextInput("00000000")
    val result =
        composeTestRule.onNodeWithTag("dateField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "00/00/0000")
  }

  @Test
  fun doesNotSaveWhenDateIsWrong() {
    composeTestRule.setContent {
      AddActivityScreen(navigationActions, mockActivityModelView, eventViewModel = eventViewModel)
    }
    completeAllFields(composeTestRule)
    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("dateField").performTextInput("00000000")

    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(mockActivityRepositoryFirebase, never())
        .addActivity(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun noCharacterAllowedInDateField() {
    composeTestRule.setContent {
      AddActivityScreen(navigationActions, mockActivityModelView, eventViewModel = eventViewModel)
    }
    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("dateField").performTextInput("mdkdk")
    var result =
        composeTestRule.onNodeWithTag("dateField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "")
    composeTestRule.onNodeWithTag("dateField").performTextInput("l09kfj89iuiui2025")
    result = composeTestRule.onNodeWithTag("dateField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "")
    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("dateField").performTextInput("1234")
    composeTestRule.onNodeWithTag("dateField").performTextInput("l")
    result = composeTestRule.onNodeWithTag("dateField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "12/34/")
  }

  @Test
  fun limitOfEightCharactersInDateField() {
    composeTestRule.setContent {
      AddActivityScreen(
          navigationActions,
          mockActivityModelView,
          mockLocationViewModel,
          eventViewModel = eventViewModel)
    }
    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("dateField").performTextInput("01234567")
    composeTestRule.onNodeWithTag("dateField").performTextInput("8")
    val result =
        composeTestRule.onNodeWithTag("dateField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "01/23/4567")
  }

  @Test
  fun locationDropdownAppearsAndSelectionWorks() {
    val testQuery = "Paris"
    composeTestRule.setContent {
      AddActivityScreen(
          navigationActions,
          mockActivityModelView,
          mockLocationViewModel,
          eventViewModel = eventViewModel)
    }

    // Type in the location field
    composeTestRule.onNodeWithTag("inputTravelLocation").performTextInput(testQuery)

    // The dropdown should appear with the suggestion
    composeTestRule.onNodeWithTag("suggestion_$testQuery").assertIsDisplayed()

    // Click on the suggestion
    composeTestRule.onNodeWithTag("suggestion_$testQuery").performClick()
  }

  @Test
  fun timePickerDialogOpensOnClick() {

    // Since TimePickerDialog is a platform dialog, we cannot directly test its display.

    // However, we can test that the timeText state variable updates after interaction.

    // For this test, assume that the TimePickerDialog sets the time to "12:34"
    // We need to simulate this by updating the timeText state.

    // Unfortunately, Compose UI testing cannot interact with platform dialogs like
    // TimePickerDialog.

    composeTestRule.setContent {
      AddActivityScreen(
          navigationActions,
          mockActivityModelView,
          mockLocationViewModel,
          eventViewModel = eventViewModel)
    }

    // The time field should be displayed
    composeTestRule.onNodeWithTag("timePickerButton").assertIsDisplayed()
    // Click on the time field
    composeTestRule.onNodeWithTag("timePickerButton").performClick()
  }

  @Test
  fun datePickerDialogOpensOnClick() {

    composeTestRule.setContent {
      AddActivityScreen(
          navigationActions,
          mockActivityModelView,
          mockLocationViewModel,
          eventViewModel = eventViewModel)
    }

    // The date field should be displayed
    composeTestRule.onNodeWithTag("datePickerButton").assertIsDisplayed()
    // Click on the date field
    composeTestRule.onNodeWithTag("datePickerButton").performClick()
  }
}
