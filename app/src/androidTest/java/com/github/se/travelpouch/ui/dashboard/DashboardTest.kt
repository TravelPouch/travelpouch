package com.github.se.travelpouch.ui.dashboard

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DashboardTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { DashboardScreen() }
  }

  @Test
  fun verifiesEverythingDisplayed() {
    composeTestRule.onNodeWithTag("DashboardScreen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("addTodoTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addTodoTitle").assertTextEquals("Create a new task")

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("TitleFieldTag").assertIsDisplayed()
    composeTestRule.onNodeWithTag("TitleFieldTag").assertTextEquals("Title", "Title_Field")
    composeTestRule.onNodeWithTag("TitleFieldTag").assert(hasText("Title_Field"))

    composeTestRule.onNodeWithTag("DestinationFieldTag").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("DestinationFieldTag")
        .assertTextEquals("Destination", "Destination_Field")
    composeTestRule.onNodeWithTag("DestinationFieldTag").assert(hasText("Destination_Field"))

    composeTestRule.onNodeWithTag("BudgetFieldTag").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BudgetFieldTag").assertTextEquals("Budget", "Budget_Field")
    composeTestRule.onNodeWithTag("BudgetFieldTag").assert(hasText("Budget_Field"))

    composeTestRule.onNodeWithTag("ParticipantsFieldTag").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("ParticipantsFieldTag")
        .assertTextEquals("Travel Companions", "Travel_Companions_Field")
    composeTestRule.onNodeWithTag("ParticipantsFieldTag").assert(hasText("Travel_Companions_Field"))

    composeTestRule.onNodeWithTag("StartDateFieldTag").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("StartDateFieldTag")
        .assertTextEquals("Start Date", "Start_Date_Field")
    composeTestRule.onNodeWithTag("StartDateFieldTag").assert(hasText("Start_Date_Field"))

    composeTestRule.onNodeWithTag("EndDateFieldTag").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EndDateFieldTag").assertTextEquals("End Date", "End_Date_Field")
    composeTestRule.onNodeWithTag("EndDateFieldTag").assert(hasText("End_Date_Field"))

    composeTestRule.onNodeWithTag("DescriptionFieldTag").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("DescriptionFieldTag")
        .assertTextEquals("Description", "Description_Field")
    composeTestRule.onNodeWithTag("DescriptionFieldTag").assert(hasText("Description_Field"))
  }
}
