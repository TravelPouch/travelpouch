package com.github.se.travelpouch.ui.notifications

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationContent
import com.github.se.travelpouch.model.notifications.NotificationType
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen

@Composable
fun NotificationItem(
    navigationActions: NavigationActions,
    listTravelViewModel: ListTravelViewModel,
    profileViewModel: ProfileModelView,
    notificationViewModel: NotificationViewModel,
    notification: Notification,
    activityViewModel : ActivityViewModel,
    documentViewModel: DocumentViewModel,
    eventsViewModel: EventViewModel,
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
            // Get travel from notification
            listTravelViewModel.getTravelById(notification.travelUid,
                onSuccess = { travel ->
                    listTravelViewModel.selectTravel(travel!!)
                    activityViewModel.setIdTravel(travel.fsUid)
                    documentViewModel.setIdTravel(travel.fsUid)
                    eventsViewModel.setIdTravel(travel.fsUid)
                    navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES)
                },
                onFailure = {
                    Toast.makeText(context, "Failed to get travel", Toast.LENGTH_SHORT).show()
                }
            )
        }
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