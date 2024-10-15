package com.github.se.travelpouch.ui.travel

import TruncatedText
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.UserInfo
import com.github.se.travelpouch.model.fsUid
import com.github.se.travelpouch.ui.navigation.NavigationActions

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantListScreen(
    listTravelViewModel: ListTravelViewModel,
    navigationActions: NavigationActions,
) {
  val selectedTravel by listTravelViewModel.selectedTravel.collectAsState()
  val participants by listTravelViewModel.participants.collectAsState()

  val (expanded, setExpanded) = remember { mutableStateOf(false) }
  val (expandedRoleDialog, setExpandedRoleDialog) = remember { mutableStateOf(false) }
  val (selectedParticipant, setSelectedParticipant) =
      remember { mutableStateOf<Map.Entry<fsUid, UserInfo>?>(null) }

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
      LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxWidth()) {
        participants.entries.forEach { participantEntry ->
          item {
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
          modifier = Modifier.padding(15.dp).testTag("noTravelSelected"))
    }

    selectedParticipant?.let { participant ->
      if (expanded) {
        Dialog(onDismissRequest = { setExpanded(false) }) {
          Box(Modifier.size(2000.dp, 200.dp).background(Color.White)) {
            Column(modifier = Modifier.padding(8.dp)) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Localized description",
                        modifier = Modifier.padding(5.dp))
                    TruncatedText(
                        text = participant.value.name,
                        fontWeight = FontWeight.Bold,
                        maxLength = 20,
                        modifier = Modifier.padding(5.dp))
                    TruncatedText(
                        text = participant.value.email,
                        maxLength = 20,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(5.dp))
                  }

              RoleEntryDialog(
                  selectedTravel = selectedTravel,
                  participant = participant,
                  changeRoleAction = setExpandedRoleDialog,
                  removeParticipantAction = {setExpanded(false)})
            }
          }
        }
      }

      if (expandedRoleDialog) {
        Dialog(onDismissRequest = { setExpandedRoleDialog(false) }) {
          Box(Modifier.size(800.dp, 250.dp).background(Color.White)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                  ChangeRoleDialog(
                      selectedTravel,
                      participant,
                      { newRole ->
                        setExpandedRoleDialog(false)
                        setExpanded(false)
                      })
                }
          }
        }
      }
    }
  }
}
