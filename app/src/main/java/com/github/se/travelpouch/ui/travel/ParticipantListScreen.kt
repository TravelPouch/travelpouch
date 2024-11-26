package com.github.se.travelpouch.ui.travel

import TruncatedText
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationContent
import com.github.se.travelpouch.model.notifications.NotificationType
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.fsUid
import com.github.se.travelpouch.ui.navigation.NavigationActions

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantListScreen(
    listTravelViewModel: ListTravelViewModel,
    navigationActions: NavigationActions,
    notificationViewModel: NotificationViewModel,
    profileViewModel: ProfileModelView
) {
  val context = LocalContext.current
  val selectedTravel by listTravelViewModel.selectedTravel.collectAsState()
  val participants by listTravelViewModel.participants.collectAsState()

  val (expanded, setExpanded) = remember { mutableStateOf(false) }
  val (expandedRoleDialog, setExpandedRoleDialog) = remember { mutableStateOf(false) }
  val (selectedParticipant, setSelectedParticipant) =
      remember { mutableStateOf<Map.Entry<fsUid, Profile>?>(null) }
  val (expandedAddUserDialog, setExpandedAddUserDialog) = remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag("participantListScreen"),
      topBar = {
        TopAppBar(
            modifier = Modifier.testTag("participantListSettingTopBar"),
            title = {
              Text(
                  "Participant Settings", modifier = Modifier.testTag("participantListSettingText"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Localized description")
                  }
            })
      },
      floatingActionButton = {
        ExtendedFloatingActionButton(
            text = { Text("Add user", modifier = Modifier.testTag("AddUserButton")) },
            icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add user") },
            onClick = {
                Log.d("ParticipantListScreen", "Add user button clicked")
                setExpandedAddUserDialog(true)
            },
            modifier = Modifier.testTag("importEmailButton"))
      }) { paddingValues ->
        if (selectedTravel != null) {
          LazyColumn(
              modifier =
                  Modifier.padding(paddingValues).fillMaxWidth().testTag("participantColumn")) {
                participants.entries.forEach { participantEntry ->
                  item {
                    // display all participants in travel
                    ParticipantRow(
                        participant = participantEntry,
                        selectedTravel = selectedTravel!!,
                        onClick = {
                          setSelectedParticipant(participantEntry)
                          setExpanded(true)
                        })
                    HorizontalDivider(thickness = 0.5.dp, color = Color.Gray)
                  }
                }
              }
        } else {
          Text(
              "No Travel is selected or the selected travel no longer exists.",
              modifier = Modifier.padding(paddingValues).testTag("noTravelSelected"))
        }

        selectedParticipant?.let { participant ->
          if (expanded) {
            Dialog(onDismissRequest = { setExpanded(false) }) {
              Box(
                  Modifier.fillMaxWidth(1f)
                      .height(250.dp)
                      .background(MaterialTheme.colorScheme.surface)
                      .testTag("participantDialogBox")) {
                    Column(modifier = Modifier.padding(8.dp).testTag("participantDialogColumn")) {
                      Row(
                          modifier = Modifier.testTag("participantDialogRow"),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.SpaceBetween) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Localized description",
                                modifier = Modifier.padding(5.dp).testTag("participantDialogIcon"))
                            TruncatedText(
                                text = participant.value.name,
                                fontWeight = FontWeight.Bold,
                                maxLength = 25,
                                modifier = Modifier.padding(5.dp).testTag("participantDialogName"))
                          }
                      TruncatedText(
                          text = participant.value.email,
                          maxLength = 30,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(5.dp).testTag("participantDialogEmail"))

                      RoleEntryDialog(
                          selectedTravel = selectedTravel,
                          participant = participant,
                          changeRoleAction = setExpandedRoleDialog,
                          removeParticipantAction = {
                            if (selectedTravel!!.allParticipants[Participant(participant.key)] ==
                                Role.OWNER) {
                              if (selectedTravel!!
                                  .allParticipants
                                  .values
                                  .count({ it == Role.OWNER }) == 1) {
                                Toast.makeText(
                                        context,
                                        "You're trying to remove the owner of the travel. Please name another owner before removing.",
                                        Toast.LENGTH_LONG)
                                    .show()
                                setExpanded(false)
                                return@RoleEntryDialog
                              }
                            }
                            val participantMap = selectedTravel!!.allParticipants.toMutableMap()
                            participantMap.remove(Participant(participant.key))
                            val participantList = selectedTravel!!.listParticipant.toMutableList()
                            participantList.remove(participant.key)
                            val updatedContainer =
                                selectedTravel!!.copy(
                                    allParticipants = participantMap.toMap(),
                                    listParticipant = participantList)
                            listTravelViewModel.updateTravel(updatedContainer)
                            listTravelViewModel.selectTravel(updatedContainer)
                            listTravelViewModel.fetchAllParticipantsInfo()
                            setExpanded(false)
                            Toast.makeText(context, "Participant removed", Toast.LENGTH_LONG).show()
                          })
                    }
                  }
            }
          }

          if (expandedAddUserDialog) {
            val addUserEmail = remember { mutableStateOf("") }
            Dialog(onDismissRequest = { setExpandedAddUserDialog(false) }) {
              Box(
                  Modifier.fillMaxWidth(1f)
                      .height(250.dp)
                      .background(MaterialTheme.colorScheme.surface)
                      .testTag("addUserDialogBox")) {
                    Column(
                        modifier =
                            Modifier.fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                                .testTag("roleDialogColumn"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                          Text(
                              "Add User by Email",
                              fontWeight = FontWeight.Bold,
                              modifier = Modifier.padding(8.dp).testTag("addUserDialogTitle"))
                          OutlinedTextField(
                              value = addUserEmail.value,
                              onValueChange = { addUserEmail.value = it },
                              label = { Text("Enter User's Email") },
                              placeholder = { Text("Enter User's Email") },
                              modifier = Modifier.testTag("addUserEmailField"))
                          Button(
                              onClick = {
                                profileViewModel.getFsUidByEmail(
                                    addUserEmail.value,
                                    onSuccess = { fsUid ->
                                      val isUserAlreadyAdded =
                                          selectedTravel!!.allParticipants.keys.any {
                                            it.fsUid == fsUid
                                          }
                                      if (fsUid == profileViewModel.profile.value.fsUid) {
                                        Toast.makeText(
                                                context,
                                                "Error: You can't invite yourself",
                                                Toast.LENGTH_SHORT)
                                            .show()
                                      } else if (isUserAlreadyAdded) {
                                        Toast.makeText(
                                                context,
                                                "Error: User already added",
                                                Toast.LENGTH_SHORT)
                                            .show()
                                      } else if (fsUid != null) {
                                        try {
                                          notificationViewModel.sendNotification(
                                              Notification(
                                                  notificationViewModel.getNewUid(),
                                                  profileViewModel.profile.value.fsUid,
                                                  fsUid,
                                                  selectedTravel!!.fsUid,
                                                  NotificationContent.InvitationNotification(
                                                      profileViewModel.profile.value.name,
                                                      selectedTravel!!.title,
                                                      Role.PARTICIPANT),
                                                  NotificationType.INVITATION))
                                        } catch (e: Exception) {
                                          Log.e(
                                              "NotificationError",
                                              "Failed to send notification: ${e.message}")
                                        }
                                        // Go back
                                        setExpandedAddUserDialog(false)
                                      } else {
                                        Toast.makeText(
                                                context,
                                                "Error: User with email not found",
                                                Toast.LENGTH_SHORT)
                                            .show()
                                      }
                                    },
                                    onFailure = { e ->
                                      Log.e(
                                          "EditTravelSettingsScreen",
                                          "Error getting fsUid by email",
                                          e)
                                      Toast.makeText(
                                              context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                          .show()
                                    })
                              },
                              modifier = Modifier.testTag("addUserButton")) {
                                Text("Add User")
                              }
                        }
                  }
            }
          }

          if (expandedRoleDialog) {
            Dialog(onDismissRequest = { setExpandedRoleDialog(false) }) {
              Box(
                  Modifier.fillMaxWidth(1f)
                      .height(250.dp)
                      .background(MaterialTheme.colorScheme.surface)
                      .testTag("roleDialogBox")) {
                    Column(
                        modifier =
                            Modifier.fillMaxSize().padding(16.dp).testTag("roleDialogColumn"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                          ChangeRoleDialog(selectedTravel, participant) { newRole ->
                            handleRoleChange(
                                context,
                                selectedTravel,
                                participant,
                                newRole,
                                listTravelViewModel,
                                notificationViewModel,
                                profileViewModel,
                                setExpandedRoleDialog,
                                setExpanded)
                          }
                        }
                  }
            }
          }
        }
      }
}

fun handleRoleChange(
    context: Context,
    selectedTravel: TravelContainer?,
    participant: Map.Entry<fsUid, Profile>,
    newRole: Role,
    listTravelViewModel: ListTravelViewModel,
    notificationViewModel: NotificationViewModel,
    profileViewModel: ProfileModelView,
    setExpandedRoleDialog: (Boolean) -> Unit,
    setExpanded: (Boolean) -> Unit
) {
  val oldRole = selectedTravel!!.allParticipants[Participant(participant.key)]
  if (oldRole == newRole) {
    // Role is already set to the new one
    Toast.makeText(context, "The role is already set to $newRole", Toast.LENGTH_LONG).show()
    setExpandedRoleDialog(false)
    setExpanded(false)
  } else if (oldRole == Role.OWNER &&
      selectedTravel.allParticipants.values.count { it == Role.OWNER } == 1) {
    // Trying to change the role of the only owner
    Toast.makeText(
            context,
            "You're trying to change the role of the only owner of the travel. Please name another owner before changing the role.",
            Toast.LENGTH_LONG)
        .show()
    setExpandedRoleDialog(false)
    setExpanded(false)
  } else {
    if (oldRole == Role.OWNER) {
      // Actual role change logic
      if (participant.key != profileViewModel.profile.value.fsUid) {
        notificationViewModel.sendNotification(
            Notification(
                notificationViewModel.getNewUid(),
                profileViewModel.profile.value.fsUid,
                participant.key,
                selectedTravel.fsUid,
                NotificationContent.RoleChangeNotification(selectedTravel.title, newRole),
                NotificationType.ROLE_UPDATE))
      }
      val participantMap = selectedTravel.allParticipants.toMutableMap()
      participantMap[Participant(participant.key)] = newRole
      val updatedContainer = selectedTravel.copy(allParticipants = participantMap.toMap())
      listTravelViewModel.updateTravel(updatedContainer)
      listTravelViewModel.selectTravel(updatedContainer)
      setExpandedRoleDialog(false)
      setExpanded(false)
      listTravelViewModel.fetchAllParticipantsInfo()
    } else {
      Toast.makeText(
              context,
              "You do not have the permission to change the role of this participant",
              Toast.LENGTH_LONG)
          .show()
    }
  }
}
