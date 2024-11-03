package com.github.se.travelpouch.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Composable function for the bottom navigation menu.
 *
 * @param tabList List of tabs to display.
 * @param navigationActions Actions for navigation.
 */
@Composable
fun BottomNavigationMenu(tabList: List<TopLevelDestination>, navigationActions: NavigationActions) {
  NavigationBar(modifier = Modifier.testTag("navigationBarTravelList")) {
    tabList.forEach { destination ->
      NavigationBarItem(
          onClick = {
            when (destination.textId) {
              "Travels" -> navigationActions.navigateTo(Screen.TRAVEL_LIST)
              "Activities" -> navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES)
              "Map" -> navigationActions.navigateTo(Screen.ACTIVITIES_MAP)
              "Calendar" -> navigationActions.navigateTo(Screen.CALENDAR)
            }
          },
          icon = { Icon(destination.icon, contentDescription = null) },
          selected = false,
          label = { Text(destination.textId) },
          modifier = Modifier.testTag(destination.textId))
    }
  }
}
