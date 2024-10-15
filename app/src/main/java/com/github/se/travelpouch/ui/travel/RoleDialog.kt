package com.github.se.travelpouch.ui.travel

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.UserInfo
import com.github.se.travelpouch.model.fsUid

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
    participant: Map.Entry<fsUid, UserInfo>,
    onRoleChange: (Role) -> Unit,
) {
  Text("Select a Role", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
  Text(
      "Current Role: ${selectedTravel!!.allParticipants[Participant(participant.key)]!!.name}",
      modifier = Modifier.padding(8.dp))
  Button(
      onClick = {
        // Handle OWNER selection
        onRoleChange(Role.OWNER)
      }) {
        Text("OWNER")
      }

  Button(
      onClick = {
        // Handle ORGANISER selection
        onRoleChange(Role.ORGANIZER)
      }) {
        Text("ORGANISER")
      }

  Button(
      onClick = {
        // Handle PARTICIPANT selection
        onRoleChange(Role.PARTICIPANT)
      }) {
        Text("PARTICIPANT")
      }
}

/**
 * A composable function that displays a dialog with options to change the role of a participant
 * or remove the participant from the travel.
 *
 * @param selectedTravel The currently selected travel container.
 * @param participant The participant whose role is to be changed or who is to be removed.
 * @param changeRoleAction A callback function to handle the action of changing the role.
 * @param removeParticipantAction A callback function to handle the action of removing the participant.
 */
@Composable
fun RoleEntryDialog(
    selectedTravel: TravelContainer?,
    participant: Map.Entry<fsUid, UserInfo>,
    changeRoleAction: (Boolean) -> Unit,
    removeParticipantAction: () -> Unit
) {
  Text(
      text = "Role : ${selectedTravel!!.allParticipants[Participant(participant.key)]!!.name}",
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(8.dp))
    // we just expand the other dialog
  Button(onClick = { changeRoleAction(true) }) { Text("Change Role") }

  Button(
      onClick = {
        // Handle Remove participant
        removeParticipantAction()
      }) {
        Text("Remove participant")
      }
}
