package com.github.se.travelpouch.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen

/**
 * This screen allows us to edit out profile information.
 *
 * @param navigationActions (NavigationActions) : the navigation actions used to navigate between
 *   screens
 * @param profileModelView (ProfileModelView) : the model view used to interact between profile
 *   information and the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyingProfileScreen(
    navigationActions: NavigationActions,
    profileModelView: ProfileModelView
) {
  val profile = profileModelView.profile.collectAsState()
  val context = LocalContext.current

  var email by remember { mutableStateOf(profile.value.email) }
  var username by remember { mutableStateOf(profile.value.username) }

  Scaffold(
      modifier = Modifier.testTag("ProfileScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Profile", Modifier.testTag("ProfileBar")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.PROFILE) },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            },
        )
      },
  ) { pd ->
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(pd)
                .testTag("ProfileColumn")) {
          OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              enabled = true,
              label = { Text("Email") },
              modifier = Modifier.testTag("emailField"))

          OutlinedTextField(
              value = username,
              onValueChange = { username = it },
              enabled = true,
              label = { Text("Username") },
              modifier = Modifier.testTag("usernameField"))

          OutlinedTextField(
              value = "No Friend, sadge :(",
              onValueChange = {},
              enabled = false,
              label = { Text("Friends") },
              modifier = Modifier.testTag("friendsField"))

          Button(
              onClick = {
                val newProfile =
                    Profile(profile.value.fsUid, username, email, null, "name", emptyList())
                profileModelView.updateProfile(newProfile, context)
                navigationActions.navigateTo(Screen.PROFILE)
              },
              modifier = Modifier.testTag("saveButton")) {
                Text("Save")
              }
        }
  }
}
