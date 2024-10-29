package com.github.se.travelpouch.ui.navigation

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
  const val TIMELINE = "Timeline Screen"
  const val PROFILE = "Profile Screen"
  const val EDIT_PROFILE = "Edit Profile Screen"
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
