package com.github.se.travelpouch.ui.notifications

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationContent
import com.github.se.travelpouch.model.notifications.NotificationType
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.CurrentProfile
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.ui.navigation.BottomNavigationMenu
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
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
    listTravelViewModel: ListTravelViewModel
) {
    profileModelView.getProfile()

    val profile = profileModelView.profile.collectAsState()
    notificationViewModel.loadNotificationsForUser(profile.value.fsUid)
    val notifications by notificationViewModel.notifications.collectAsState()

    val categorizedNotifications = categorizeNotifications(notifications)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                actions = {
                    Button(
                        onClick = {
                            notificationViewModel.deleteAllNotificationsForUser(
                                profile.value.fsUid,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "All notifications deleted",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    notificationViewModel.loadNotificationsForUser(profile.value.fsUid)
                                },
                                onFailure = { e ->
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                        }) {
                        Text("Delete All")
                    }
                },
                modifier = Modifier.padding(8.dp)
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Padding to avoid overlap with top bar
            ) {
                categorizedNotifications.forEach { (category, notifications) ->
                    item {
                        Text(
                            text = category,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    itemsIndexed(notifications) { _, notification ->
                        notificationViewModel.markNotificationAsRead(notification.notificationUid)
                        NotificationItem(
                            notification = notification,
                            notificationViewModel = notificationViewModel,
                            profileViewModel = profileModelView,
                            listTravelViewModel = listTravelViewModel,
                            navigationActions = navigationActions
                        ) {
                            // Handle notification click
                        }
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
                    TopLevelDestinations.CALENDAR
                )
            )
        })
}

@Composable
fun NotificationItem(
    navigationActions: NavigationActions,
    listTravelViewModel: ListTravelViewModel,
    profileViewModel: ProfileModelView,
    notificationViewModel: NotificationViewModel,
    notification: Notification,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .clickable(onClick = onClick), // Handle item clicks
        shape = RoundedCornerShape(13.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = {
            navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES)
        } // TODO : Probably to change to a specific travel !!
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = notification.timestamp.toDate().toString(),
                fontSize = 12.sp,
                color = Color.Gray // Subtle color for the timestamp
            )
            Spacer(modifier = Modifier.height(4.dp)) // Space between message and timestamp

            Text(
                text = notification.content.toDisplayString(),
                fontSize = 15.sp,
                color = Color(0xFF669bbc) // A vibrant color for the title
            )

            // Button ACCEPT or DECLINE
            if (notification.notificationType == NotificationType.INVITATION) {
                Spacer(modifier = Modifier.height(4.dp)) // Space between message and timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            listTravelViewModel.getTravelById(notification.travelUid, { travel ->
                                notificationViewModel.sendNotification(
                                    Notification(
                                        notification.notificationUid,
                                        profileViewModel.profile.value.fsUid,
                                        notification.senderUid,
                                        notification.travelUid,
                                        NotificationContent.InvitationResponseNotification(
                                            profileViewModel.profile.value.name,
                                            travel!!.title,
                                            true
                                        ),
                                        NotificationType.ACCEPTED
                                    )
                                )

                                listTravelViewModel.addUserToTravel(
                                    profileViewModel.profile.value.email,
                                    travel,
                                    { updatedContainer ->
                                        listTravelViewModel.selectTravel(updatedContainer)
                                        Toast.makeText(
                                            context, "User added successfully!", Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    {
                                        Toast.makeText(
                                            context,
                                            "Failed to add user",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    })

                                Toast.makeText(context, "ACCEPTED", Toast.LENGTH_SHORT).show()
                            }, onFailure = {
                                Toast.makeText(context, "Failed to get travel", Toast.LENGTH_SHORT)
                                    .show()
                            })

                        },
                        modifier = Modifier.padding(end = 8.dp), // Space between buttons
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF12c15d)
                        )
                    ) {
                        Text(text = "ACCEPT")
                    }
                    Button(
                        onClick = {
                            listTravelViewModel.getTravelById(notification.travelUid, { travel ->
                                notificationViewModel.sendNotification(
                                    Notification(
                                        notification.notificationUid,
                                        profileViewModel.profile.value.fsUid,
                                        notification.senderUid,
                                        notification.travelUid,
                                        NotificationContent.InvitationResponseNotification(
                                            profileViewModel.profile.value.name,
                                            travel!!.title,
                                            false
                                        ),
                                        NotificationType.DECLINED
                                    )
                                )

                                Toast.makeText(context, "DECLINED", Toast.LENGTH_SHORT).show()
                            }, onFailure = {
                                Toast.makeText(context, "Failed to get travel", Toast.LENGTH_SHORT)
                                    .show()
                            })
                        },
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent, contentColor = Color.Red
                        )
                    ) {
                        Text(text = "DECLINE")
                    }
                }
            }
        }
    }
}

fun categorizeNotifications(notifications: List<Notification>): Map<String, List<Notification>> {
    val thisWeek = mutableListOf<Notification>()
    val lastWeek = mutableListOf<Notification>()
    val lastMonth = mutableListOf<Notification>()
    val lastYear = mutableListOf<Notification>()

    val now = Calendar.getInstance()

    notifications.forEach { notification ->
        val notificationTime = notification.timestamp.toDate()
        val diff = now.time.time - notificationTime.time

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
        "Last year" to lastYear
    )
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
