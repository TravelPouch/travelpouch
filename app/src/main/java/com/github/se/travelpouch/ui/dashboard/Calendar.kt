package com.github.se.travelpouch.ui.dashboard

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel,
) {
  // Set the default selected date to today
  LaunchedEffect(Unit) {
    val today = Calendar.getInstance().time
    calendarViewModel.onDateSelected(today)
    calendarViewModel.getActivitiesForMonth(today) // Fetch activities for the current month
  }

  // Observe the state of activities from the ViewModel
  val calendarState by calendarViewModel.calendarState.observeAsState(initial = emptyList())

  Scaffold(
      topBar = {
        // TopAppBar with title and icon to indicate the calendar feature
        TopAppBar(
            title = { Text("Calendar") },
            navigationIcon = {
              Icon(Icons.Filled.Event, contentDescription = null) // Calendar icon for navigation
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE), // Purple color for the AppBar
                    titleContentColor =
                        Color.White // White color for the text and icons in the AppBar
                    ))
      }) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize() // Fill the entire screen
                    .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              // CalendarView composable to wrap Android's CalendarView
              CalendarView(
                  selectedDate = calendarViewModel.selectedDate,
                  onDateSelected = { date ->
                    calendarViewModel.onDateSelected(
                        date) // Notify ViewModel when a new date is selected
                    calendarViewModel.getActivitiesForDate(
                        date) // Fetch activities for the selected date
                  },
                  modifier =
                      Modifier.fillMaxWidth() // Make the calendar view fill the width of the screen
                  )

              // LazyColumn to display the list of activities for the selected date
              LazyColumn(
                  modifier = Modifier.fillMaxSize() // Fill available space
                  ) {
                    // Display each activity as a row in the list
                    items(calendarState) { activity ->
                      ActivityRow(activity = activity) // Composable to render each activity item
                    }
                  }
            }
      }
}

@Composable
fun CalendarView(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
  // Wrap Android's CalendarView inside a Compose Composable
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
      modifier = modifier,
      update = { it.date = selectedDate.time })
}

@Composable
fun ActivityRow(activity: Activity) {
  // Card to visually separate each activity
  Card(
      modifier =
          Modifier.fillMaxWidth() // Fill the width of the screen
              .padding(8.dp), // Add padding around the card
      elevation = CardDefaults.cardElevation(2.dp) // Correct elevation parameter
      ) {
        // Row to arrange activity details horizontally
        Row(
            modifier =
                Modifier.fillMaxWidth() // Fill the width of the card
                    .padding(16.dp), // Add padding inside the card
            horizontalArrangement = Arrangement.SpaceBetween // Space out the elements in the row
            ) {
              Text(activity.title) // Display the title of the activity
              Text(activity.time) // Display the time of the activity
        }
      }
}

// ViewModel changes to match Firestore logic in TravelListScreen
class CalendarViewModel(val activityViewModel: ActivityViewModel) : ViewModel() {
  private val db = FirebaseFirestore.getInstance()
  private val _calendarState = MutableLiveData<List<Activity>>()
  val calendarState: LiveData<List<Activity>> = _calendarState
  var selectedDate: Date = Calendar.getInstance().time

  init {
    getAllActivities()
    // Populate with mock data for debugging
    populateMockActivities()
  }
  // Factory to create CalendarViewModel with an ActivityViewModel
  companion object {
    fun Factory(activityViewModel: ActivityViewModel): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(activityViewModel) as T
          }
        }
  }

  // Fetch activities for the current date
  fun getActivitiesForDate(date: Date) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formattedDate = dateFormat.format(date)
    Log.d("CalendarViewModel", "Fetching activities for date: $formattedDate")
    db.collection("activities")
        .whereEqualTo("date", formattedDate)
        .get()
        .addOnSuccessListener { result ->
          _calendarState.value = result.toObjects(Activity::class.java)
        }
        .addOnFailureListener { exception ->
          Log.e("CalendarViewModel", "Error fetching activities", exception)
        }
  }

  // Fetch activities for the current month
  fun getActivitiesForMonth(date: Date) {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)
    Log.d("CalendarViewModel", "Fetching activities for month: $month, year: $year")

    db.collection("activities")
        .whereEqualTo("month", month)
        .whereEqualTo("year", year)
        .get()
        .addOnSuccessListener { result ->
          _calendarState.value = result.toObjects(Activity::class.java)
        }
        .addOnFailureListener { exception ->
          Log.e("CalendarViewModel", "Error fetching activities", exception)
        }
  }
  // 111006, 125729, 144248, 144447
  // Update selected date
  fun onDateSelected(date: Date) {
    selectedDate = date
  }

  // Use ActivityViewModel to get all activities
  fun getAllActivities() {
    activityViewModel.getAllActivities()
  }

  // Populate with mock activities for debugging
  private fun populateMockActivities() {
    val mockActivities =
        listOf(
            Activity(title = "Meeting", time = "10:00 AM", date = "2024-10-30"),
            Activity(title = "Lunch", time = "12:00 PM", date = "2024-10-31"),
            Activity(title = "Workout", time = "6:00 PM", date = "2024-10-31"))
    _calendarState.value = mockActivities
  }
}

// Data class for Activity
data class Activity(val title: String = "", val time: String = "", val date: String = "")
