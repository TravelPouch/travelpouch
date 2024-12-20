// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.dashboard.CalendarScreen
import com.github.se.travelpouch.ui.dashboard.TravelActivitiesScreen
import com.github.se.travelpouch.ui.dashboard.map.ActivitiesMapScreen
import com.github.se.travelpouch.ui.documents.DocumentListScreen

// credit to : https://www.youtube.com/watch?v=inrGyNJbZaI&t=620s

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipePager(
    navigationActions: NavigationActions,
    activityViewModel: ActivityViewModel,
    calendarViewModel: CalendarViewModel,
    documentViewModel: DocumentViewModel,
    listTravelViewModel: ListTravelViewModel,
    onNavigateToDocumentPreview: () -> Unit
) {

  val listOfTopLevelDestination =
      listOf(
          TopLevelDestinations.ACTIVITIES,
          TopLevelDestinations.CALENDAR,
          TopLevelDestinations.DOCUMENTS,
          TopLevelDestinations.MAP)

  val pagerState = rememberPagerState(initialPage = 0) { listOfTopLevelDestination.size }

  var selectedScreen by remember { mutableIntStateOf(pagerState.currentPage) }

  LaunchedEffect(selectedScreen) { pagerState.scrollToPage(selectedScreen) }

  LaunchedEffect(pagerState.currentPage) { selectedScreen = pagerState.currentPage }

  Scaffold(
      modifier = Modifier.testTag("pagerSwipe"),
      topBar = {
        TopAppBar(
            modifier = Modifier.testTag("topBar"),
            title = {
              Text(
                  listTravelViewModel.selectedTravel.value?.title ?: "",
                  Modifier.testTag("travelTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.TRAVEL_LIST) },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            },
            actions = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.EDIT_TRAVEL_SETTINGS) },
                  modifier = Modifier.testTag("settingsButton")) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                  }

              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.TIMELINE) },
                  modifier = Modifier.testTag("eventTimelineButton")) {
                    Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null)
                  }
            })
      },
      bottomBar = {
        NavigationBar(modifier = Modifier.testTag("navigationBarTravelList")) {
          listOfTopLevelDestination.forEachIndexed { i, destination ->
            NavigationBarItem(
                onClick = {
                  when (destination.textId) {
                    "Activities" -> selectedScreen = 0
                    "Calendar" -> selectedScreen = 1
                    "Documents" -> selectedScreen = 2
                    "Map" -> selectedScreen = 3
                  }
                },
                icon = { Icon(destination.icon, contentDescription = null) },
                selected = i == selectedScreen,
                label = { Text(destination.textId) },
                modifier = Modifier.testTag(destination.textId))
          }
        }
      }) { pd ->
        HorizontalPager(state = pagerState, modifier = Modifier.padding(pd), userScrollEnabled = selectedScreen != 3) { pageIndex ->
          when (pageIndex) {
            0 -> TravelActivitiesScreen(navigationActions, activityViewModel, documentViewModel)
            1 -> CalendarScreen(calendarViewModel, navigationActions)
            2 ->
                DocumentListScreen(
                    documentViewModel,
                    listTravelViewModel,
                    navigationActions,
                    onNavigateToDocumentPreview)
            3 -> ActivitiesMapScreen(activityViewModel, navigationActions)
          }
        }
      }
}
