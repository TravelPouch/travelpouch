package com.github.se.travelpouch

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test fun testMainActivityDisplaysCorrectContent() {}
}
