package com.github.se.travelpouch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.activity.map.DirectionsViewModel
import com.github.se.travelpouch.model.authentication.AuthenticationService
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.home.StorageDashboardViewModel
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.authentication.SignInScreen
import com.github.se.travelpouch.ui.authentication.SignInWithPassword
import com.github.se.travelpouch.ui.dashboard.AddActivityScreen
import com.github.se.travelpouch.ui.dashboard.EditActivity
import com.github.se.travelpouch.ui.dashboard.TimelineScreen
import com.github.se.travelpouch.ui.dashboard.map.ActivitiesMapScreen
import com.github.se.travelpouch.ui.documents.DocumentListScreen
import com.github.se.travelpouch.ui.documents.DocumentPreview
import com.github.se.travelpouch.ui.home.AddTravelScreen
import com.github.se.travelpouch.ui.home.StorageDashboard
import com.github.se.travelpouch.ui.home.OnboardingScreen
import com.github.se.travelpouch.ui.home.TravelListScreen
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Route
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.ui.navigation.SwipePager
import com.github.se.travelpouch.ui.notifications.NotificationsScreen
import com.github.se.travelpouch.ui.profile.ModifyingProfileScreen
import com.github.se.travelpouch.ui.profile.ProfileScreen
import com.github.se.travelpouch.ui.theme.SampleAppTheme
import com.github.se.travelpouch.ui.travel.EditTravelSettingsScreen
import com.github.se.travelpouch.ui.travel.ParticipantListScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject lateinit var auth: AuthenticationService

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SampleAppTheme(dynamicColor = false) {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().testTag("MainScreenContainer"),
            // color = MaterialTheme.colorScheme.background,
        ) {
          TravelPouchApp()
        }
      }
    }
  }

  @Composable
  fun TravelPouchApp() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    val listTravelViewModel = hiltViewModel<ListTravelViewModel>()
    val documentViewModel: DocumentViewModel = hiltViewModel<DocumentViewModel>()
    val activityModelView: ActivityViewModel = hiltViewModel<ActivityViewModel>()
    val eventsViewModel: EventViewModel = hiltViewModel<EventViewModel>()
    val profileModelView = hiltViewModel<ProfileModelView>()
    val notificationViewModel: NotificationViewModel = viewModel<NotificationViewModel>()
    val calendarViewModel: CalendarViewModel =
        viewModel(factory = CalendarViewModel.Factory(activityModelView))
    val locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
    val storageDashboardViewModel = viewModel<StorageDashboardViewModel>()

    val directionsViewModel: DirectionsViewModel =
        viewModel(
            factory =
                DirectionsViewModel.provideFactory(
                    BuildConfig.MAPS_API_KEY) // Inject the API key for the DirectionsViewModel
            )
    NavHost(navController = navController, startDestination = Route.DEFAULT) {
      navigation(
          startDestination = Screen.AUTH,
          route = Route.DEFAULT,
      ) {
        composable(Screen.AUTH) {
          SignInScreen(navigationActions, profileModelView, listTravelViewModel)
        }

        composable(Screen.SWIPER) {
          SwipePager(
              navigationActions,
              activityModelView,
              calendarViewModel,
              documentViewModel,
              listTravelViewModel,
              onNavigateToDocumentPreview = {
                navigationActions.navigateTo(Screen.DOCUMENT_PREVIEW)
              })
        }

        composable(Screen.TRAVEL_LIST) {
          TravelListScreen(
              navigationActions,
              listTravelViewModel,
              activityModelView,
              eventsViewModel,
              documentViewModel,
              profileModelView)
        }

        composable(Screen.ADD_ACTIVITY) { AddActivityScreen(navigationActions, activityModelView) }
        composable(Screen.EDIT_ACTIVITY) { EditActivity(navigationActions, activityModelView) }
        composable(Screen.ADD_TRAVEL) {
          AddTravelScreen(
              listTravelViewModel, navigationActions, profileModelView = profileModelView)
        }
        composable(Screen.EDIT_TRAVEL_SETTINGS) {
          EditTravelSettingsScreen(
              listTravelViewModel,
              navigationActions,
              notificationViewModel,
              profileModelView,
              locationViewModel)
        }

        composable(Screen.ACTIVITIES_MAP) {
          ActivitiesMapScreen(activityModelView, navigationActions, directionsViewModel)
        }

        composable(Screen.PARTICIPANT_LIST) {
          ParticipantListScreen(
              listTravelViewModel, navigationActions, notificationViewModel, profileModelView)
        }
        composable(Screen.DOCUMENT_LIST) {
          DocumentListScreen(
              documentViewModel,
              listTravelViewModel,
              navigationActions,
              onNavigateToDocumentPreview = {
                navigationActions.navigateTo(Screen.DOCUMENT_PREVIEW)
              })
        }
        composable(Screen.DOCUMENT_PREVIEW) {
          DocumentPreview(documentViewModel, navigationActions)
        }
        composable(Screen.TIMELINE) { TimelineScreen(eventsViewModel) }

        composable(Screen.PROFILE) { ProfileScreen(navigationActions, profileModelView) }
        composable(Screen.EDIT_PROFILE) {
          ModifyingProfileScreen(navigationActions, profileModelView, notificationViewModel)
        }

        composable(Screen.SIGN_IN_PASSWORD) {
          SignInWithPassword(navigationActions, profileModelView, listTravelViewModel, auth)
        }

        composable(Screen.NOTIFICATION) {
          NotificationsScreen(
              navigationActions,
              notificationViewModel,
              profileModelView,
              listTravelViewModel,
              activityModelView,
              documentViewModel,
              eventsViewModel)
        }
        
        composable(Screen.STORAGE) { StorageDashboard(storageDashboardViewModel, navigationActions) }
        composable(Screen.ONBOARDING) { OnboardingScreen(navigationActions, profileModelView) }
      }
    }
  }
}
