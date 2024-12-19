// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationContent
import com.github.se.travelpouch.model.notifications.NotificationSector
import com.github.se.travelpouch.model.notifications.NotificationStatus
import com.github.se.travelpouch.model.notifications.NotificationType
import com.github.se.travelpouch.model.notifications.NotificationViewModel
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
    profileModelView: ProfileModelView,
    notificationViewModel: NotificationViewModel
) {
  val profile = profileModelView.profile.collectAsState()
  val context = LocalContext.current

  var email by remember { mutableStateOf(profile.value.email) }
  var username by remember { mutableStateOf(profile.value.username) }
  var name by remember { mutableStateOf(profile.value.name) }
  var openDialog by remember { mutableStateOf(false) }
  var openDialogToDeleteFriend by remember { mutableStateOf(false) }
  var friendUidToDelete by remember { mutableStateOf("") }

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
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(pd)
                .testTag("ProfileColumn")
                .verticalScroll(rememberScrollState())) {
          OutlinedTextField(
              value = email,
              onValueChange = {},
              enabled = false,
              label = { Text("Email") },
              modifier = Modifier.fillMaxWidth().testTag("emailField"))

          OutlinedTextField(
              value = username,
              onValueChange = { username = it },
              enabled = true,
              label = { Text("Username") },
              modifier = Modifier.fillMaxWidth().testTag("usernameField"))

          OutlinedTextField(
              value = name,
              onValueChange = { name = it },
              enabled = true,
              label = { Text("Name") },
              modifier = Modifier.fillMaxWidth().testTag("nameField"))

          if (profile.value.friends.isNotEmpty()) {

            Text("Friends : ", modifier = Modifier.testTag("friendsText"))

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).height(150.dp),
            ) {
              val friends = profile.value.friends.keys.toList()

              items(friends.size) { friend ->
                Card(
                    modifier =
                        Modifier.testTag("friendCard${friend}")
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .clickable {
                              val uid = profile.value.friends[friends[friend]]
                              if (uid == null) {
                                Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG)
                                    .show()
                              } else {
                                friendUidToDelete = uid
                                openDialogToDeleteFriend = true
                              }
                            },
                    colors =
                        CardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            disabledContentColor = MaterialTheme.colorScheme.inverseSurface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                        )) {
                      Box(Modifier.fillMaxSize().testTag("boxOfFriend${friend}")) {
                        Text(
                            friends[friend],
                            Modifier.align(Alignment.Center).testTag("friend_${friend}"))
                      }
                    }
              }
            }
          }

          Button(
              onClick = { openDialog = true },
              modifier = Modifier.testTag("floatingButtonAddingFriend")) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.padding(horizontal = 10.dp) // Adjust padding for better alignment
                    ) {
                      Icon(
                          imageVector =
                              Icons.Default.PersonAddAlt1, // Replace with your mail icon resource
                          contentDescription = null,
                          modifier =
                              Modifier.size(24.dp)
                                  .testTag("addingFriendIcon") // Adjust size as needed
                          )
                      Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                      Text(
                          text = "Add Friend",
                          style = MaterialTheme.typography.bodyLarge // Or customize further
                          )
                    }
              }

          Button(
              onClick = {
                val newProfile =
                    Profile(
                        profile.value.fsUid,
                        username,
                        email,
                        profile.value.friends,
                        name,
                        profile.value.userTravelList)
                profileModelView.updateProfile(newProfile, context)
                navigationActions.navigateTo(Screen.PROFILE)
              },
              modifier = Modifier.testTag("saveButton")) {
                Text("Save")
              }
        }

    if (openDialog) {
      Dialog(onDismissRequest = { openDialog = false }) {
        var friendMail by remember { mutableStateOf("") }
        Box(
            Modifier.fillMaxWidth(1f)
                .height(250.dp)
                .background(MaterialTheme.colorScheme.surface)
                .testTag("boxAddingFriend")) {
              Column(
                  modifier =
                      Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center) {
                    Text("Adding a friend", modifier = Modifier.testTag("addingFriendTitle"))

                    OutlinedTextField(
                        value = friendMail,
                        onValueChange = { friendMail = it },
                        label = { Text("Friend Email") },
                        placeholder = { Text("example@example.com") },
                        modifier = Modifier.testTag("addingFriendField"))

                    Button(
                        onClick = {
                          if (friendMail == profile.value.email) {
                            Log.d("Friend added", "It is you")
                            Toast.makeText(context, "You cannot add yourself", Toast.LENGTH_LONG)
                                .show()
                          } else if (profile.value.friends.contains(friendMail)) {
                            Log.d("Friend added", "Already friend with this user")
                            Toast.makeText(
                                    context,
                                    "You cannot add a friend you are already friend with",
                                    Toast.LENGTH_LONG)
                                .show()
                          } else {
                            profileModelView.sendFriendNotification(
                                email = friendMail,
                                onSuccess = { friendUid ->
                                  Toast.makeText(context, "Invitation sent", Toast.LENGTH_LONG)
                                      .show()
                                    val notificationContent =
                                        NotificationContent.FriendInvitationNotification(
                                            profile.value.email)
                                  notificationViewModel.sendNotification(
                                      Notification(
                                          notificationViewModel.getNewUid(),
                                          senderUid = profile.value.fsUid,
                                          receiverUid = friendUid,
                                          travelUid = null,
                                          notificationContent,
                                          notificationType = NotificationType.INVITATION,
                                          status = NotificationStatus.UNREAD,
                                          sector = NotificationSector.PROFILE))
                                    notificationViewModel.sendNotificationToUser(
                                        friendUid,
                                        notificationContent)

                                  openDialog = false
                                },
                                onFailure = { e ->
                                  Toast.makeText(context, e.message!!, Toast.LENGTH_LONG).show()
                                })
                          }
                        },
                        modifier = Modifier.testTag("addingFriendButton")) {
                          Text("Add Friend")
                        }
                  }
            }
      }
    }

    if (openDialogToDeleteFriend) {
      Dialog(onDismissRequest = { openDialogToDeleteFriend = false }) {
        Box(
            Modifier.background(MaterialTheme.colorScheme.surface)
                .height(100.dp)
                .width(200.dp)
                .testTag("boxDeletingFriend")) {
              Column(
                  modifier = Modifier.fillMaxSize().padding(16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center) {
                    Text("Deleting a friend", modifier = Modifier.testTag("DeletingFriendTitle"))

                    Button(
                        onClick = {
                          profileModelView.removeFriend(
                              friendUidToDelete,
                              onFailure = { e ->
                                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                              },
                              onSuccess = {
                                Toast.makeText(context, "Friend deleted", Toast.LENGTH_LONG).show()
                              })
                          openDialogToDeleteFriend = false
                        },
                        modifier = Modifier.testTag("deletingFriendButton")) {
                          Text("Delete Friend")
                        }
                  }
            }
      }
    }
  }
}
