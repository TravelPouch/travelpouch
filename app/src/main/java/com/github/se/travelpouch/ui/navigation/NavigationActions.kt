package com.github.se.travelpouch.ui.navigation

import androidx.navigation.NavHostController

object Route {
  const val AUTH = "Auth"
  const val TRAVEL = "Edit"
  const val PARTICIPANT_LIST = "Participant List"
  const val OVERVIEW = "Overview"
}

object Screen {
  const val AUTH = "Auth Screen"
  const val EDIT = "Edit Screen"
  const val PARTICIPANT_LIST = "Participant List Screen"
  const val ADD_TRAVEL = "AddTravel Screen"
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
