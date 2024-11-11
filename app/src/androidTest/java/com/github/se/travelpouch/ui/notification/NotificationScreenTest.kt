package com.github.se.travelpouch.ui.notification

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.github.se.travelpouch.ui.navigation.BottomNavigationMenu
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.ui.navigation.TopLevelDestinations
import com.github.se.travelpouch.ui.notifications.NotificationsScreen
import com.github.se.travelpouch.ui.travel.ParticipantListScreen
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.whenever

class NotificationScreenTest {

    @Mock
    private lateinit var notificationRepository: NotificationRepository
    @Mock
    private lateinit var notificationViewModel: NotificationViewModel
    @Mock
    private lateinit var profileRepository: ProfileRepository
    @Mock
    private lateinit var profileModelView: ProfileModelView
    @Mock
    private lateinit var navController: NavHostController
    @Mock
    private lateinit var navigationActions: NavigationActions
    @Mock
    private lateinit var travelRepository: TravelRepository
    @Mock
    private lateinit var listTravelViewModel: ListTravelViewModel
    @Mock
    private lateinit var activityRepository: ActivityRepository
    @Mock
    private lateinit var activityViewModel: ActivityViewModel
    @Mock
    private lateinit var documentRepository: DocumentRepository
    @Mock
    private lateinit var documentViewModel: DocumentViewModel
    @Mock
    private lateinit var fileDownloader: FileDownloader
    @Mock
    private lateinit var eventRepository: EventRepository
    @Mock
    private lateinit var eventViewModel: EventViewModel

    @get:Rule
    val composeTestRule = createComposeRule()

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

        composeTestRule.setContent {
            NotificationsScreen(
                navigationActions = navigationActions,
                notificationViewModel = notificationViewModel,
                profileModelView = profileModelView,
                listTravelViewModel = listTravelViewModel,
                activityViewModel = activityViewModel,
                documentViewModel = documentViewModel,
                eventsViewModel = eventViewModel
            )
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun bottomNavigationMenu_displayAndClickActions() {

        composeTestRule.onNodeWithTag(TopLevelDestinations.NOTIFICATION.textId).assertExists()
        composeTestRule.onNodeWithTag(TopLevelDestinations.NOTIFICATION.textId).performClick()
        verify(navigationActions, times(1)).navigateTo(Screen.NOTIFICATION)

        composeTestRule.onNodeWithTag(TopLevelDestinations.TRAVELS.textId).assertExists()
        composeTestRule.onNodeWithTag(TopLevelDestinations.TRAVELS.textId).performClick()
        verify(navigationActions, times(1)).navigateTo(Screen.TRAVEL_LIST)

        composeTestRule.onNodeWithTag(TopLevelDestinations.CALENDAR.textId).assertExists()
        composeTestRule.onNodeWithTag(TopLevelDestinations.CALENDAR.textId).performClick()
        verify(navigationActions, times(1)).navigateTo(Screen.CALENDAR)
    }

    @Test
    fun contentScaffold_display() {
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen", useUnmergedTree = true).assertExists()

        //Test categories text
        composeTestRule.onNodeWithTag("CategoryTitle", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("CategoryTitle", useUnmergedTree = true).assert(hasText("This week"))
        composeTestRule.onNodeWithTag("CategoryTitle", useUnmergedTree = true).assert(hasText("Last week"))
        composeTestRule.onNodeWithTag("CategoryTitle", useUnmergedTree = true).assert(hasText("Last month"))
        composeTestRule.onNodeWithTag("CategoryTitle", useUnmergedTree = true).assert(hasText("Last year"))
    }

    @Test
    fun topBar_display() {
        composeTestRule.onNodeWithTag("TopAppBarNotificationsScreen").assertExists()
        composeTestRule.onNodeWithTag("TopAppBarNotificationsScreen").assert(hasText("Notifications"))

        composeTestRule.onNodeWithTag("DefaultTopAppBarBackButton").assertExists()
        composeTestRule.onNodeWithTag("DefaultTopAppBarBackButton").performClick()
        verify(notificationViewModel).deleteAllNotificationsForUser(anyString(), any(), any())

    }

}