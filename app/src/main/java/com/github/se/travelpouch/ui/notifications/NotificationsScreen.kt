package com.github.se.travelpouch.ui.notifications

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotificationsScreen(
    navigationActions: NavigationActions,
    notificationViewModel: NotificationViewModel,
    profileModelView: ProfileModelView,
    listTravelViewModel: ListTravelViewModel,
    activityViewModel: ActivityViewModel,
    documentViewModel: DocumentViewModel,
    eventsViewModel: EventViewModel
) {
  // Collect the profile state
  val profile = profileModelView.profile.collectAsState()

  // Only trigger `getProfile()` if the fsUid changes
  LaunchedEffect(profile.value.fsUid) { profileModelView.getProfile() }

  // Load notifications when the profile is available
  LaunchedEffect(profile.value.fsUid) {
    if (profile.value.fsUid.isNotEmpty()) {
      notificationViewModel.loadNotificationsForUser(profile.value.fsUid)
    }
  }

  notificationViewModel.loadNotificationsForUser(profile.value.fsUid)
  val notifications by notificationViewModel.notifications.collectAsState()

  val categorizedNotifications = categorizeNotifications(notifications)
  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag("ScalNotificationsScreen"),
      topBar = {
        TopAppBar(
            title = {
              Text(text = "Notifications", modifier = Modifier.testTag("TitleNotificationsScreen"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.TRAVEL_LIST) },
                  modifier = Modifier.testTag("goBackButton") // Tag for back button
                  ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            },
            actions = {
              Button(
                  onClick = {
                    notificationViewModel.deleteAllNotificationsForUser(
                        profile.value.fsUid,
                        onSuccess = {
                          Toast.makeText(context, "All notifications deleted", Toast.LENGTH_SHORT)
                              .show()
                          notificationViewModel.loadNotificationsForUser(profile.value.fsUid)
                        },
                        onFailure = { e ->
                          Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        })
                  },
                  modifier = Modifier.testTag("DeleteAllNotificationsButton"),
              ) {
                Text("Delete All")
              }
            },
        )
      },
      content = { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .testTag("LazyColumnNotificationsScreen")) {
              categorizedNotifications.forEach { (category, notifications) ->
                item {
                  Text(
                      text = category,
                      style = MaterialTheme.typography.titleLarge,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(8.dp).testTag("CategoryTitle"))
                }
                itemsIndexed(notifications) { _, notification ->
                  notificationViewModel.markNotificationAsRead(notification.notificationUid)
                  NotificationItem(
                      notification = notification,
                      notificationViewModel = notificationViewModel,
                      profileViewModel = profileModelView,
                      listTravelViewModel = listTravelViewModel,
                      navigationActions = navigationActions,
                      activityViewModel = activityViewModel,
                      documentViewModel = documentViewModel,
                      eventsViewModel = eventsViewModel)
                }
              }
            }
      })
}

fun categorizeNotifications(notifications: List<Notification>): Map<String, List<Notification>> {
  val thisWeek = mutableListOf<Notification>()
  val lastWeek = mutableListOf<Notification>()
  val lastMonth = mutableListOf<Notification>()
  val lastYear = mutableListOf<Notification>()

  val now = Calendar.getInstance()

  notifications.forEach { notification ->
    val notificationTime = notification.timestamp.toDate()

    when {
      isThisWeek(notificationTime, now) -> thisWeek.add(notification)
      isLastWeek(notificationTime, now) -> lastWeek.add(notification)
      isLastMonth(notificationTime, now) -> lastMonth.add(notification)
      isLastYear(notificationTime, now) -> lastYear.add(notification)
    }
  }

  return mapOf(
      "This week" to thisWeek,
      "Last week" to lastWeek,
      "Last month" to lastMonth,
      "Last year" to lastYear)
}

fun isThisWeek(date: Date, now: Calendar): Boolean {
  val calendar = Calendar.getInstance().apply { time = date }
  return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
      calendar.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)
}

fun isLastWeek(date: Date, now: Calendar): Boolean {
  val calendar = Calendar.getInstance().apply { time = date }
  val diff = now.get(Calendar.WEEK_OF_YEAR) - calendar.get(Calendar.WEEK_OF_YEAR)
  return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) && diff == 1
}

fun isLastMonth(date: Date, now: Calendar): Boolean {
  val calendar = Calendar.getInstance().apply { time = date }
  val diff = now.get(Calendar.MONTH) - calendar.get(Calendar.MONTH)
  return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) && diff == 1
}

fun isLastYear(date: Date, now: Calendar): Boolean {
  val calendar = Calendar.getInstance().apply { time = date }
  return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) - 1
}
