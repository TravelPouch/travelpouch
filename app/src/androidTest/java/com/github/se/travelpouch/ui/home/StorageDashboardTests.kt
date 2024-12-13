package com.github.se.travelpouch.ui.home

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.model.home.StorageDashboardStats
import com.github.se.travelpouch.model.home.StorageDashboardViewModel
import com.github.se.travelpouch.model.home.formatStorageUnit
import com.github.se.travelpouch.ui.navigation.NavigationActions
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class StorageDashboardTests {
  @Composable
  fun withActivityResultRegistry(
      activityResultRegistry: ActivityResultRegistry,
      content: @Composable () -> Unit
  ) {
    val activityResultRegistryOwner =
        object : ActivityResultRegistryOwner {
          override val activityResultRegistry = activityResultRegistry
        }
    CompositionLocalProvider(
        LocalActivityResultRegistryOwner provides activityResultRegistryOwner) {
          content()
        }
  }

  private lateinit var navigationActions: NavigationActions
  private lateinit var storageDashboardViewModel: StorageDashboardViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    storageDashboardViewModel = mock(StorageDashboardViewModel::class.java)
  }

  @Test
  fun everythingIsDisplayedWhenLoading() {
    `when` (storageDashboardViewModel.updateStorageStats()).then { }
    `when` (storageDashboardViewModel.storageStats).then { MutableStateFlow<StorageDashboardStats?>(null) }
    `when` (storageDashboardViewModel.isLoading).then { MutableStateFlow(true) }

    composeTestRule.setContent {
      withActivityResultRegistry(mock(ActivityResultRegistry::class.java)) {
        StorageDashboard(storageDashboardViewModel = storageDashboardViewModel, navigationActions)
      }
    }

    composeTestRule.onNodeWithTag("StorageDashboard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("StorageBar").assertIsDisplayed()

    // At this point the dashboard is still loading
    composeTestRule.onNodeWithTag("storageUsageByTravelTitle").assertIsNotDisplayed()
  }

  @Test
  fun everythingIsDisplayedOnceLoaded() {
    val storageStats = StorageDashboardStats(
        100L,
        50L,
        25L
    )
    `when` (storageDashboardViewModel.updateStorageStats()).then { }
    `when` (storageDashboardViewModel.storageStats).then { MutableStateFlow<StorageDashboardStats?>(storageStats) }
    `when` (storageDashboardViewModel.isLoading).then { MutableStateFlow(false) }

    composeTestRule.setContent {
      withActivityResultRegistry(mock(ActivityResultRegistry::class.java)) {
        StorageDashboard(storageDashboardViewModel = storageDashboardViewModel, navigationActions)
      }
    }

    composeTestRule.onNodeWithTag("storageUsageByTravelTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("storageLimitCard").assertIsDisplayed()
  }

  @Test
  fun storageLimitDialogIsDisplayed() {
    val storageStats = StorageDashboardStats(
      100L,
      50L,
      25L
    )
    `when` (storageDashboardViewModel.updateStorageStats()).then { }
    `when` (storageDashboardViewModel.storageStats).then { MutableStateFlow<StorageDashboardStats?>(storageStats) }
    `when` (storageDashboardViewModel.isLoading).then { MutableStateFlow(false) }

    composeTestRule.setContent {
      withActivityResultRegistry(mock(ActivityResultRegistry::class.java)) {
        StorageDashboard(storageDashboardViewModel = storageDashboardViewModel, navigationActions)
      }
    }

    composeTestRule.onNodeWithTag("storageLimitCard").performClick()
    composeTestRule.waitForIdle()
    val currentLimitText = composeTestRule.onNodeWithTag("storageLimitDialogCurrentLimitText")
    currentLimitText.assertIsDisplayed()
    currentLimitText.assertTextEquals("Current limit: ${formatStorageUnit(storageStats.storageLimit!!)}")
  }
}
