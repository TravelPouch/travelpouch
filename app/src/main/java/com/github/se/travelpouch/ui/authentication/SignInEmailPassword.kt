package com.github.se.travelpouch.ui.authentication

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun SignInEmailPassword(
    navigationActions: NavigationActions,
    profileModelView: ProfileModelView,
    travelViewModel: ListTravelViewModel
) {
  val context = LocalContext.current

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("loginScreenScaffold"),
      content = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("loginScreenColumn"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
          // Welcome Text
          OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              modifier = Modifier.testTag("emailSignIn"))

          OutlinedTextField(
              value = password,
              onValueChange = { password = it },
              modifier = Modifier.testTag("passwordSignIn"))

          Spacer(modifier = Modifier.height(48.dp))

          Button(
              onClick = {
                Firebase.auth
                    .createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                      if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                          Log.d("ENDTOENDTEST", "before the job")

                          val job =
                            GlobalScope.launch {
                                Log.d("ENDTOENDTEST", "in the coroutine")

                                profileModelView.initAfterLogin {
                                  //travelViewModel.initAfterLogin()
                                  Log.d("ENDTOENDTEST", "before the toast")
                                  Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
                                  Log.d("ENDTOENDTEST", "before the navigate to")
                                  navigationActions.navigateTo(Screen.TRAVEL_LIST)
                                  Log.d("ENDTOENDTEST", "after the navigate to")

                              }
                                Log.d("ENDTOENDTEST", "end of the coroutine")

                            }

                          Log.d("ENDTOENDTEST", "outside of coroutine")



                      } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                                context,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                      }
                    }
              },
              enabled = true) {
                Text("Create account and Log in")
              }
        }
      })
}
