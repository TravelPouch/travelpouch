package com.github.se.travelpouch.model.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.util.Calendar

@Composable
fun CalendarScreenLaunchedEffect(
    calendarViewModel: CalendarViewModel,
) {

  // Initial Setup
  LaunchedEffect(Unit) {
    // Set the default selected date to today
    val today = Calendar.getInstance().time
    calendarViewModel.onDateSelected(today)

    calendarViewModel.activityViewModel.getAllActivities()
  }
}
