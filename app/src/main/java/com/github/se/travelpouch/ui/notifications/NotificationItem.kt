package com.github.se.travelpouch.ui.notifications

// parts of this file was generated using Github Copilot or ChatGPT

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationContent
import com.github.se.travelpouch.model.notifications.NotificationSector
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
                    profileViewModel,
                    context)
              }
              .testTag("notification_item"),
      shape = RoundedCornerShape(13.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth().testTag("notification_item_content"),
            verticalArrangement = Arrangement.SpaceBetween) {
              Text(notification.sector.toString())
              NotificationTimestamp(notification)
              Spacer(modifier = Modifier.height(4.dp).testTag("notification_item_space"))
              NotificationMessage(notification)

              if (notification.notificationType == NotificationType.INVITATION) {
                InvitationButtons(
                    notification,
                    listTravelViewModel,
                    profileViewModel,
                    notificationViewModel,
                    context,
                    eventsViewModel)
              }
            }
      }
}

@Composable
fun NotificationTimestamp(notification: Notification) {
  val darkTheme = isSystemInDarkTheme()
  Text(
      text = notification.timestamp.toDate().toString(),
      style = MaterialTheme.typography.labelMedium,
      color = if (darkTheme) Color.LightGray else Color.Gray,
      modifier = Modifier.testTag("notification_item_timestamp"))
}

@Composable
fun NotificationMessage(notification: Notification) {
  Text(
      text = notification.content.toDisplayString(),
      style = MaterialTheme.typography.bodyLarge,
      color = Color(0xFF669bbc),
      modifier = Modifier.testTag("notification_item_message"))
}

@Composable
fun InvitationButtons(
    notification: Notification,
    listTravelViewModel: ListTravelViewModel,
    profileViewModel: ProfileModelView,
    notificationViewModel: NotificationViewModel,
    context: android.content.Context,
    eventsViewModel: EventViewModel
) {
  var b by remember { mutableStateOf(true) }

  Row(
      modifier = Modifier.fillMaxWidth().testTag("notification_item_buttons"),
      horizontalArrangement = Arrangement.Center) {
        AcceptButton(
            notification,
            listTravelViewModel,
            profileViewModel,
            notificationViewModel,
            context,
            eventsViewModel,
            { b = !b },
            b)
        DeclineButton(
            notification,
            listTravelViewModel,
            profileViewModel,
            notificationViewModel,
            context,
            eventsViewModel,
            { b = !b },
            b)
      }
}

@Composable
fun AcceptButton(
    notification: Notification,
    listTravelViewModel: ListTravelViewModel,
    profileViewModel: ProfileModelView,
    notificationViewModel: NotificationViewModel,
    context: android.content.Context,
    eventsViewModel: EventViewModel,
    chosen: () -> Unit,
    enabled: Boolean
) {
  Button(
      onClick = {
        chosen()
        handleInvitationResponse(
            notification,
            listTravelViewModel,
            profileViewModel,
            notificationViewModel,
            context,
            isAccepted = true,
            eventsViewModel,
            chosen)
      },
      modifier = Modifier.padding(end = 8.dp).testTag("notification_item_accept_button"),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = Color.Transparent, contentColor = Color(0xFF12c15d)),
      enabled = enabled) {
        Text(text = "ACCEPT")
      }
}

@Composable
fun DeclineButton(
    notification: Notification,
    listTravelViewModel: ListTravelViewModel,
    profileViewModel: ProfileModelView,
    notificationViewModel: NotificationViewModel,
    context: android.content.Context,
    eventsViewModel: EventViewModel,
    chosen: () -> Unit,
    enabled: Boolean
) {
  Button(
      onClick = {
        chosen()
        handleInvitationResponse(
            notification,
            listTravelViewModel,
            profileViewModel,
            notificationViewModel,
            context,
            isAccepted = false,
            eventsViewModel = eventsViewModel,
            chosen)
      },
      colors =
          ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Red),
      modifier = Modifier.testTag("notification_item_decline_button"),
      enabled = enabled) {
        Text(text = "DECLINE")
      }
}

