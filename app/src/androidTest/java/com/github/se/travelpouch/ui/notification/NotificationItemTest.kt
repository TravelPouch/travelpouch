package com.github.se.travelpouch.ui.notification

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.helper.FileDownloader
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventRepository
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationContent
import com.github.se.travelpouch.model.notifications.NotificationRepository
import com.github.se.travelpouch.model.notifications.NotificationType
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainerMock.generateAutoObjectId
import com.github.se.travelpouch.model.travels.TravelContainerMock.generateAutoUserId
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.notifications.AcceptButton
import com.github.se.travelpouch.ui.notifications.DeclineButton
import com.github.se.travelpouch.ui.notifications.InvitationButtons
import com.github.se.travelpouch.ui.notifications.NotificationMessage
import com.github.se.travelpouch.ui.notifications.NotificationTimestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.*

class NotificationItemTest {

  @Mock private lateinit var notificationRepository: NotificationRepository
  @Mock private lateinit var notificationViewModel: NotificationViewModel
  @Mock private lateinit var profileRepository: ProfileRepository
  @Mock private lateinit var profileModelView: ProfileModelView
  @Mock private lateinit var navigationActions: NavigationActions
  @Mock private lateinit var travelRepository: TravelRepository
  @Mock private lateinit var listTravelViewModel: ListTravelViewModel
  @Mock private lateinit var activityRepository: ActivityRepository
  @Mock private lateinit var activityViewModel: ActivityViewModel
  @Mock private lateinit var documentRepository: DocumentRepository
  @Mock private lateinit var documentViewModel: DocumentViewModel
  @Mock private lateinit var fileDownloader: FileDownloader
  @Mock private lateinit var eventRepository: EventRepository
  @Mock private lateinit var eventViewModel: EventViewModel

  @get:Rule val composeTestRule = createComposeRule()

  val senderUid = generateAutoUserId()
  val receiverUid = generateAutoUserId()
  val notificationUid = generateAutoObjectId()
  val travel1Uid = generateAutoObjectId()
  val content1 =
      NotificationContent.InvitationNotification("John Doe", "Trip to Paris", Role.PARTICIPANT)
  val notificationType1 = NotificationType.INVITATION

  val notification1 =
      Notification(
          notificationUid = notificationUid,
          senderUid = senderUid,
          receiverUid = receiverUid,
          travelUid = travel1Uid,
          content = content1,
          notificationType = notificationType1)

  @Before
  fun setUp() {
    travelRepository = mock(TravelRepository::class.java)
    notificationRepository = mock(NotificationRepository::class.java)
    profileRepository = mock(ProfileRepository::class.java)
    activityRepository = mock(ActivityRepository::class.java)
    documentRepository = mock(DocumentRepository::class.java)
    eventRepository = mock(EventRepository::class.java)
    fileDownloader = mock(FileDownloader::class.java)

    navigationActions = mock(NavigationActions::class.java)
    notificationViewModel = NotificationViewModel(notificationRepository)
    profileModelView = ProfileModelView(profileRepository)
    listTravelViewModel = ListTravelViewModel(travelRepository)
    activityViewModel = ActivityViewModel(activityRepository)
    documentViewModel = DocumentViewModel(documentRepository, fileDownloader)
    eventViewModel = EventViewModel(eventRepository)
  }

  @Test
  fun notificationTimestampTest() {
    composeTestRule.setContent { NotificationTimestamp(notification1) }

    composeTestRule.onNodeWithTag("notification_item_timestamp").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("notification_item_timestamp")
        .assertTextEquals(notification1.timestamp.toDate().toString())
  }

  @Test
  fun notificationMessageTest() {
    composeTestRule.setContent { NotificationMessage(notification1) }

    composeTestRule.onNodeWithTag("notification_item_message").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("notification_item_message")
        .assertTextEquals(notification1.content.toDisplayString())
  }

  @Test
  fun invitationButtonsTest() {
    val context: android.content.Context = mock()

    composeTestRule.setContent {
      InvitationButtons(
          notification1,
          listTravelViewModel,
          profileModelView,
          notificationViewModel,
          context,
          eventViewModel)
    }

    composeTestRule.onNodeWithTag("notification_item_buttons").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notification_item_accept_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notification_item_decline_button").assertIsDisplayed()
  }

  @Test
  fun acceptButtonTest() {
    val context: android.content.Context = mock()

    composeTestRule.setContent {
      AcceptButton(
          notification1,
          listTravelViewModel,
          profileModelView,
          notificationViewModel,
          context,
          eventViewModel)
    }

    composeTestRule.onNodeWithTag("notification_item_accept_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notification_item_accept_button").assertTextEquals("ACCEPT")
    composeTestRule.onNodeWithTag("notification_item_accept_button").performClick()
    verify(travelRepository).getTravelById(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun declineButtonTest() {
    val context: android.content.Context = mock()

    composeTestRule.setContent {
      DeclineButton(
          notification1,
          listTravelViewModel,
          profileModelView,
          notificationViewModel,
          context,
          eventsViewModel = eventViewModel)
    }

    composeTestRule.onNodeWithTag("notification_item_decline_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notification_item_decline_button").assertTextEquals("DECLINE")
    composeTestRule.onNodeWithTag("notification_item_decline_button").performClick()
    verify(travelRepository).getTravelById(anyOrNull(), anyOrNull(), anyOrNull())
  }
}
