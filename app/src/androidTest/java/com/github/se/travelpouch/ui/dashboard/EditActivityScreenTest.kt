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
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never

class EditActivityScreenTest {
  private lateinit var mockActivityRepositoryFirebase: ActivityRepository
  private lateinit var mockActivityModelView: ActivityViewModel
  private lateinit var navigationActions: NavigationActions

  val activity =
      Activity(
          "uid",
          "title",
          "description",
          Location(0.0, 0.0, Timestamp(0, 0), "location"),
          Timestamp(0, 0),
          mapOf())

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mockActivityRepositoryFirebase = mock(ActivityRepository::class.java)
    mockActivityModelView = ActivityViewModel(mockActivityRepositoryFirebase)

    `when`(mockActivityModelView.getNewUid()).thenReturn("uid")
    mockActivityModelView.selectActivity(activity)
  }

  @Test
  fun everythingIsDisplayed() {
    composeTestRule.setContent { EditActivity(navigationActions, mockActivityModelView) }

    composeTestRule.onNodeWithTag("EditActivityScreen").isDisplayed()
    composeTestRule.onNodeWithTag("titleField").isDisplayed()
    composeTestRule.onNodeWithTag("descriptionField").isDisplayed()
    composeTestRule.onNodeWithTag("dateField").isDisplayed()
    composeTestRule.onNodeWithTag("locationField").isDisplayed()
    composeTestRule.onNodeWithTag("saveButton").isDisplayed()
    composeTestRule.onNodeWithTag("saveButton").assertTextEquals("Save")
    composeTestRule.onNodeWithTag("deleteButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteButton").assertTextEquals("Delete")
  }

  private fun VerifyFieldsAreCorrect(composeTestRule: ComposeContentTestRule) {

    var result =
        composeTestRule.onNodeWithTag("titleField").fetchSemanticsNode().config[EditableText]
    assert(result.text == activity.title)
    result =
        composeTestRule.onNodeWithTag("descriptionField").fetchSemanticsNode().config[EditableText]
    assert(result.text == activity.description)
    result =
        composeTestRule.onNodeWithTag("locationField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "location")
    result = composeTestRule.onNodeWithTag("dateField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "01/01/1970")
  }

  @Test
  fun deletesCallsDeleteById() {
    composeTestRule.setContent { EditActivity(navigationActions, mockActivityModelView) }
    composeTestRule.onNodeWithTag("deleteButton").performClick()
    verify(mockActivityRepositoryFirebase).deleteActivityById(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun verifiesDisplayWorksAndModificationPlusSaving() {
    composeTestRule.setContent { EditActivity(navigationActions, mockActivityModelView) }

    VerifyFieldsAreCorrect(composeTestRule)

    composeTestRule.onNodeWithTag("titleField").performTextClearance()
    composeTestRule.onNodeWithTag("titleField").performTextInput("newTitle")
    composeTestRule.onNodeWithTag("descriptionField").performTextClearance()
    composeTestRule.onNodeWithTag("descriptionField").performTextInput("newDescription")
    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("dateField").performTextInput("23062025")

    var result =
        composeTestRule.onNodeWithTag("titleField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "newTitle")
    result =
        composeTestRule.onNodeWithTag("descriptionField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "newDescription")
    result =
        composeTestRule.onNodeWithTag("locationField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "location")
    result = composeTestRule.onNodeWithTag("dateField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "23/06/2025")

    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(mockActivityRepositoryFirebase).updateActivity(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun dateFormattingWorksCorrectly() {
    composeTestRule.setContent { EditActivity(navigationActions, mockActivityModelView) }
    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("dateField").performTextInput("00000000")
    val result =
        composeTestRule.onNodeWithTag("dateField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "00/00/0000")
  }

  @Test
  fun savesWhenFieldsAreCorrect() {
    composeTestRule.setContent { EditActivity(navigationActions, mockActivityModelView) }
    VerifyFieldsAreCorrect(composeTestRule)
    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("dateField").performTextInput("23122024")

    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(mockActivityRepositoryFirebase).updateActivity(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun doesNotSaveWhenDateIsWrong() {
    composeTestRule.setContent { EditActivity(navigationActions, mockActivityModelView) }
    VerifyFieldsAreCorrect(composeTestRule)
    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("dateField").performTextInput("00000000")

    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(mockActivityRepositoryFirebase, never())
        .addActivity(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun noCharacterAllowedInDateField() {
    composeTestRule.setContent { EditActivity(navigationActions, mockActivityModelView) }
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
    composeTestRule.setContent { EditActivity(navigationActions, mockActivityModelView) }
    composeTestRule.onNodeWithTag("dateField").performTextClearance()
    composeTestRule.onNodeWithTag("dateField").performTextInput("01234567")
    composeTestRule.onNodeWithTag("dateField").performTextInput("8")
    val result =
        composeTestRule.onNodeWithTag("dateField").fetchSemanticsNode().config[EditableText]
    assert(result.text == "01/23/4567")
  }
}
