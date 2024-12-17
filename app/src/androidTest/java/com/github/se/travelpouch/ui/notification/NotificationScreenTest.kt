package com.github.se.travelpouch.ui.notification

import android.annotation.SuppressLint
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.documents.DocumentsManager
import com.github.se.travelpouch.model.events.EventRepository
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationContent
import com.github.se.travelpouch.model.notifications.NotificationRepository
import com.github.se.travelpouch.model.notifications.NotificationSector
import com.github.se.travelpouch.model.notifications.NotificationType
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.profile.ProfileRepositoryFirebase
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainerMock.generateAutoObjectId
import com.github.se.travelpouch.model.travels.TravelContainerMock.generateAutoUserId
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.notifications.NotificationsScreen
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
          notificationType = notificationType1,
          sector = NotificationSector.TRAVEL)

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
    documentViewModel = DocumentViewModel(documentRepository, documentsManager, mock())
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

  @Test
  fun friendInvitationResponseIsHandledCorrectly() {
    val notification =
        Notification(
            "qwertzuiopasdfghjkly",
            "qwertzuiopasdfghjklyxcvbnm13",
            "qwertzuiopasdfghjklyxcvbnm14",
            null,
            NotificationContent.FriendInvitationNotification("test@test.com"),
            NotificationType.INVITATION,
            sector = NotificationSector.PROFILE)

    `when`(notificationRepository.fetchNotificationsForUser(anyOrNull(), anyOrNull())).then {
      it.getArgument<(List<Notification>) -> Unit>(1)(listOf(notification))
    }

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

    composeTestRule.onNodeWithText("ACCEPT").performClick()
    verify(profileRepository).addFriend(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun friendInvitationResponseIsHandledCorrectlyIfDeclined() {
    val notification =
        Notification(
            "qwertzuiopasdfghjkly",
            "qwertzuiopasdfghjklyxcvbnm13",
            "qwertzuiopasdfghjklyxcvbnm14",
            null,
            NotificationContent.FriendInvitationNotification("test@test.com"),
            NotificationType.INVITATION,
            sector = NotificationSector.PROFILE)

    `when`(notificationRepository.fetchNotificationsForUser(anyOrNull(), anyOrNull())).then {
      it.getArgument<(List<Notification>) -> Unit>(1)(listOf(notification))
    }

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

    composeTestRule.onNodeWithText("DECLINE").performClick()
    verify(profileRepository, never()).addFriend(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    verify(notificationRepository).addNotification(anyOrNull())
  }

  // Chat-GPT was helpful in this test because he helped me understand how to separate the capture
  // of
  // two calls that used the same Task<T>. We separated the task by having a task 1 with argument
  // captor 1
  // for getting the profile, and a task 2 with argument captor 2 for adding a friend. As both
  // addFriend
  // and getProfile use the same mocked database and the same repository, they need to be separated
  // in the task
  // otherwise there is a confusion. This is why each call has now its own task and its own argument
  // captor
  @Test
  fun acceptingInvitationCallbackWorks() {
    val friendProfile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm12",
            "username",
            "email@email.com",
            emptyMap(),
            "name",
            emptyList())

    val profile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm13",
            "usernameTest",
            "test@test.com",
            emptyMap(),
            "nameTest",
            emptyList())

    val notification =
        Notification(
            "qwertzuiopasdfghjkly",
            "qwertzuiopasdfghjklyxcvbnm12",
            "qwertzuiopasdfghjklyxcvbnm13",
            null,
            NotificationContent.FriendInvitationNotification("test@test.com"),
            NotificationType.INVITATION,
            sector = NotificationSector.PROFILE)

    `when`(notificationRepository.fetchNotificationsForUser(anyOrNull(), anyOrNull())).then {
      it.getArgument<(List<Notification>) -> Unit>(1)(listOf(notification))
    }

    val mockDatabase: FirebaseFirestore = mock()
    val mockCollectionReference: CollectionReference = mock()
    val mockDocumentReference: DocumentReference = mock()
    val mockDocumentSnapshot: DocumentSnapshot = mock()
    val mockDocumentSnapshotUser: DocumentSnapshot = mock()
    val taskDocumentSnapshotGetProfile: Task<DocumentSnapshot> = mock()
    val taskDocumentSnapshotAddFriend: Task<DocumentSnapshot> = mock()

    val secondLayerTask: Task<Void> = mock()

    whenever(mockDocumentSnapshot.id).thenReturn("qwertzuiopasdfghjklyxcvbnm12")
    whenever(mockDocumentSnapshot.getString("username")).thenReturn("username")
    whenever(mockDocumentSnapshot.getString("email")).thenReturn("email@email.com")
    whenever(mockDocumentSnapshot.get("friends")).thenReturn(emptyMap<String, String>())
    whenever(mockDocumentSnapshot.get("listoftravellinked")).thenReturn(emptyList<String>())
    whenever(mockDocumentSnapshot.getString("name")).thenReturn("name")
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockDocumentSnapshot.reference).thenReturn(mockDocumentReference)

    whenever(mockDocumentSnapshotUser.id).thenReturn(profile.fsUid)
    whenever(mockDocumentSnapshotUser.getString("username")).thenReturn(profile.username)
    whenever(mockDocumentSnapshotUser.getString("email")).thenReturn(profile.email)
    whenever(mockDocumentSnapshotUser.get("friends")).thenReturn(emptyMap<String, String>())
    whenever(mockDocumentSnapshotUser.get("listoftravellinked")).thenReturn(emptyList<String>())
    whenever(mockDocumentSnapshotUser.getString("name")).thenReturn(profile.name)

    val profileModelView = ProfileModelView(ProfileRepositoryFirebase(mockDatabase))

    val privateField = profileModelView.javaClass.getDeclaredField("profile_")
    privateField.isAccessible = true
    privateField.set(profileModelView, MutableStateFlow<Profile>(profile))

    whenever(mockDatabase.collection(anyOrNull())).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.document(anyOrNull())).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get())
        .thenReturn(taskDocumentSnapshotGetProfile, taskDocumentSnapshotAddFriend)

    whenever(taskDocumentSnapshotGetProfile.isSuccessful).thenReturn(true)
    whenever(taskDocumentSnapshotGetProfile.result).thenReturn(mockDocumentSnapshotUser)
    whenever(taskDocumentSnapshotGetProfile.addOnSuccessListener(anyOrNull()))
        .thenReturn(taskDocumentSnapshotGetProfile)
    whenever(taskDocumentSnapshotGetProfile.addOnFailureListener(anyOrNull()))
        .thenReturn(taskDocumentSnapshotGetProfile)

    whenever(taskDocumentSnapshotAddFriend.isSuccessful).thenReturn(true)
    whenever(taskDocumentSnapshotAddFriend.result).thenReturn(mockDocumentSnapshot)
    whenever(taskDocumentSnapshotAddFriend.addOnSuccessListener(anyOrNull()))
        .thenReturn(taskDocumentSnapshotAddFriend)
    whenever(taskDocumentSnapshotAddFriend.addOnFailureListener(anyOrNull()))
        .thenReturn(taskDocumentSnapshotAddFriend)

    whenever(mockDatabase.runTransaction<Void>(anyOrNull())).thenReturn(secondLayerTask)
    whenever(secondLayerTask.isSuccessful).thenReturn(true)
    whenever(secondLayerTask.addOnSuccessListener(anyOrNull())).thenReturn(secondLayerTask)
    whenever(secondLayerTask.addOnFailureListener(anyOrNull())).thenReturn(secondLayerTask)

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

    val onCompleteListenerCaptor3 = argumentCaptor<OnSuccessListener<DocumentSnapshot>>()
    verify(taskDocumentSnapshotGetProfile).addOnSuccessListener(onCompleteListenerCaptor3.capture())
    onCompleteListenerCaptor3.firstValue.onSuccess(mockDocumentSnapshotUser)

    composeTestRule.onNodeWithText("ACCEPT").performClick()

    val onCompleteListenerCaptor2 = argumentCaptor<OnSuccessListener<DocumentSnapshot>>()
    verify(taskDocumentSnapshotAddFriend).addOnSuccessListener(onCompleteListenerCaptor2.capture())
    onCompleteListenerCaptor2.firstValue.onSuccess(mockDocumentSnapshot)

    val onCompleteListenerCaptor = argumentCaptor<OnSuccessListener<Void>>()
    verify(secondLayerTask).addOnSuccessListener(onCompleteListenerCaptor.capture())
    composeTestRule.runOnIdle { onCompleteListenerCaptor.firstValue.onSuccess(null) }

    verify(notificationRepository).addNotification(anyOrNull())
  }
}
