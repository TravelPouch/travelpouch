package com.github.se.travelpouch.endtoend

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.se.travelpouch.MainActivity
import org.junit.Rule

class EndToEndTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()
}
