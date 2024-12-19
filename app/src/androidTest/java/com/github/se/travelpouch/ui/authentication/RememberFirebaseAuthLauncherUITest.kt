// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.authentication

import androidx.compose.ui.test.junit4.createComposeRule
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class RememberFirebaseAuthLauncherUITest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun rememberFirebaseAuthLauncher() {
    val mockAuthResult = mock<AuthResult>()
    val mockApiException = mock<ApiException>()
    val onAuthComplete = mock<(AuthResult) -> Unit>()
    val onAuthError = mock<(ApiException) -> Unit>()

    `when`(onAuthComplete(mockAuthResult)).thenReturn(Unit)
    `when`(onAuthError(mockApiException)).thenReturn(Unit)

    composeTestRule.setContent { rememberFirebaseAuthLauncher(onAuthComplete, onAuthError) }
  }
}
