package com.github.se.travelpouch.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

object Route {
  const val DEFAULT = "Default"
}

object Screen {
  const val AUTH = "Auth Screen"
  const val TRAVEL_LIST = "TravelList Screen"
  const val EDIT_TRAVEL_SETTINGS = "Edit Screen"
  const val PARTICIPANT_LIST = "Participant List Screen"
  const val ADD_TRAVEL = "AddTravel Screen"
  const val DOCUMENT_LIST = "DocumentList Screen"
  const val DOCUMENT_PREVIEW = "DocumentPreview Screen"
  const val TRAVEL_ACTIVITIES = "TravelActivities Screen"
  const val ADD_ACTIVITY = "AddActivity Screen"
  const val EDIT_ACTIVITY = "Edit Activity Screen"
  const val TIMELINE = "Timeline Screen"

  const val PROFILE = "Profile Screen"
  const val EDIT_PROFILE = "Edit Profile Screen"

  const val ACTIVITIES_MAP = "MapActivities Screen"

  const val CALENDAR = "Calendar Screen"
}

data class TopLevelDestination(val screen: String, val icon: ImageVector, val textId: String)

object TopLevelDestinations {
  val TRAVELS = TopLevelDestination(Screen.TRAVEL_LIST, Icons.Default.CardTravel, "Travels")
  val ACTIVITIES = TopLevelDestination(Screen.TRAVEL_ACTIVITIES, Icons.Default.Home, "Activities")
  val MAP = TopLevelDestination(Screen.ACTIVITIES_MAP, Icons.Default.Place, "Map")
  val CALENDAR = TopLevelDestination(Screen.CALENDAR, Icons.Default.DateRange, "Calendar")
}

open class NavigationActions(
    private val navController: NavHostController,
) {

  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: String) {
    navController.navigate(screen)
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }
}
