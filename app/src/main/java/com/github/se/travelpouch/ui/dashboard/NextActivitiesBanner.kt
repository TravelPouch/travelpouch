// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.dashboard

import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    modifier: Modifier = Modifier,
    localConfig: Configuration = LocalConfiguration.current
) {
  // this variable is used to determine the screen orientation
  var isPortrait = localConfig.orientation == ORIENTATION_PORTRAIT
  var screenSize = if (isPortrait) localConfig.screenHeightDp else localConfig.screenWidthDp
  val dismissButtonSize = (screenSize * 0.05).dp // 5% of the screen size
  val showOutline = remember { mutableStateOf(false) }
  val reminderRange = 86400L // 24 hours in milliseconds
  val nowTime = Timestamp.now().seconds
  val darkTheme = isSystemInDarkTheme()
  val overlayColor =
      if (darkTheme) Color.DarkGray.copy(alpha = 0.85f) else Color.Gray.copy(alpha = 0.85f)

  // Filter activities due within the next 24 hours
  val upcomingActivities =
      activities
          .filter { activity ->
            val timeDiff: Long = activity.date.seconds - nowTime
            timeDiff in 0..reminderRange // 0 - 24 hours in milliseconds
          }
          .sortedBy { it.date } // Sort by due time

  if (upcomingActivities.isNotEmpty()) {
    Box(
        modifier =
            modifier
                .heightIn(
                    max =
                        (0.3f * LocalConfiguration.current.screenHeightDp.dp.value)
                            .dp) // 30% of screen height
                .background(overlayColor, RoundedCornerShape(8.dp))
                .clickable { showOutline.value = true } // Set the outline on first tap
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("NextActivitiesBannerBox"),
        contentAlignment = Alignment.Center) {
          Column(
              verticalArrangement =
                  Arrangement.SpaceBetween, // Spread content with space in between
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Upcoming Activities in the next 24 hours",
                    color = if (darkTheme) Color.LightGray else Color.DarkGray,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    modifier =
                        Modifier.padding(bottom = 8.dp, start = 2.dp, end = 2.dp)
                            .testTag("reminderTitle")
                            .background(
                                MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp)))

                // Scrollable list of upcoming activities
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f) // Fill available horizontal space
                    ) {
                      items(upcomingActivities) { activity ->
                        Text(
                            text = "- ${activity.title}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                            modifier =
                                Modifier.padding(vertical = 4.dp)
                                    .fillMaxWidth()
                                    .testTag("reminderEntry"))
                      }
                    }
              }
          // Dismiss button in the bottom right corner
          Box(
              modifier =
                  Modifier.align(if (isPortrait) Alignment.BottomEnd else Alignment.CenterEnd)
                      .padding(8.dp) // Add padding to ensure the border doesn't touch the edges of
              // the box
              ) {
                IconButton(
                    onClick = onDismiss,
                    modifier =
                        Modifier.size(dismissButtonSize)
                            .border(
                                width = if (showOutline.value) 2.dp else 0.dp,
                                color = if (showOutline.value) Color.White else Color.Transparent,
                                shape = CircleShape)
                            .testTag("NextActivitiesBannerDismissButton")
                            .padding(
                                8.dp) // Add padding to ensure the border doesn't touch the edges
                    // of the box
                    ) {
                      Icon(
                          imageVector = Icons.Default.Close,
                          contentDescription = "Dismiss",
                          tint = Color.White)
                    }
              }
        }
  }
}
