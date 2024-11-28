package com.github.se.travelpouch.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
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
                  onClick = { navigationActions.navigateTo(Screen.TRAVEL_LIST) },
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
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(pd)
                .testTag("ProfileColumn")) {
          OutlinedTextField(
              value = profile.value.email,
              onValueChange = {},
              enabled = false,
              label = { Text("Email") },
              modifier = Modifier.fillMaxWidth().testTag("emailField"))

          OutlinedTextField(
              value = profile.value.username,
              onValueChange = {},
              enabled = false,
              label = { Text("Username") },
              modifier = Modifier.fillMaxWidth().testTag("usernameField"))

          OutlinedTextField(
              value = profile.value.name,
              onValueChange = {},
              enabled = false,
              label = { Text("Name") },
              modifier = Modifier.fillMaxWidth().testTag("nameField"))

          Text("Friends : ", modifier = Modifier.testTag("friendsText"))

          LazyColumn(
              modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
              contentPadding = PaddingValues(bottom = 80.dp)) {
                if (profile.value.friends.isNotEmpty()) {

                  items(profile.value.friends.size) { friend ->
                    Card(
                        modifier =
                            Modifier.testTag("friendCard${friend}")
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                        colors =
                            CardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                disabledContentColor = MaterialTheme.colorScheme.inverseSurface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                            )) {
                          Box(Modifier.fillMaxSize().testTag("boxOfFriend${friend}")) {
                            Text(
                                profile.value.friends[friend],
                                Modifier.align(Alignment.Center).testTag("friend_${friend}"))
                          }
                        }
                  }
                } else {
                  item {
                    Card(
                        modifier =
                            Modifier.testTag("emptyFriendCard")
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                        colors =
                            CardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                disabledContentColor = MaterialTheme.colorScheme.inverseSurface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                            )) {
                          Box(Modifier.fillMaxSize().testTag("emptyFriendBox")) {
                            Text(
                                "No friends are saved",
                                Modifier.align(Alignment.Center).testTag("emptyFriendText"))
                          }
                        }
                  }
                }
              }
        }
  }
}
