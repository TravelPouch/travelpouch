package com.github.se.travelpouch.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
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
