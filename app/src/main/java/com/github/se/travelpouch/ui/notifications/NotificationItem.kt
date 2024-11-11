package com.github.se.travelpouch.ui.notifications

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
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
    activityViewModel: ActivityViewModel,
    documentViewModel: DocumentViewModel,
    eventsViewModel: EventViewModel,
) {
  val context = LocalContext.current

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp, horizontal = 12.dp)
              .clickable {
                onCardClick(
                    notification,
                    listTravelViewModel,
                    activityViewModel,
                    documentViewModel,
                    eventsViewModel,
                    navigationActions,
                    context)
              }
              .testTag("notification_item"),
      shape = RoundedCornerShape(13.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth().testTag("notification_item_content"),
            verticalArrangement = Arrangement.SpaceBetween) {
              NotificationTimestamp(notification)
              Spacer(modifier = Modifier.height(4.dp).testTag("notification_item_space"))
              NotificationMessage(notification)

              if (notification.notificationType == NotificationType.INVITATION) {
                InvitationButtons(
                    notification,
                    listTravelViewModel,
                    profileViewModel,
                    notificationViewModel,
                    context)
              }
            }
      }
}

@Composable
fun NotificationTimestamp(notification: Notification) {
  Text(
      text = notification.timestamp.toDate().toString(),
      fontSize = 12.sp,
      color = Color.Gray,
      modifier = Modifier.testTag("notification_item_timestamp"))
}

@Composable
fun NotificationMessage(notification: Notification) {
  Text(
      text = notification.content.toDisplayString(),
      fontSize = 15.sp,
      color = Color(0xFF669bbc),
      modifier = Modifier.testTag("notification_item_message"))
}

@Composable
fun InvitationButtons(
    notification: Notification,
    listTravelViewModel: ListTravelViewModel,
    profileViewModel: ProfileModelView,
    notificationViewModel: NotificationViewModel,
    context: android.content.Context
) {
  Row(
      modifier = Modifier.fillMaxWidth().testTag("notification_item_buttons"),
      horizontalArrangement = Arrangement.Center) {
        AcceptButton(
            notification, listTravelViewModel, profileViewModel, notificationViewModel, context)
        DeclineButton(
            notification, listTravelViewModel, profileViewModel, notificationViewModel, context)
      }
}

@Composable
fun AcceptButton(
    notification: Notification,
    listTravelViewModel: ListTravelViewModel,
    profileViewModel: ProfileModelView,
    notificationViewModel: NotificationViewModel,
    context: android.content.Context
) {
  Button(
      onClick = {
        handleInvitationResponse(
            notification,
            listTravelViewModel,
            profileViewModel,
            notificationViewModel,
            context,
            isAccepted = true)
      },
      modifier = Modifier.padding(end = 8.dp).testTag("notification_item_accept_button"),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = Color.Transparent, contentColor = Color(0xFF12c15d))) {
        Text(text = "ACCEPT")
      }
}

@Composable
fun DeclineButton(
    notification: Notification,
    listTravelViewModel: ListTravelViewModel,
    profileViewModel: ProfileModelView,
    notificationViewModel: NotificationViewModel,
    context: android.content.Context
) {
  Button(
      onClick = {
        handleInvitationResponse(
            notification,
            listTravelViewModel,
            profileViewModel,
            notificationViewModel,
            context,
            isAccepted = false)
      },
      colors =
          ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Red),
      modifier = Modifier.testTag("notification_item_decline_button")) {
        Text(text = "DECLINE")
      }
}

fun handleInvitationResponse(
    notification: Notification,
    listTravelViewModel: ListTravelViewModel,
    profileViewModel: ProfileModelView,
    notificationViewModel: NotificationViewModel,
    context: android.content.Context,
    isAccepted: Boolean
) {
  listTravelViewModel.getTravelById(
      notification.travelUid,
      { travel ->
        val responseType = if (isAccepted) NotificationType.ACCEPTED else NotificationType.DECLINED
        val responseMessage = if (isAccepted) "ACCEPTED" else "DECLINED"

        val invitationResponse =
            Notification(
                notification.notificationUid,
                profileViewModel.profile.value.fsUid,
                notification.senderUid,
                notification.travelUid,
                NotificationContent.InvitationResponseNotification(
                    profileViewModel.profile.value.name, travel!!.title, isAccepted),
                responseType)

        notificationViewModel.sendNotification(invitationResponse)
        if (isAccepted) {
          listTravelViewModel.addUserToTravel(
              profileViewModel.profile.value.email,
              travel,
              { updatedContainer ->
                listTravelViewModel.selectTravel(updatedContainer)
                Toast.makeText(context, "User added successfully!", Toast.LENGTH_SHORT).show()
              },
              { Toast.makeText(context, "Failed to add user", Toast.LENGTH_SHORT).show() })
        }
        Toast.makeText(context, responseMessage, Toast.LENGTH_SHORT).show()
      },
      onFailure = { Toast.makeText(context, "Failed to get travel", Toast.LENGTH_SHORT).show() })
}

fun onCardClick(
    notification: Notification,
    listTravelViewModel: ListTravelViewModel,
    activityViewModel: ActivityViewModel,
    documentViewModel: DocumentViewModel,
    eventsViewModel: EventViewModel,
    navigationActions: NavigationActions,
    context: android.content.Context
) {
  listTravelViewModel.getTravelById(
      notification.travelUid,
      onSuccess = { travel ->
        listTravelViewModel.selectTravel(travel!!)
        activityViewModel.setIdTravel(travel.fsUid)
        documentViewModel.setIdTravel(travel.fsUid)
        eventsViewModel.setIdTravel(travel.fsUid)
        navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES)
      },
      onFailure = { Toast.makeText(context, "Failed to get travel", Toast.LENGTH_SHORT).show() })
}
