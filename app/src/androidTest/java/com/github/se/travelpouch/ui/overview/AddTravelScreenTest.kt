package com.github.se.travelpouch.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class AddTravelScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displayAllComponents() {
        composeTestRule.setContent { AddTravelScreen() }

        composeTestRule.onNodeWithTag("addTravelScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("travelTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("travelTitle").assertTextEquals("Create a new travel")
        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("travelSaveButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("travelSaveButton").assertTextEquals("Save")

        composeTestRule.onNodeWithTag("inputTravelTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("inputTravelDescription").assertIsDisplayed()
        composeTestRule.onNodeWithTag("inputTravelStartDate").assertIsDisplayed()
        composeTestRule.onNodeWithTag("inputTravelEndDate").assertIsDisplayed()
    }

    @Test
    fun invalidDateDoesNotTriggerSave() {
        composeTestRule.setContent { AddTravelScreen() }

        // Input invalid date format
        composeTestRule.onNodeWithTag("inputTravelStartDate").performTextInput("invalidDate")
        composeTestRule.onNodeWithTag("inputTravelEndDate").performTextInput("invalidDate")
        composeTestRule.onNodeWithTag("travelSaveButton").performClick()

        // Assert that the Save action was not triggered
        composeTestRule.onNodeWithTag("invalidDateToast").assertIsDisplayed()
    }

    @Test
    fun validDateAllowsSave() {
        composeTestRule.setContent { AddTravelScreen() }

        // Input valid date format
        composeTestRule.onNodeWithTag("inputTravelStartDate").performTextInput("10/10/2024")
        composeTestRule.onNodeWithTag("inputTravelEndDate").performTextInput("15/10/2024")

        composeTestRule.onNodeWithTag("travelSaveButton").performClick()

        // Assuming the navigation goes back or shows a success toast
        composeTestRule.onNodeWithTag("successToast").assertIsDisplayed()
    }

    @Test
    fun invalidStartDatePreventsSave() {
        composeTestRule.setContent { AddTravelScreen() }

        composeTestRule.onNodeWithTag("inputTravelStartDate").performTextClearance()
        composeTestRule.onNodeWithTag("inputTravelStartDate").performTextInput("invalidDate")
        composeTestRule.onNodeWithTag("travelSaveButton").performClick()

        // Assert that no save action occurs when date is invalid
        composeTestRule.onNodeWithTag("invalidDateToast").assertIsDisplayed()
    }

    @Test
    fun saveButtonDisabledWhenTitleIsBlank() {
        composeTestRule.setContent { AddTravelScreen() }

        // Leave title blank
        composeTestRule.onNodeWithTag("inputTravelTitle").performTextClearance()
        composeTestRule.onNodeWithTag("inputTravelStartDate").performTextInput("10/10/2024")
        composeTestRule.onNodeWithTag("inputTravelEndDate").performTextInput("15/10/2024")

        composeTestRule.onNodeWithTag("travelSaveButton").assertIsNotEnabled()
    }

    @Test
    fun goBackButtonNavigatesBack() {
        composeTestRule.setContent { AddTravelScreen() }

        // Simulate a click on the back button
        composeTestRule.onNodeWithTag("goBackButton").performClick()

        // Assert that the navigation back action was triggered
        // Assuming a toast or some other indicator for back navigation
        composeTestRule.onNodeWithTag("backNavigationTriggered").assertIsDisplayed()
    }
}
