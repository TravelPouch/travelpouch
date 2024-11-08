package com.github.se.travelpouch.ui.authentication

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.travelpouch.R
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A composable function that displays the sign-in screen.
 *
 * @param navigationActions An instance of `NavigationActions` to handle navigation events.
 */
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun SignInScreen(
    navigationActions: NavigationActions,
    profileModelView: ProfileModelView,
    travelViewModel: ListTravelViewModel
) {
  val context = LocalContext.current
  val isLoading = rememberSaveable { mutableStateOf(false) }

  // launcher for Firebase authentication
  val launcher =
      rememberFirebaseAuthLauncher(
          onAuthComplete = { result ->
            Log.d("SignInScreen", "User signed in: ${result.user?.displayName}")

            val job =
                GlobalScope.launch {
                  profileModelView.initAfterLogin { travelViewModel.initAfterLogin() }
                  isLoading.value = false
                }

            Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
            navigationActions.navigateTo(Screen.TRAVEL_LIST)
          },
          onAuthError = {
            isLoading.value = false
            Log.e("SignInScreen", "Failed to sign in: ${it.statusCode}")
            Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
          })

  val token = stringResource(R.string.default_web_client_id)

  // The main container for the screen
  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("loginScreenScaffold"),
      content = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("loginScreenColumn"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
          // App Logo Image
          Image(
              painter = painterResource(id = R.drawable.travelpouch_logo),
              contentDescription = "App Logo",
              modifier = Modifier.size(250.dp).testTag("appLogo"))

          Spacer(modifier = Modifier.height(16.dp))

          // Welcome Text
          Text(
              modifier = Modifier.testTag("welcomText"),
              text = "Welcome",
              style =
                  MaterialTheme.typography.headlineLarge.copy(fontSize = 57.sp, lineHeight = 64.sp),
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(48.dp))

          // Use AnimatedVisibility to smoothly transition between button and progress indicator
          AnimatedVisibility(
              visible = !isLoading.value,
              enter = fadeIn(animationSpec = tween(150)),
              exit = fadeOut(animationSpec = tween(300))) {
                GoogleSignInButton(
                    onSignInClick = {
                      val gso =
                          GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                              .requestIdToken(token)
                              .requestEmail()
                              .build()
                      val googleSignInClient = GoogleSignIn.getClient(context, gso)
                      launcher.launch(googleSignInClient.signInIntent)
                      isLoading.value = true
                    })
                // Authenticate With Google Button
              }

          // Show CircularProgressIndicator when loading
          AnimatedVisibility(
              visible = isLoading.value,
              enter = fadeIn(animationSpec = tween(300)),
              exit = fadeOut(animationSpec = tween(300))) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(0.25f).testTag("loadingSpinner"),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 5.dp)
              }
        }
      })
}
