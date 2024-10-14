package com.github.se.travelpouch.ui.travel

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.ui.navigation.NavigationActions

// parts of this file was generated using Github Copilot
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantListScreen(
    listTravelViewModel: ListTravelViewModel,
    navigationActions: NavigationActions,
) {
  val selectedTravel by listTravelViewModel.selectedTravel.collectAsState()
  val participants by listTravelViewModel.participants.collectAsState()

  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag("listScreen"),
      topBar = {
        MediumTopAppBar(
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            title = {
              Text("Participant Settings", modifier = Modifier.testTag("participantSettingText"))
            },
            navigationIcon = {
              Button(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Localized description")
                  }
            })
      },
  ) { paddingValues ->
    if (selectedTravel != null) {

      LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
        participants.entries.forEach { participant ->
          item {
            Row(
                modifier =
                    Modifier.fillMaxWidth().clickable {
                      Toast.makeText(context, "Participant clicked", Toast.LENGTH_SHORT).show()
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Default.Person,
                      contentDescription = "Localized description",
                      modifier = Modifier.padding(10.dp))
                  Text(
                      text = participant.value.name,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(10.dp))
                  Text(
                      text = participant.value.email,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(10.dp))
                  Text(
                      text = selectedTravel!!.allParticipants[Participant(participant.key)]!!.name,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(10.dp))
                }
            HorizontalDivider(thickness = 0.5.dp, color = Color.Gray)
          }
        }
      }
    } else {
      Text(
          "No Travel is selected or the selected travel no longer exists.",
          modifier = Modifier.padding(15.dp).testTag("noTravelSelected"))
    }
  }
}
