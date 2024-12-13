import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.documents.DocumentsManager
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
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

  @Mock private lateinit var mockDocumentsManager: DocumentsManager
  @Mock private lateinit var mockDataStore: DataStore<Preferences>

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    travelRepository = mock(TravelRepository::class.java)
    activityRepository = mock(ActivityRepository::class.java)
    documentRepository = mock(DocumentRepository::class.java)
    eventRepository = mock(EventRepository::class.java)
    mockDocumentsManager = mock()
    mockDataStore = mock()

    profileRepository = mock(ProfileRepository::class.java)
    profileModelView = ProfileModelView((profileRepository))

    listTravelViewModel = ListTravelViewModel(travelRepository)
    activityViewModel = ActivityViewModel(activityRepository)
    eventViewModel = EventViewModel(eventRepository)
    documentViewModel = DocumentViewModel(documentRepository, mockDocumentsManager, mockDataStore)

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
    listTravelViewModel = ListTravelViewModel(travelRepository).apply {}
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
          // don't return any travels so that it keeps spinning
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

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loadingSpinner").assertIsDisplayed()
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

    // Assert
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }

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
    verify(navigationActions).navigateTo(Screen.SWIPER)
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

  @Test
  fun displayEmptyTravelListAndDragStowawayMap() {
    // Arrange
    val emptyTravelList = emptyList<TravelContainer>()
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<TravelContainer>) -> Unit
          onSuccess(emptyTravelList)
          null
        }
        .whenever(travelRepository)
        .getTravels(anyOrNull(), anyOrNull())

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

    // Assert
    composeTestRule.onNodeWithTag("emptyTravelPrompt").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapLatch").assertIsDisplayed()
    val density = composeTestRule.density
    val heightBefore =
        (composeTestRule.onNodeWithTag("mapScreen").captureToImage().height / density.density)
            .roundToInt()
    var dragDistance = 100 // For example, drag it 100 dp vertically
    val correctionFactor = 8 // somehow all values are off by 8 dp
    // we drag it up by 100 dp
    composeTestRule.onNodeWithTag("mapLatch").performTouchInput {
      down(center) // Start the drag at the center of the mapLatch
      moveBy(Offset(0.dp.toPx(), -(dragDistance + correctionFactor).dp.toPx()))
      up() // End the drag
    }
    composeTestRule
        .onNodeWithTag("mapScreen")
        .assertHeightIsEqualTo((heightBefore - dragDistance).dp)
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    // we drag it down by 100 dp
    composeTestRule.onNodeWithTag("mapLatch").performTouchInput {
      down(center) // Start the drag at the center of the mapLatch
      moveBy(Offset(0.dp.toPx(), +(dragDistance + correctionFactor).dp.toPx()))
      up() // End the drag
    }
    composeTestRule.onNodeWithTag("mapScreen").assertHeightIsEqualTo(heightBefore.dp)
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()

    // we over-drag it to see that nothing changes
    composeTestRule.onNodeWithTag("mapLatch").performTouchInput {
      down(center) // Start the drag at the center of the mapLatch
      moveBy(Offset(0.dp.toPx(), +(dragDistance + correctionFactor).dp.toPx()))
      up() // End the drag
    }
    composeTestRule.onNodeWithTag("mapScreen").assertHeightIsEqualTo(heightBefore.dp)
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()

    // we will drag it down by 160 dp so we have map size at 270 - 160 = 110 dp because it's 10 dp
    // before the threshold for closer
    dragDistance = 160
    composeTestRule.onNodeWithTag("mapLatch").performTouchInput {
      down(center) // Start the drag at the center of the mapLatch
      moveBy(Offset(0.dp.toPx(), -(dragDistance + correctionFactor).dp.toPx()))
      up() // End the drag
    }
    composeTestRule
        .onNodeWithTag("mapScreen")
        .assertHeightIsEqualTo((heightBefore - dragDistance).dp)
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    // we will drag it down by 15 dp, so we hit 95 dp, this should lead to the map closing itself
    dragDistance = 15
    composeTestRule.onNodeWithTag("mapLatch").performTouchInput {
      down(center) // Start the drag at the center of the mapLatch
      moveBy(Offset(0.dp.toPx(), -(dragDistance + correctionFactor).dp.toPx()))
      up() // End the drag
    }
    composeTestRule.onNodeWithTag("mapScreen").assertHeightIsEqualTo(0.dp)
    composeTestRule.onNodeWithTag("mapScreen").assertIsNotDisplayed()
    // we will now drag it up by 50 dp, and it shouldn't snap back to 0 dp
    dragDistance = 50
    composeTestRule.onNodeWithTag("mapLatch").performTouchInput {
      down(center) // Start the drag at the center of the mapLatch
      moveBy(Offset(0.dp.toPx(), +(dragDistance + correctionFactor).dp.toPx()))
      up() // End the drag
    }
    composeTestRule.onNodeWithTag("mapScreen").assertHeightIsEqualTo(dragDistance.dp)
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    // we will now drag it up by 60 dp, and this means we're in the zone where the snap to 0 is
    // available again
    dragDistance = 60
    composeTestRule.onNodeWithTag("mapLatch").performTouchInput {
      down(center) // Start the drag at the center of the mapLatch
      moveBy(Offset(0.dp.toPx(), +(dragDistance + correctionFactor).dp.toPx()))
      up() // End the drag
    }
    composeTestRule.onNodeWithTag("mapScreen").assertHeightIsEqualTo(110.dp)
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    // now it we drag it down again by 20dp, we should hit the threshold and it should close
    dragDistance = 20
    composeTestRule.onNodeWithTag("mapLatch").performTouchInput {
      down(center) // Start the drag at the center of the mapLatch
      moveBy(Offset(0.dp.toPx(), -(dragDistance + correctionFactor).dp.toPx()))
      up() // End the drag
    }
    composeTestRule.onNodeWithTag("mapScreen").assertHeightIsEqualTo(0.dp)
    composeTestRule.onNodeWithTag("mapScreen").assertIsNotDisplayed()
    // now we drag it up and it shouldn't change anything
    dragDistance = 20
    composeTestRule.onNodeWithTag("mapLatch").performTouchInput {
      down(center) // Start the drag at the center of the mapLatch
      moveBy(Offset(0.dp.toPx(), -(dragDistance + correctionFactor).dp.toPx()))
      up() // End the drag
    }
    composeTestRule.onNodeWithTag("mapScreen").assertHeightIsEqualTo(0.dp)
    composeTestRule.onNodeWithTag("mapScreen").assertIsNotDisplayed()
    // and now we don't have any "drag" debt so it we drag it back by 60 dp, it should open
    dragDistance = 60
    composeTestRule.onNodeWithTag("mapLatch").performTouchInput {
      down(center) // Start the drag at the center of the mapLatch
      moveBy(Offset(0.dp.toPx(), +(dragDistance + correctionFactor).dp.toPx()))
      up() // End the drag
    }
    composeTestRule.onNodeWithTag("mapScreen").assertHeightIsEqualTo(60.dp)
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }

  @Test
  fun verifiesThatMenuWorksCorrectly() {
    composeTestRule.setContent {
      TravelListScreen(
          navigationActions = navigationActions,
          listTravelViewModel = listTravelViewModel,
          activityViewModel,
          eventViewModel,
          documentViewModel,
          profileModelView)
    }

    // This button is always displayed, but not always visible
    composeTestRule.onNodeWithTag("menuFab").assertIsDisplayed()

    // When menu is closed
    composeTestRule.onNodeWithTag("closingMenuBox").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("closingMenuFab").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("drawerSheetMenu").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemProfile").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemHome").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemNotifications").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemLogout").assertIsNotDisplayed()

    // Click on menu button
    composeTestRule.onNodeWithTag("menuFab").performClick()

    // Menu opened
    composeTestRule.onNodeWithTag("closingMenuBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("closingMenuFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("drawerSheetMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("itemProfile").assertIsDisplayed()
    composeTestRule.onNodeWithTag("itemHome").assertIsDisplayed()
    composeTestRule.onNodeWithTag("itemNotifications").assertIsDisplayed()
    composeTestRule.onNodeWithTag("itemLogout").assertIsDisplayed()

    // Click on closing menu button
    composeTestRule.onNodeWithTag("closingMenuFab").performClick()

    // Menu closed with button
    composeTestRule.onNodeWithTag("closingMenuBox").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("closingMenuFab").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("drawerSheetMenu").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemProfile").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemHome").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemNotifications").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemLogout").assertIsNotDisplayed()

    // Click on menu button
    composeTestRule.onNodeWithTag("menuFab").performClick()

    // Menu opened
    composeTestRule.onNodeWithTag("closingMenuBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("closingMenuFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("drawerSheetMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("itemProfile").assertIsDisplayed()
    composeTestRule.onNodeWithTag("itemHome").assertIsDisplayed()
    composeTestRule.onNodeWithTag("itemNotifications").assertIsDisplayed()

    // Click on closing menu button
    composeTestRule.onNodeWithTag("closingMenuBox").performClick()

    // Menu closed with button
    composeTestRule.onNodeWithTag("closingMenuBox").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("closingMenuFab").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("drawerSheetMenu").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemProfile").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemHome").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemNotifications").assertIsNotDisplayed()

    // click on menu buttons
    composeTestRule.onNodeWithTag("menuFab").performClick()

    composeTestRule.onNodeWithTag("itemLogout").performClick()
    // Verify that the navigation action was called for LOGOUT
    verify(navigationActions).navigateTo(Screen.AUTH)
    composeTestRule.onNodeWithTag("menuFab").performClick()

    composeTestRule.onNodeWithTag("itemProfile").performClick()
    // Verify that the navigation action was called for PROFILE
    verify(navigationActions).navigateTo(Screen.PROFILE)
    composeTestRule.onNodeWithTag("menuFab").performClick()

    composeTestRule.onNodeWithTag("itemHome").performClick()
    // Verify that everything was closed
    composeTestRule.onNodeWithTag("closingMenuBox").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("closingMenuFab").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("drawerSheetMenu").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemProfile").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemHome").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemNotifications").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("itemLogout").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("menuFab").performClick()

    composeTestRule.onNodeWithTag("itemNotifications").performClick()
    // Verify that the navigation action was called for NOTIFICATIONS
    verify(navigationActions).navigateTo(Screen.NOTIFICATION)
  }
}
