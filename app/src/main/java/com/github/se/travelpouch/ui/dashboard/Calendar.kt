package com.github.se.travelpouch.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Composable function to display the Calendar screen.
 *
 * @param calendarViewModel The ViewModel associated with the Calendar screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel,
) {
  // Observe the state of activities from the ViewModel
  val calendarState by calendarViewModel.calendarState.collectAsState(initial = emptyList())

  // Initial Setup
  LaunchedEffect(Unit) {
    // Set the default selected date to today
    val today = Calendar.getInstance().time
    calendarViewModel.onDateSelected(today)

    calendarViewModel.activityViewModel.getAllActivities()
  }

  // Application
  Scaffold(
      topBar = {
        // TopAppBar with title and icon to indicate the calendar feature
        TopAppBar(
            title = { Text("Calendar") },
            navigationIcon = {
              IconButton(
                  modifier = Modifier.testTag("goBackIcon"),
                  onClick = { /* TODO: Implement go back navigation logic */}) {
                    // Back icon for navigation
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE), titleContentColor = Color.White),
            modifier = Modifier.testTag("calendarTopAppBar"))
      }) { innerPadding ->
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

/**
 * Composable function to display an Android CalendarView.
 *
 * @param selectedDate The currently selected date.
 * @param onDateSelected Callback function to handle date selection.
 * @param modifier Modifier to be applied to the CalendarView.
 */
@Composable
fun CalendarView(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  AndroidView(
      factory = {
        android.widget.CalendarView(context).apply {
          // Set the initial selected date
          date = selectedDate.time

          setOnDateChangeListener { _, year, month, dayOfMonth ->
            val newDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }.time
            onDateSelected(newDate)
          }
        }
      },
      modifier = modifier.testTag("androidCalendarView"),
      update = { it.date = selectedDate.time })
}

/**
 * Composable function to display an activity row.
 *
 * @param activity The activity to be displayed.
 */
@Composable
fun ActivityRow(activity: Activity) {
  // Card to visually separate each activity
  Card(
      modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("activityCard"),
      elevation = CardDefaults.elevatedCardElevation(2.dp),
  ) {
    // Column to arrange activity details vertically
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("activityRow")) {
      val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
      val activityTime = timeFormat.format(activity.date.toDate())

      Text(
          text =
              "Title: ${activity.title}\nTime: $activityTime\nDescription: ${activity.description}",
          modifier =
              Modifier.padding(bottom = 3.dp)
                  .testTag(
                      "activityDetails")) // Display the activity details as a single text block
    }
  }
}
