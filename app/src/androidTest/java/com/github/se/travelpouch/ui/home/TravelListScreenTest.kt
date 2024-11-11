import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.helper.FileDownloader
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventRepository
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.TravelContainerMock
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.ui.home.MapScreen
import com.github.se.travelpouch.ui.home.TravelListScreen
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TravelListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var profileRepository: ProfileRepository
  private lateinit var profileModelView: ProfileModelView

  private lateinit var navigationActions: NavigationActions
  private lateinit var listTravelViewModel: ListTravelViewModel
  private lateinit var travelRepository: TravelRepository
  private lateinit var activityViewModel: ActivityViewModel
  private lateinit var activityRepository: ActivityRepository
  private lateinit var documentViewModel: DocumentViewModel
  private lateinit var documentRepository: DocumentRepository
  private lateinit var eventViewModel: EventViewModel
  private lateinit var eventRepository: EventRepository
  @Mock private lateinit var mockFileDownloader: FileDownloader

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    travelRepository = mock(TravelRepository::class.java)
    activityRepository = mock(ActivityRepository::class.java)
    documentRepository = mock(DocumentRepository::class.java)
    eventRepository = mock(EventRepository::class.java)
    mockFileDownloader = mock()

    profileRepository = mock(ProfileRepository::class.java)
    profileModelView = ProfileModelView((profileRepository))

    listTravelViewModel = ListTravelViewModel(travelRepository)
    activityViewModel = ActivityViewModel(activityRepository)
    eventViewModel = EventViewModel(eventRepository)
    documentViewModel = DocumentViewModel(documentRepository, mockFileDownloader)

    // Mock the repository methods
    val participant = Participant(fsUid = TravelContainerMock.generateAutoUserId())
    val participants = mapOf(participant to Role.OWNER)
    val travelList =
        listOf(
            TravelContainer(
                fsUid = TravelContainerMock.generateAutoObjectId(),
                title = "Trip to Paris",
                description = "A wonderful trip to Paris",
                startTime = Timestamp(Date()),
                endTime = Timestamp(Date(Timestamp(Date()).toDate().time + 86400000)),
                location =
                    Location(
                        latitude = 48.8566,
                        longitude = 2.3522,
                        insertTime = Timestamp.now(),
                        name = "Paris"),
                allAttachments = emptyMap(),
                allParticipants = participants,
                emptyList()))

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<TravelContainer>) -> Unit
          onSuccess(travelList)
          null
        }
        .whenever(travelRepository)
        .getTravels(anyOrNull(), anyOrNull())

    // Initialize the ViewModel's travels StateFlow
    listTravelViewModel =
        ListTravelViewModel(travelRepository).apply {} // travels.value = travelList }
  }

  @Test
  fun hasRequiredComponents() {
    // Act
    composeTestRule.setContent {
      TravelListScreen(
          navigationActions = navigationActions,
          listTravelViewModel = listTravelViewModel,
          activityViewModel,
          eventViewModel,
          documentViewModel,
          profileModelView)
    }
    //Thread.sleep(3000)
    // Assert
    composeTestRule.onNodeWithTag("TravelListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTravelFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }

  @Test
  fun displayTravelListWhenNotEmpty() {
    composeTestRule.setContent {
      TravelListScreen(
          navigationActions = navigationActions,
          listTravelViewModel = listTravelViewModel,
          activityViewModel,
          eventViewModel,
          documentViewModel,
          profileModelView)
    }

    // Assert
    composeTestRule.onNodeWithTag("travelListItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Trip to Paris").assertIsDisplayed()
    composeTestRule.onNodeWithText("A wonderful trip to Paris").assertIsDisplayed()
  }

    @Test
    fun displayTravelListWhenEmpty() {
        // Act
        val travelsField = ListTravelViewModel::class.java.getDeclaredField("travels_")
        travelsField.isAccessible = true
        travelsField.set(listTravelViewModel, MutableStateFlow(emptyList<TravelContainer>()))

        doAnswer { invocation ->
            val onSuccess = invocation.getArgument(0) as (List<TravelContainer>) -> Unit
            //onSuccess(emptyList())
            null
        }
            .whenever(travelRepository)
            .getTravels(anyOrNull(), anyOrNull())

        composeTestRule.setContent {
            TravelListScreen(
                navigationActions = navigationActions,
                listTravelViewModel = listTravelViewModel,
                activityViewModel,
                eventViewModel,
                documentViewModel,
                profileModelView)
        }

        val isLoadingField = ListTravelViewModel::class.java.getDeclaredField("_isLoading")
        isLoadingField.isAccessible = true
        isLoadingField.set(listTravelViewModel, MutableStateFlow(true))
        composeTestRule.waitForIdle()
        // Assert
        composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loadingSpinner").assertIsDisplayed()
        isLoadingField.set(listTravelViewModel, MutableStateFlow(false))
        composeTestRule.waitForIdle()

    }


  @Test
  fun displayMapWithMarkers() {
    // Arrange
    val startTime = Timestamp(Date())
    val endTime = Timestamp(Date(startTime.toDate().time + 86400000))
    val participant = Participant(fsUid = TravelContainerMock.generateAutoUserId())
    val participants = mapOf(participant to Role.OWNER)
    val attachments = mapOf<String, String>()

    val locationParis =
        Location(latitude = 48.8566, longitude = 2.3522, insertTime = startTime, name = "Paris")
    val travelParis =
        TravelContainer(
            fsUid = TravelContainerMock.generateAutoObjectId(),
            title = "Trip to Paris",
            description = "A wonderful trip to Paris",
            startTime = startTime,
            endTime = endTime,
            location = locationParis,
            allAttachments = attachments,
            allParticipants = participants,
            emptyList())

    val locationNYC =
        Location(
            latitude = 40.7128, longitude = -74.0060, insertTime = startTime, name = "New York")
    val travelNYC =
        TravelContainer(
            fsUid = TravelContainerMock.generateAutoObjectId(),
            title = "Visit New York",
            description = "Exploring NYC",
            startTime = startTime,
            endTime = endTime,
            location = locationNYC,
            allAttachments = attachments,
            allParticipants = participants,
            emptyList())

    val travelContainers = listOf(travelParis, travelNYC)

    // Act
    composeTestRule.setContent { MapScreen(travelContainers = travelContainers) }
    composeTestRule.waitForIdle()
    //Thread.sleep(3000)

    // Assert
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }

  //  @Test
  //  fun testNavigationBottomBar() {
  //    // Act
  //      composeTestRule.setContent {
  //          TravelListScreen(
  //              navigationActions = navigationActions,
  //              listTravelViewModel = listTravelViewModel,
  //              activityViewModel, eventViewModel, documentViewModel
  //          )
  //      }
  //    composeTestRule.waitForIdle()
  //
  //    // Click on the "Activities" navigation item
  //    composeTestRule.onNodeWithTag("Travels").performClick()
  //    composeTestRule.waitForIdle()
  //
  //    // Verify that the navigation action was called for "Activities"
  //    verify(navigationActions).navigateTo(Screen.TRAVEL_LIST)
  //
  //    // Click on the "Calendar" navigation item
  //    composeTestRule.onNodeWithTag("Calendar").performClick()
  //    composeTestRule.waitForIdle()
  //
  //    // Verify that the navigation action was called for "Calendar"
  //    verify(navigationActions).navigateTo(Screen.CALENDAR)
  //  }

  @Test
  fun testTravelItemClickNavigatesToTravelActivities() {
    // Act
    composeTestRule.setContent {
      TravelListScreen(
          navigationActions = navigationActions,
          listTravelViewModel = listTravelViewModel,
          activityViewModel,
          eventViewModel,
          documentViewModel,
          profileModelView)
    }
    composeTestRule.waitForIdle()

    // Find and click on a travel list item
    composeTestRule.onNodeWithTag("travelListItem").performClick()
    composeTestRule.waitForIdle()

    // Verify that the navigation action was called for TRAVEL_ACTIVITIES
    verify(navigationActions).navigateTo(Screen.TRAVEL_ACTIVITIES)
  }

  @Test
  fun testCreateTravelFabClickNavigatesToAddTravel() {
    // Act
    composeTestRule.setContent {
      TravelListScreen(
          navigationActions = navigationActions,
          listTravelViewModel = listTravelViewModel,
          activityViewModel,
          eventViewModel,
          documentViewModel,
          profileModelView)
    }
    composeTestRule.waitForIdle()

    // Find and click on the create travel FAB
    composeTestRule.onNodeWithTag("createTravelFab").performClick()
    composeTestRule.waitForIdle()

    // Verify that the navigation action was called for ADD_TRAVEL
    verify(navigationActions).navigateTo(Screen.ADD_TRAVEL)
  }
}
