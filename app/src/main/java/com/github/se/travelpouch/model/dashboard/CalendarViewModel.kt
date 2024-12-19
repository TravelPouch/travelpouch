// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityViewModel
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the Calendar screen.
 *
 * @param activityViewModel The ViewModel associated with activities.
 */
class CalendarViewModel(val activityViewModel: ActivityViewModel) : ViewModel() {
  val calendarState: StateFlow<List<Activity>> = activityViewModel.activities
  var selectedDate by mutableStateOf(Calendar.getInstance().time)

  companion object {
    /**
     * Factory to create CalendarViewModel with an ActivityViewModel.
     *
     * @param activityViewModel The ViewModel associated with activities.
     * @return A ViewModelProvider.Factory instance.
     */
    fun Factory(activityViewModel: ActivityViewModel): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(activityViewModel) as T
          }
        }
  }

  /**
   * Update selected date.
   *
   * @param date The new selected date.
   */
  fun onDateSelected(date: Date) {
    selectedDate = date
  }
}
