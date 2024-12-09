package com.github.se.travelpouch.ui.authentication

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.authentication.AuthenticationService
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.isValidEmail
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SignInWithPassword(
    navigationActions: NavigationActions,
    profileModelView: ProfileModelView,
    travelViewModel: ListTravelViewModel,
    authService: AuthenticationService
) {
  val context = LocalContext.current

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  val methodChosen = rememberSaveable { mutableStateOf(false) }
  val waitUntilProfileFetched = rememberSaveable { mutableStateOf(false) }

  // Navigate to the next screen after the profile is fetched
  LaunchedEffect(waitUntilProfileFetched.value) {
    if (waitUntilProfileFetched.value) {
      if (profileModelView.profile.value.needsOnboarding) {
        Toast.makeText(context, "Welcome to TravelPouch!", Toast.LENGTH_LONG).show()
        navigationActions.navigateTo(Screen.ONBOARDING)
      } else {
        navigationActions.navigateTo(Screen.TRAVEL_LIST)
      }
    }
  }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        TopAppBar(
            title = { Text("Signing in with password", Modifier.testTag("PasswordTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.AUTH) },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
          OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              modifier = Modifier.testTag("emailField"),
              label = { Text("Email") },
              placeholder = { Text("example@example.com") })

          Spacer(Modifier.padding(10.dp))

          OutlinedTextField(
              value = password,
              onValueChange = { password = it },
              modifier = Modifier.testTag("passwordField"),
              label = { Text("Password") },
              placeholder = { Text("password") },
              visualTransformation = PasswordVisualTransformation())

          Spacer(Modifier.padding(10.dp))

          Button(
              enabled =
                  email.isNotBlank() &&
                      password.isNotBlank() &&
                      isValidEmail(email) &&
                      !methodChosen.value,
              onClick = {
                methodChosen.value = true
                Log.d("SignInScreen", "Creating user with email and password")
                authService.createUser(
                    email,
                    password,
                    onSuccess = { user ->
                      Log.d(
                          "SignInScreen",
                          "User signed in: ${user?.uid} ${user?.email} ${user?.displayName} ${user?.isAnonymous}")

                      GlobalScope.launch {
                        profileModelView.initAfterLogin {
                          travelViewModel.initAfterLogin()
                          waitUntilProfileFetched.value = true
                        }
                      }

                      Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
                    },
                    onFailure = { task ->
                      methodChosen.value = false
                      Log.w(TAG, "createUserWithEmail:failure", task.exception)
                      Toast.makeText(
                              context,
                              "Signing in failed.",
                              Toast.LENGTH_SHORT,
                          )
                          .show()
                    })
              }) {
                Text("Sign up")
              }

          Spacer(Modifier.padding(5.dp))

          Button(
              enabled =
                  email.isNotBlank() &&
                      password.isNotBlank() &&
                      isValidEmail(email) &&
                      !methodChosen.value,
              onClick = {
                methodChosen.value = true
                authService.login(
                    email,
                    password,
                    onSuccess = { user ->
                      Log.d("SignInScreen", "User logged in: ${user?.displayName}")

                      GlobalScope.launch {
                        profileModelView.initAfterLogin {
                          travelViewModel.initAfterLogin()
                          waitUntilProfileFetched.value = true
                        }
                      }

                      Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
                    },
                    onFailure = { task ->
                      Log.w(TAG, "LoginWithEmailAndPassword:failure", task.exception)
                      Toast.makeText(
                              context,
                              "Authentication failed.",
                              Toast.LENGTH_SHORT,
                          )
                          .show()
                      methodChosen.value = false
                    })
              }) {
                Text("Log in")
              }
        }
      }
}
