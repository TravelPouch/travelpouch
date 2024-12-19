// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
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
              "Map" -> navigationActions.navigateTo(Screen.ACTIVITIES_MAP)
              "Notifications" -> navigationActions.navigateTo(Screen.NOTIFICATION)
              "Travels" -> navigationActions.navigateTo(Screen.TRAVEL_LIST)
            }
          },
          icon = { Icon(destination.icon, contentDescription = null) },
          selected = false,
          label = { Text(destination.textId) },
          modifier = Modifier.testTag(destination.textId))
    }
  }
}
