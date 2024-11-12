package com.github.se.travelpouch.ui.authentication

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
    travelViewModel: ListTravelViewModel,
    isLoading: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
) {
  val context = LocalContext.current
  val isLoading: MutableState<Boolean> = isLoading

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
              modifier = Modifier.fillMaxWidth().height(150.dp).testTag("appLogo"))

          Spacer(modifier = Modifier.height(16.dp))

          // Welcome Text
          Text(
              modifier = Modifier.testTag("welcomText").padding(horizontal = 16.dp),
              text = "Welcome",
              style =
                  MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp, lineHeight = 44.sp),
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(48.dp))


          Box(
              modifier =
                  Modifier.fillMaxWidth(0.8f) // Fixed width for both button and spinner
                      .height(56.dp), // Fixed height for both button and spinner
              contentAlignment = Alignment.Center) {

                // Google Sign-In Button (before the loading state)
                this@Column.AnimatedVisibility(
                    visible = !isLoading.value,
                    enter = fadeIn(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(300))) {
                      // Assuming `GoogleSignInButton` is provided by Google Sign-In SDK
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
                      
                      Button(onClick = { navigationActions.navigateTo(Screen.SIGN_IN_PASSWORD) }) {
            Text("Sign in with email and password")
          }
                    }

                // CircularProgressIndicator (when loading)
                this@Column.AnimatedVisibility(
                    visible = isLoading.value,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))) {
                      CircularProgressIndicator(
                          modifier =
                              Modifier.height(28.dp)
                                  .testTag(
                                      "loadingSpinner"), // Same height as Google Sign-In button
                          color = MaterialTheme.colorScheme.primary,
                          strokeWidth = 5.dp)
                    }
              }

        }
      })
}
