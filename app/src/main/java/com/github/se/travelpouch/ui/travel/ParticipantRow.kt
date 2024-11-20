package com.github.se.travelpouch.ui.travel

import TruncatedText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.fsUid

@Composable
fun ParticipantRow(
    participant: Map.Entry<fsUid, Profile>,
    selectedTravel: TravelContainer,
    onClick: () -> Unit
) {

  Row(
      modifier = Modifier.fillMaxWidth().testTag("participantRow").clickable { onClick() },
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Localized description",
            modifier = Modifier.padding(5.dp).testTag("participantIcon"))
        Text(
            text = participant.value.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(5.dp).testTag("participantName").weight(0.8f))
        TruncatedText(
            text = participant.value.email,
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.labelSmall,
            maxLength = 25,
            modifier = Modifier.padding(5.dp).testTag("participantEmail").weight(0.8f))
        TruncatedText(
            text = selectedTravel.allParticipants[Participant(participant.key)]!!.name,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            maxLength = 20,
            modifier = Modifier.padding(5.dp).testTag("participantRole").weight(1f))
      }
}
