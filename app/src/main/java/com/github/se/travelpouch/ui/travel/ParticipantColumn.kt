// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.travel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.fsUid

@Composable
fun ParticipantColumn(
    participant: Map.Entry<fsUid, Profile>,
    selectedTravel: TravelContainer,
    onClick: () -> Unit
) {

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .testTag("participantColumn")
              .clickable { onClick() }
              .padding(10.dp) // Apply padding for the entire column
      ) {
        // Icon on top
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Localized description",
            modifier =
                Modifier.padding(bottom = 8.dp) // Add space below the icon
                    .testTag("participantIcon"))

        // Username
        Text(
            text = participant.value.name,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.titleSmall,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(bottom = 4.dp) // Add space between text elements
                    .testTag("participantName"))

        // Email
        Text(
            text = participant.value.email,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.titleSmall,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(bottom = 4.dp) // Add space between text elements
                    .testTag("participantEmail"))

        // Role
        Text(
            text = selectedTravel.allParticipants[Participant(participant.key)]!!.name,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.titleSmall,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(bottom = 4.dp) // Add space between text elements
                    .testTag("participantRole"))
      }
}
