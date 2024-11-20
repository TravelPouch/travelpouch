package com.github.se.travelpouch.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
import com.github.se.travelpouch.ui.dashboard.CalendarScreen
import com.github.se.travelpouch.ui.dashboard.TravelActivitiesScreen

@Composable
fun SwipePager(
    navigationActions: NavigationActions,
    activityViewModel: ActivityViewModel,
    calendarViewModel: CalendarViewModel
) {
  val listOfTopLevelDestination =
      listOf(TopLevelDestinations.ACTIVITIES, TopLevelDestinations.CALENDAR)
  val pagerState = rememberPagerState(initialPage = 0) { listOfTopLevelDestination.size }

  Column {
    HorizontalPager(state = pagerState) {
      when (it) {
        0 -> TravelActivitiesScreen(navigationActions, activityViewModel)
        1 -> CalendarScreen(calendarViewModel, navigationActions)
      }
    }
  }
}
