package com.github.se.travelpouch.ui.notification

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import android.view.View
import android.widget.TextView
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
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
import com.github.se.travelpouch.model.profile.Profile
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
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.whenever
import java.util.regex.Pattern.matches

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

    private val myProfile = Profile(
        fsUid = generateAutoUserId(),
        name = "John Doe",
        email = "email@gmail.com",
        username = "john_doe",
        friends = emptyMap(),
        userTravelList = emptyList())

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
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").assertExists()

        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[0].assertExists()
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[0].isDisplayed()
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[0].assert(hasText("This week"))
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[1].assertExists()
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[1].isDisplayed()
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[1].assert(hasText("Last week"))
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[2].assertExists()
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[2].isDisplayed()
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[2].assert(hasText("Last month"))
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[3].assertExists()
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[3].isDisplayed()
        composeTestRule.onNodeWithTag("LazyColumnNotificationsScreen").onChildren()[3].assert(hasText("Last year"))
    }

    @Test
    fun topBar_display() {
        composeTestRule.onNodeWithTag("TopAppBarNotificationsScreen").assertExists()
        composeTestRule.onNodeWithTag("TitleNotificationsScreen").assertExists()
        composeTestRule.onNodeWithTag("TitleNotificationsScreen").isDisplayed()
        composeTestRule.onNodeWithTag("TitleNotificationsScreen").assert(hasText("Notifications"))

        composeTestRule.onNodeWithTag("DeleteAllNotificationsButton").assertExists()
        composeTestRule.onNodeWithTag("DeleteAllNotificationsButton").isDisplayed()
        composeTestRule.onNodeWithTag("DeleteAllNotificationsButton").performClick()
    }

}