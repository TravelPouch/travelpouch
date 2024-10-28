package com.github.se.travelpouch.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.activity.Activity
import com.google.firebase.Timestamp

@Composable
fun NextActivitiesBanner(
    activities: List<Activity>,  // Your activity data model
    onDismiss: () -> Unit
) {
    // Filter activities due within the next 24 hours
    val upcomingActivities = activities.filter { activity ->
        val timeDiff:Long = activity.date.seconds - Timestamp.now().seconds
        timeDiff in 0..86400000L // 24 hours in milliseconds
    }

    if (upcomingActivities.isNotEmpty()) {
        Box(
            modifier = Modifier
                .background(Color.Gray.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Display upcoming activities
                Text(
                    text = "Next activities due: ${
                        upcomingActivities.joinToString(", ") { it.title }
                    }"+" in the next 24 hours",
                    color = Color.White
                )
                // Dismiss button
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
