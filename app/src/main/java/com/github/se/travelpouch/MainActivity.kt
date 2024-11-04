package com.github.se.travelpouch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.ui.authentication.SignInScreen
import com.github.se.travelpouch.ui.dashboard.AddActivityScreen
import com.github.se.travelpouch.ui.dashboard.CalendarScreen
import com.github.se.travelpouch.ui.dashboard.EditActivity
import com.github.se.travelpouch.ui.dashboard.TimelineScreen
import com.github.se.travelpouch.ui.dashboard.TravelActivitiesScreen
import com.github.se.travelpouch.ui.dashboard.map.ActivitiesMapScreen
import com.github.se.travelpouch.ui.documents.DocumentListScreen
import com.github.se.travelpouch.ui.documents.DocumentPreview
import com.github.se.travelpouch.ui.home.AddTravelScreen
import com.github.se.travelpouch.ui.home.TravelListScreen
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Route
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.ui.profile.ModifyingProfileScreen
import com.github.se.travelpouch.ui.profile.ProfileScreen
import com.github.se.travelpouch.ui.theme.SampleAppTheme
import com.github.se.travelpouch.ui.travel.EditTravelSettingsScreen
import com.github.se.travelpouch.ui.travel.ParticipantListScreen

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
  val context = LocalContext.current
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val listTravelViewModel: ListTravelViewModel = viewModel(factory = ListTravelViewModel.Factory)
  val documentViewModel: DocumentViewModel =
      viewModel(factory = DocumentViewModel.Factory(context.contentResolver))
  val activityModelView: ActivityViewModel = viewModel(factory = ActivityViewModel.Factory)
  val eventsViewModel: EventViewModel = viewModel(factory = EventViewModel.Factory)
  val profileModelView: ProfileModelView = viewModel(factory = ProfileModelView.Factory)

  val calendarViewModel: CalendarViewModel =
      viewModel(factory = CalendarViewModel.Factory(activityModelView))

  NavHost(navController = navController, startDestination = Route.DEFAULT) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.DEFAULT,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions) }

      composable(Screen.TRAVEL_LIST) { TravelListScreen(navigationActions, listTravelViewModel) }
      composable(Screen.TRAVEL_ACTIVITIES) {
        TravelActivitiesScreen(navigationActions, activityModelView)
      }
      composable(Screen.ADD_ACTIVITY) { AddActivityScreen(navigationActions, activityModelView) }
      composable(Screen.EDIT_ACTIVITY) { EditActivity(navigationActions, activityModelView) }
      composable(Screen.ADD_TRAVEL) {
        AddTravelScreen(listTravelViewModel, navigationActions, profileModelView = profileModelView)
      }
      composable(Screen.EDIT_TRAVEL_SETTINGS) {
        EditTravelSettingsScreen(listTravelViewModel, navigationActions)
      }

      composable(Screen.ACTIVITIES_MAP) {
        ActivitiesMapScreen(activityModelView, navigationActions)
      }

      composable(Screen.PARTICIPANT_LIST) {
        ParticipantListScreen(listTravelViewModel, navigationActions)
      }
      composable(Screen.DOCUMENT_LIST) {
        DocumentListScreen(
            documentViewModel,
            listTravelViewModel,
            navigationActions,
            onNavigateToDocumentPreview = { navigationActions.navigateTo(Screen.DOCUMENT_PREVIEW) })
      }
      composable(Screen.DOCUMENT_PREVIEW) { DocumentPreview(documentViewModel, navigationActions) }
      composable(Screen.TIMELINE) { TimelineScreen(eventsViewModel) }

      composable(Screen.PROFILE) { ProfileScreen(navigationActions, profileModelView) }
      composable(Screen.EDIT_PROFILE) {
        ModifyingProfileScreen(navigationActions, profileModelView)
      }

      composable(Screen.CALENDAR) { CalendarScreen(calendarViewModel, navigationActions) }
    }
  }
}
