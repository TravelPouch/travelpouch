package com.github.se.travelpouch.ui.travel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.fsUid
import com.github.se.travelpouch.model.profile.Profile

@Composable
fun ParticipantRow(
    participant: Map.Entry<fsUid, Profile>,
    selectedTravel: TravelContainer,
    onClick: () -> Unit
) {
  val context = LocalContext.current

  Row(
      modifier = Modifier.fillMaxWidth().testTag("participantRow").clickable { onClick() },
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Localized description",
            modifier = Modifier.padding(10.dp).testTag("participantIcon"))
        Text(
            text = participant.value.name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(10.dp).testTag("participantName"))
        Text(
            text = participant.value.email,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(10.dp).testTag("participantEmail"))
        Text(
            text = selectedTravel.allParticipants[Participant(participant.key)]!!.name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(10.dp).testTag("participantRole"))
      }
}
