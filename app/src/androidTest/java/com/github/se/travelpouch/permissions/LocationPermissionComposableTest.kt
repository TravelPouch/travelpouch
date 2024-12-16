// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LocationPermissionComposableTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testPermissionGrantedCallsOnPermissionGranted() {

    mockkStatic(ContextCompat::class)

    // Mock the required permissions
    every {
      ContextCompat.checkSelfPermission(any(), Manifest.permission.ACCESS_FINE_LOCATION)
    } returns PackageManager.PERMISSION_GRANTED
    every {
      ContextCompat.checkSelfPermission(any(), Manifest.permission.ACCESS_COARSE_LOCATION)
    } returns PackageManager.PERMISSION_GRANTED

    var isPermissionGrantedCalled = false

    composeTestRule.setContent {
      LocationPermissionComposable(
          onPermissionGranted = { isPermissionGrantedCalled = true }, onPermissionDenied = {})
    }

    // Assert that the callback was invoked
    composeTestRule.runOnIdle { assertTrue(isPermissionGrantedCalled) }
  }

  @Test
  fun testPermissionDeniedCallsOnPermissionDenied() {

    mockkStatic(ContextCompat::class)

    // Mock the required permissions
    every {
      ContextCompat.checkSelfPermission(any(), Manifest.permission.ACCESS_FINE_LOCATION)
    } returns PackageManager.PERMISSION_DENIED
    every {
      ContextCompat.checkSelfPermission(any(), Manifest.permission.ACCESS_COARSE_LOCATION)
    } returns PackageManager.PERMISSION_DENIED

    var isPermissionDeniedCalled = false

    composeTestRule.setContent {
      LocationPermissionComposable(
          onPermissionGranted = {}, onPermissionDenied = { isPermissionDeniedCalled = true })
    }

    // Assert that the callback was invoked
    composeTestRule.runOnIdle { assertTrue(isPermissionDeniedCalled) }
  }
}
