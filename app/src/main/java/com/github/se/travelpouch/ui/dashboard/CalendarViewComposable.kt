package com.github.se.travelpouch.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Custom Composable for displaying a calendar grid with event indicators.
 *
 * @param selectedDate The currently selected date.
 * @param events A list of dates with events.
 * @param onDateSelected Callback when a date is selected.
 * @param modifier Modifier to be applied to the CalendarView.
 */
@Composable
fun CustomCalendarView(
    selectedDate: Date,
    events: List<Date>,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
  val calendar = Calendar.getInstance()
  calendar.time = selectedDate

  val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
  calendar.set(Calendar.DAY_OF_MONTH, 1)
  val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // Adjust to 0-index

  val days =
      (1..daysInMonth).map { day ->
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.time
      }

  val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  val eventDates = events.map { dateFormat.format(it) }.toSet()

  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .background(
                  MaterialTheme.colorScheme
                      .background) // Set a distinct background color for the Days of the week
              .padding(4.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Days of the week titles
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
          listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false))
          }
        }
      }

  LazyVerticalGrid(
      columns = GridCells.Fixed(7),
      modifier =
          modifier
              .fillMaxWidth()
              .testTag("customCalendarGrid")
              .background(MaterialTheme.colorScheme.background),
      horizontalArrangement = Arrangement.Center) {
        // Add empty spaces for days before the start of the month
        items(firstDayOfWeek) { Spacer(modifier = Modifier.size(40.dp)) }

        // Add days with optional event indicators
        items(days.size) { index ->
          val date = days[index]
          val isEventDay = eventDates.contains(dateFormat.format(date))
          val isSelected = dateFormat.format(selectedDate) == dateFormat.format(date)

          Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.size(40.dp).clickable { onDateSelected(date) }) {
                if (isSelected) {
                  Box(
                      modifier =
                          Modifier.size(36.dp)
                              .background(MaterialTheme.colorScheme.primary, shape = CircleShape))
                }

                BasicText(
                    text = (index + 1).toString(),
                    style =
                        androidx.compose.ui.text.TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            color =
                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onBackground),
                )

                if (isEventDay) {
                  Box(
                      modifier =
                          Modifier.size(8.dp)
                              .align(Alignment.BottomCenter)
                              .background(Color.Red, shape = CircleShape))
                }
              }
        }
      }
}

/**
 * Wrapper Composable for Calendar with Back and Forward navigation arrows.
 *
 * @param selectedDate The currently selected date.
 * @param events A list of dates with events.
 * @param onDateSelected Callback when a date is selected.
 * @param modifier Modifier to be applied to the calendar.
 */
@Composable
fun CalendarView(
    selectedDate: Date,
    events: List<Date>,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
  var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
  currentMonth.time = selectedDate

  Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    // Header with navigation arrows
    Row(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          // Previous month arrow
          IconButton(
              onClick = {
                currentMonth.add(Calendar.MONTH, -1)
                onDateSelected(currentMonth.time)
              }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month")
              }

          // Month and year text
          Text(
              text =
                  "${
                    currentMonth.getDisplayName(
                        Calendar.MONTH, Calendar.LONG, Locale.getDefault()
                    )
                } ${currentMonth.get(Calendar.YEAR)}",
              style = MaterialTheme.typography.bodyLarge)

          // Next month arrow
          IconButton(
              onClick = {
                currentMonth.add(Calendar.MONTH, 1)
                onDateSelected(currentMonth.time)
              }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month")
              }
        }

    // Calendar grid
    CustomCalendarView(
        selectedDate = currentMonth.time,
        events =
            events.filter {
              val eventCalendar = Calendar.getInstance().apply { time = it }
              eventCalendar.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                  eventCalendar.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
            },
        onDateSelected = { date ->
          onDateSelected(date)
          currentMonth.time = date // Update month when date selected
        },
        modifier = Modifier.fillMaxWidth())
  }
}
