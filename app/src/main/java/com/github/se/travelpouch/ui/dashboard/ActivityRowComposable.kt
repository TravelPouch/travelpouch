// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.activity.Activity
import java.text.SimpleDateFormat
import java.util.Locale

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
