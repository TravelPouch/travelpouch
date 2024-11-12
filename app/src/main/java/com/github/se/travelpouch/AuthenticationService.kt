package com.github.se.travelpouch

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface AuthenticationService {
  fun createUser(
      email: String,
      password: String,
      context: Context,
      profileModelView: ProfileModelView,
      travelViewModel: ListTravelViewModel,
      navigationActions: NavigationActions
  )
}

class FirebaseAuthenticationService(private val auth: FirebaseAuth) : AuthenticationService {
  override fun createUser(
      email: String,
      password: String,
      context: Context,
      profileModelView: ProfileModelView,
      travelViewModel: ListTravelViewModel,
      navigationActions: NavigationActions
  ) {
    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
      if (task.isSuccessful) {
        // Sign in success, update UI with the signed-in user's information
        Log.d(TAG, "createUserWithEmail:success")
        val user = auth.currentUser

        Log.d("SignInScreen", "User signed in: ${user?.displayName}")

        val job =
            GlobalScope.launch {
              profileModelView.initAfterLogin { travelViewModel.initAfterLogin() }
            }

        Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
        navigationActions.navigateTo(Screen.TRAVEL_LIST)
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
  }
}

class MockFirebaseAuthenticationService : AuthenticationService {
  override fun createUser(
      email: String,
      password: String,
      context: Context,
      profileModelView: ProfileModelView,
      travelViewModel: ListTravelViewModel,
      navigationActions: NavigationActions
  ) {

    Log.d("ENDTOEND-FINAL", "in the mock sign in")

    val job =
        GlobalScope.launch { profileModelView.initAfterLogin { travelViewModel.initAfterLogin() } }

    Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
    navigationActions.navigateTo(Screen.TRAVEL_LIST)
  }
}