fun handleInvitationResponse(
    notification: Notification,
    listTravelViewModel: ListTravelViewModel,
    profileViewModel: ProfileModelView,
    notificationViewModel: NotificationViewModel,
    context: android.content.Context,
    isAccepted: Boolean,
    eventsViewModel: EventViewModel,
    chosen: () -> Unit
) {
  when (notification.sector) {
    NotificationSector.TRAVEL -> {
      listTravelViewModel.getTravelById(
          notification.travelUid!!,
          { travel ->
            val responseType =
                if (isAccepted) NotificationType.ACCEPTED else NotificationType.DECLINED
            val responseMessage = if (isAccepted) "ACCEPTED" else "DECLINED"


              val responseNotification =
                  NotificationContent.InvitationResponseNotification(
                    profileViewModel.profile.value.username,
                      travel!!.title, isAccepted)
            val invitationResponse =
                Notification(
                    notification.notificationUid,
                    profileViewModel.profile.value.fsUid,
                    notification.senderUid,
                    notification.travelUid,
                    responseNotification,
                    responseType,
                    sector = notification.sector)

              notificationViewModel.sendNotificationToUser(
                  notification.senderUid,
                  responseNotification
              )


            notificationViewModel.sendNotification(invitationResponse)
            if (isAccepted) {
              listTravelViewModel.addUserToTravel(
                  profileViewModel.profile.value.email,
                  travel,
                  { updatedContainer ->
                    listTravelViewModel.selectTravel(updatedContainer)
                    Toast.makeText(context, "User added successfully!", Toast.LENGTH_SHORT).show()
                  },
                  {
                    Toast.makeText(context, "Failed to add user", Toast.LENGTH_SHORT).show()
                    chosen()
                  },
                  eventsViewModel.getNewDocumentReferenceForNewTravel(travel.fsUid))
            }
            notificationViewModel.loadNotificationsForUser(profileViewModel.profile.value.fsUid)
            Toast.makeText(context, responseMessage, Toast.LENGTH_SHORT).show()
          },
          onFailure = {
            Toast.makeText(context, "Failed to get travel", Toast.LENGTH_SHORT).show()
            chosen()
          })
    }
    NotificationSector.PROFILE -> {
      if (isAccepted) {
        profileViewModel.addFriend(
            notification.senderUid,
            onSuccess = {
                val firendNotification =
                    NotificationContent.FriendInvitationResponseNotification(
                        profileViewModel.profile.value.email, true)
              val invitationResponse =
                  Notification(
                      notification.notificationUid,
                      profileViewModel.profile.value.fsUid,
                      notification.senderUid,
                      notification.travelUid,
                      firendNotification,
                      NotificationType.ACCEPTED,
                      sector = notification.sector)

              notificationViewModel.sendNotification(invitationResponse)
                notificationViewModel.sendNotificationToUser(
                    notification.senderUid,
                    firendNotification
                )

              Toast.makeText(context, "Friend added", Toast.LENGTH_LONG).show()
            },
            onFailure = { e -> Toast.makeText(context, e.message!!, Toast.LENGTH_LONG).show() })
      } else {
          val firendNotification =
              NotificationContent.FriendInvitationResponseNotification(
                  profileViewModel.profile.value.email, false)
        val invitationResponse =
            Notification(
                notification.notificationUid,
                profileViewModel.profile.value.fsUid,
                notification.senderUid,
                notification.travelUid,
                firendNotification,
                NotificationType.DECLINED,
                sector = notification.sector)

        notificationViewModel.sendNotification(invitationResponse)

          notificationViewModel.sendNotificationToUser(
              notification.senderUid,
              firendNotification
          )
          
        Toast.makeText(context, "Request declined", Toast.LENGTH_LONG).show()
      }
    }
  }
}

fun onCardClick(
    notification: Notification,
    listTravelViewModel: ListTravelViewModel,
    activityViewModel: ActivityViewModel,
    documentViewModel: DocumentViewModel,
    eventsViewModel: EventViewModel,
    navigationActions: NavigationActions,
    profileViewModel: ProfileModelView,
    context: android.content.Context
) {

  if (notification.sector == NotificationSector.TRAVEL && notification.travelUid != null) {
    listTravelViewModel.getTravelById(
        notification.travelUid,
        onSuccess = { travel ->
          if (travel != null) {
            if (travel.listParticipant.contains(profileViewModel.profile.value.fsUid)) {
              listTravelViewModel.selectTravel(travel)
              activityViewModel.setIdTravel(travel.fsUid)
              documentViewModel.setIdTravel(travel.fsUid)
              eventsViewModel.setIdTravel(travel.fsUid)
              navigationActions.navigateTo(Screen.SWIPER)
            } else {
              Toast.makeText(context, "You are not a member of this travel", Toast.LENGTH_SHORT)
                  .show()
            }
          } else {
            Toast.makeText(context, "Travel not found", Toast.LENGTH_SHORT).show()
          }
        },
        onFailure = { Toast.makeText(context, "Failed to get travel", Toast.LENGTH_SHORT).show() })
  }
}
