package com.github.se.travelpouch.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.activity.Activity
import com.google.firebase.Timestamp

/**
 * This composable function displays a banner with the next activities due within the next 24 hours.
 * It shows a list of upcoming activities and provides a dismiss button to hide the banner.
 *
 * @param activities List\<Activity\> : The list of activities to be displayed.
 * @param onDismiss () -> Unit : The function to be called when the dismiss button is clicked.
 * @param modifier Modifier : The modifier to be applied to the banner.
 */
@Composable
fun NextActivitiesBanner(
    activities: List<Activity>, // Your activity data model
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
  val showOutline = remember { mutableStateOf(false) }
  // Filter activities due within the next 24 hours
  val reminderRange = 86400L // 2400 hours in milliseconds
  val nowTime = Timestamp.now().seconds
  val upcomingActivities =
      activities.filter { activity ->
        val timeDiff: Long = activity.date.seconds - nowTime
        timeDiff in 0..reminderRange // 24 hours in milliseconds
      }

  if (upcomingActivities.isNotEmpty()) {
    Box(
        modifier =
            modifier
                .background(Color.Gray.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .clickable {
                  // Set the outline on first tap
                  showOutline.value = true
                }
                .padding(16.dp)
                .testTag("NextActivitiesBannerBox"),
        contentAlignment = Alignment.Center) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()) {
                // Display upcoming activities
                Box(modifier = Modifier.weight(1f)) {
                  Text(
                      text =
                          "Next activities due: ${
                            upcomingActivities.joinToString(", ") { it.title }
                        } in the next 24 hours.",
                      color = Color.White,
                      maxLines =
                          3, // Limit max lines if you want to restrict vertical growth as well
                      modifier = Modifier.testTag("NextActivitiesBannerText"))
                }

                // Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier =
                        Modifier.border(
                                width = if (showOutline.value) 2.dp else 0.dp,
                                color = if (showOutline.value) Color.Black else Color.Transparent,
                                shape = CircleShape)
                            .testTag("NextActivitiesBannerDismissButton")) {
                      Icon(
                          imageVector = Icons.Default.Close,
                          contentDescription = "Dismiss",
                          tint = Color.White)
                    }
              }
        }
  } else {
    onDismiss()
  }
}
