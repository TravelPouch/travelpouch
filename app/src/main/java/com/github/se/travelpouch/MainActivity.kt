package com.github.se.travelpouch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.TravelRepositoryFirestore
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Route
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.ui.overview.AddTravelScreen
import com.github.se.travelpouch.ui.theme.SampleAppTheme
import com.github.se.travelpouch.ui.travel.EditTravelSettingsScreen
import com.github.se.travelpouch.ui.travel.ParticipantListScreen
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SampleAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().testTag("MainScreenContainer"),
        ) {
          TravelPouchApp()
        }
      }
    }
  }
}

@Composable
fun TravelPouchApp() {
  val db = FirebaseFirestore.getInstance()
  val travelRepository = TravelRepositoryFirestore(db)
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val listTravelViewModel: ListTravelViewModel = viewModel(factory = ListTravelViewModel.Factory)

  val location = Location(12.34, 56.78, Timestamp(1234567890L, 0), "Test Location")
  val attachments: MutableMap<String, String> = HashMap()
  attachments["Attachment1"] = "UID1"
  val user1ID = "rythwEmprFhOOgsANXnv"
  val user2ID = "sigmasigmasigmasigma"
  val participants: MutableMap<Participant, Role> = HashMap()
  participants[Participant(user1ID)] = Role.OWNER
  participants[Participant(user2ID)] = Role.PARTICIPANT
  val travelContainer =
      TravelContainer(
          "DFZft6Z95ABnRQ3YZQ2d",
          "Test Title",
          "Test Description",
          Timestamp(1234567890L - 1, 0),
          Timestamp(1234567890L, 0),
          location,
          attachments,
          participants)
  val sigma by listTravelViewModel.travels.collectAsState()
  println("${sigma.size}")
  listTravelViewModel.selectTravel(travelContainer)

  NavHost(navController = navController, startDestination = Route.OVERVIEW) {
    navigation(
        startDestination = Screen.EDIT,
        route = Route.OVERVIEW,
    ) {
      composable(Screen.ADD_TRAVEL) { AddTravelScreen(listTravelViewModel, navigationActions) }
      composable(Screen.EDIT) { EditTravelSettingsScreen(listTravelViewModel, navigationActions) }
      composable(Screen.PARTICIPANT_LIST) {
        ParticipantListScreen(listTravelViewModel, navigationActions)
      }
    }
  }
}
