// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.travel

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.fsUid

/**
 * A composable function that displays a dialog for changing the role of a participant.
 *
 * @param selectedTravel The currently selected travel container.
 * @param participant The participant whose role is to be changed.
 * @param onRoleChange A callback function to handle the role change.
 */
@Composable
fun ChangeRoleDialog(
    selectedTravel: TravelContainer?,
    participant: Map.Entry<fsUid, Profile>,
    onRoleChange: (Role) -> Unit,
) {
  Text(
      "Select a Role",
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(8.dp).testTag("roleDialogTitle"))
  Text(
      "Current Role: ${selectedTravel!!.allParticipants[Participant(participant.key)]!!.name}",
      modifier = Modifier.padding(8.dp).testTag("roleDialogCurrentRole"))
  Button(
      modifier = Modifier.testTag("ownerButton"),
      onClick = {
        // Handle OWNER selection
        onRoleChange(Role.OWNER)
      }) {
        Text("OWNER")
      }

  Button(
      modifier = Modifier.testTag("organizerButton"),
      onClick = {
        // Handle ORGANISER selection
        onRoleChange(Role.ORGANIZER)
      }) {
        Text("ORGANISER")
      }

  Button(
      modifier = Modifier.testTag("participantButton"),
      onClick = {
        // Handle PARTICIPANT selection
        onRoleChange(Role.PARTICIPANT)
      }) {
        Text("PARTICIPANT")
      }
}

/**
 * A composable function that displays a dialog with options to change the role of a participant or
 * remove the participant from the travel.
 *
 * @param selectedTravel The currently selected travel container.
 * @param participant The participant whose role is to be changed or who is to be removed.
 * @param changeRoleAction A callback function to handle the action of changing the role.
 * @param removeParticipantAction A callback function to handle the action of removing the
 *   participant.
 */
@Composable
fun RoleEntryDialog(
    selectedTravel: TravelContainer?,
    participant: Map.Entry<fsUid, Profile>,
    changeRoleAction: (Boolean) -> Unit,
    removeParticipantAction: () -> Unit
) {
  Text(
      text = "Role : ${selectedTravel!!.allParticipants[Participant(participant.key)]!!.name}",
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(8.dp).testTag("participantDialogRole"))
  // we just expand the other dialog
  Button(onClick = { changeRoleAction(true) }, modifier = Modifier.testTag("changeRoleButton")) {
    Text("Change Role")
  }

  Button(
      modifier = Modifier.testTag("removeParticipantButton"),
      onClick = {
        // Handle Remove participant
        removeParticipantAction()
      }) {
        Text("Remove participant")
      }
}
