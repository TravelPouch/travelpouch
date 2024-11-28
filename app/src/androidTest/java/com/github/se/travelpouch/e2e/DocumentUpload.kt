package com.github.se.travelpouch.e2e

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.se.travelpouch.MainActivity
import com.github.se.travelpouch.di.AppModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class)
class DocumentUpload {
  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() {
    hiltRule.inject()
  }

  @Test fun userFlowForDocumentUpload() = runTest {}
}
