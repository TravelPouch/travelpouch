package com.github.se.travelpouch.ui.authentication

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.github.se.travelpouch.AuthenticationService
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

  Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      OutlinedTextField(
          value = email, onValueChange = { email = it }, modifier = Modifier.testTag("emailField"))

      OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          modifier = Modifier.testTag("passwordField"))

      Button(
          onClick = {
            authService.createUser(
                email,
                password,
                onSuccess = { user ->
                  Log.d("SignInScreen", "User signed in: ${user?.displayName}")

                  val job =
                      GlobalScope.launch {
                        profileModelView.initAfterLogin { travelViewModel.initAfterLogin() }
                      }

                  Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
                  navigationActions.navigateTo(Screen.TRAVEL_LIST)
                },
                onFailure = { task ->
                  Log.w(TAG, "createUserWithEmail:failure", task.exception)
                  Toast.makeText(
                          context,
                          "Authentication failed.",
                          Toast.LENGTH_SHORT,
                      )
                      .show()
                })
          }) {
            Text("Sign in")
          }
    }
  }
}
