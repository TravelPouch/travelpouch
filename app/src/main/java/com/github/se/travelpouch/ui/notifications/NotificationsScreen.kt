package com.github.se.travelpouch.ui.notifications

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.navigation.BottomNavigationMenu
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.TopLevelDestinations
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
  profileModelView.getProfile()

  val profile = profileModelView.profile.collectAsState()
  notificationViewModel.loadNotificationsForUser(profile.value.fsUid)
  val notifications by notificationViewModel.notifications.collectAsState()

  val categorizedNotifications = categorizeNotifications(notifications)
  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag("ScalNotificationsScreen"),
      topBar = {
        TopAppBar(
            modifier = Modifier.padding(8.dp).testTag("TopAppBarNotificationsScreen"),
            title = {
              Text(
                  text = "Notifications",
                  fontSize = 32.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                  modifier = Modifier.fillMaxWidth().testTag("TitleNotificationsScreen"))
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
                      fontSize = 20.sp,
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
      },
      bottomBar = {
        BottomNavigationMenu(
            navigationActions = navigationActions,
            tabList =
                listOf(
                    TopLevelDestinations.NOTIFICATION,
                    TopLevelDestinations.TRAVELS,
                    TopLevelDestinations.CALENDAR))
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
