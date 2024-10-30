package com.github.se.travelpouch.model.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Calendar
import java.util.Date

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
