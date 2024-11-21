package com.github.se.travelpouch.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable function to display the Calendar screen.
 *
 * @param calendarViewModel The ViewModel associated with the Calendar screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(calendarViewModel: CalendarViewModel, navigationActions: NavigationActions) {
  // Observe the state of activities from the ViewModel
  val calendarState by calendarViewModel.calendarState.collectAsState(initial = emptyList())

  // Initial Setup
  CalendarScreenLaunchedEffect(calendarViewModel = calendarViewModel)

  // Application
  Scaffold(
      modifier = Modifier.testTag("calenmdarScreen")
      //      topBar = {
      //        // TopAppBar with title and icon to indicate the calendar feature
      //        TopAppBar(
      //            title = { Text("Calendar") },
      //            navigationIcon = {
      //              IconButton(
      //                  modifier = Modifier.testTag("goBackIcon"),
      //                  onClick = { navigationActions.goBack() }) {
      //                    // Back icon for navigation
      //                    Icon(
      //                        Icons.AutoMirrored.Filled.ArrowBack,
      //                        contentDescription = null,
      //                    )
      //                  }
      //            },
      //            modifier = Modifier.testTag("calendarTopAppBar"))
      //      },
      //      bottomBar = {
      //        BottomNavigationMenu(
      //            tabList =
      //                listOf(
      //                    TopLevelDestinations.ACTIVITIES,
      //                    TopLevelDestinations.CALENDAR,
      //                    TopLevelDestinations.MAP),
      //            navigationActions = navigationActions)
      //      }
      ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).testTag("calendarScreenColumn"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              // CalendarView composable to wrap Android's CalendarView
              CalendarView(
                  selectedDate = calendarViewModel.selectedDate,
                  onDateSelected = { date -> calendarViewModel.onDateSelected(date) },
                  modifier = Modifier.fillMaxWidth().testTag("androidCalendarView"))

              // Activities list
              LazyColumn(modifier = Modifier.fillMaxSize().testTag("activityList")) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val selectedDateFormatted = dateFormat.format(calendarViewModel.selectedDate)
                val selectedDayActivities =
                    calendarState
                        .filter { activity ->
                          val activityDate = activity.date.toDate() // Convert Timestamp to Date
                          val activityDateFormatted = dateFormat.format(activityDate)
                          selectedDateFormatted == activityDateFormatted
                        }
                        .sortedBy { it.date }

                // Display each activity as a row in the list
                items(selectedDayActivities) { activity ->
                  ActivityRow(activity = activity) // Composable to render each activity item
                }
              }
            }
      }
}
