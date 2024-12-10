package com.github.se.travelpouch.ui.notification

import android.annotation.SuppressLint
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.helper.DocumentsManager
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
import com.github.se.travelpouch.ui.notifications.NotificationsScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull

class NotificationScreenTest {

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
  @Mock private lateinit var documentsManager: DocumentsManager
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
    documentsManager = mock(DocumentsManager::class.java)

    navigationActions = mock(NavigationActions::class.java)
    notificationViewModel = NotificationViewModel(notificationRepository)
    profileModelView = ProfileModelView(profileRepository)
    listTravelViewModel = ListTravelViewModel(travelRepository)
    activityViewModel = ActivityViewModel(activityRepository)
    documentViewModel = DocumentViewModel(documentRepository, documentsManager)
    eventViewModel = EventViewModel(eventRepository)
  }

  @SuppressLint("CheckResult")
  @Test
  fun bottomNavigationMenu_displayAndClickActions() {
    composeTestRule.setContent {
      NotificationsScreen(
          navigationActions = navigationActions,
          notificationViewModel = notificationViewModel,
          profileModelView = profileModelView,
          listTravelViewModel = listTravelViewModel,
          activityViewModel = activityViewModel,
          documentViewModel = documentViewModel,
          eventsViewModel = eventViewModel)
    }

    // Mock the repository call to return the desired notifications
    `when`(notificationRepository.fetchNotificationsForUser(anyOrNull(), anyOrNull())).then {
      it.getArgument<(List<Notification>) -> Unit>(1)(listOf(notification1))
    }

    // Load the notifications for the user (this will update the StateFlow)
    notificationViewModel.loadNotificationsForUser("uid")

    // Perform assertions and interactions
    composeTestRule.waitForIdle()

    // Interactions with the notification item
    composeTestRule.onNodeWithTag("notification_item").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("notification_item_accept_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notification_item_decline_button").assertIsDisplayed()

    // Interact with the "Delete All Notifications" button
    composeTestRule.onNodeWithTag("DeleteAllNotificationsButton").assertIsDisplayed()
  }

  @Test
  fun contentScaffold_display() {
    composeTestRule.setContent {
      NotificationsScreen(
          navigationActions = navigationActions,
          notificationViewModel = notificationViewModel,
          profileModelView = profileModelView,
          listTravelViewModel = listTravelViewModel,
          activityViewModel = activityViewModel,
          documentViewModel = documentViewModel,
          eventsViewModel = eventViewModel)
    }

    composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[0].assertExists()
    composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[0].isDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnNotificationsScreen")
        .onChildren()[0]
        .assert(hasText("This week"))
    composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[1].assertExists()
    composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[1].isDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnNotificationsScreen")
        .onChildren()[1]
        .assert(hasText("Last week"))
    composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[2].assertExists()
    composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[2].isDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnNotificationsScreen")
        .onChildren()[2]
        .assert(hasText("Last month"))
    composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[3].assertExists()
    composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[3].isDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnNotificationsScreen")
        .onChildren()[3]
        .assert(hasText("Last year"))
  }

  @Test
  fun topBar_display() {
    composeTestRule.setContent {
      NotificationsScreen(
          navigationActions = navigationActions,
          notificationViewModel = notificationViewModel,
          profileModelView = profileModelView,
          listTravelViewModel = listTravelViewModel,
          activityViewModel = activityViewModel,
          documentViewModel = documentViewModel,
          eventsViewModel = eventViewModel)
    }
    // composeTestRule.onNodeWithTag("TopAppBarNotificationsScreen").assertExists()
    composeTestRule.onNodeWithTag("TitleNotificationsScreen").assertExists()
    composeTestRule.onNodeWithTag("TitleNotificationsScreen").isDisplayed()
    composeTestRule.onNodeWithTag("TitleNotificationsScreen").assert(hasText("Notifications"))

    composeTestRule.onNodeWithTag("DeleteAllNotificationsButton").assertExists()
    composeTestRule.onNodeWithTag("DeleteAllNotificationsButton").isDisplayed()
    composeTestRule.onNodeWithTag("DeleteAllNotificationsButton").performClick()
  }
}
