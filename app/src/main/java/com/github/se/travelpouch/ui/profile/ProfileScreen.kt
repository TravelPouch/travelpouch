package com.github.se.travelpouch.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen

/**
 * This screen displays the information of the user profile.
 *
 * @param navigationActions (NavigationActions) : the navigation actions used to navigate between
 *   screens
 * @param profileModelView (ProfileModelView) : the model view used to interact between profile
 *   information and the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navigationActions: NavigationActions, profileModelView: ProfileModelView) {

  profileModelView.getProfile()

  val profile = profileModelView.profile.collectAsState()

  Scaffold(
      modifier = Modifier.testTag("ProfileScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Profile", Modifier.testTag("ProfileBar")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES) },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            },
            actions = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) },
                  modifier = Modifier.testTag("settingsButton")) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                  }
            })
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
              value = profile.value.email,
              onValueChange = {},
              enabled = false,
              label = { Text("Email") },
              modifier = Modifier.testTag("emailField"))

          OutlinedTextField(
              value = profile.value.username,
              onValueChange = {},
              enabled = false,
              label = { Text("Username") },
              modifier = Modifier.testTag("usernameField"))

          OutlinedTextField(
              value = "No Friend, sadge :(",
              onValueChange = {},
              enabled = false,
              label = { Text("Friends") },
              modifier = Modifier.testTag("friendsField"))
        }
  }
}
