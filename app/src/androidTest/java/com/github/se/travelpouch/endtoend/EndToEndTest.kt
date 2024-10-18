package com.github.se.travelpouch.endtoend

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.github.se.travelpouch.MainActivity
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.TravelRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.location.LocationRepository
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.ui.authentication.SignInScreen
import com.github.se.travelpouch.ui.dashboard.AddActivityScreen
import com.github.se.travelpouch.ui.dashboard.TimelineScreen
import com.github.se.travelpouch.ui.dashboard.TravelActivitiesScreen
import com.github.se.travelpouch.ui.documents.DocumentList
import com.github.se.travelpouch.ui.documents.DocumentPreview
import com.github.se.travelpouch.ui.home.AddTravelScreen
import com.github.se.travelpouch.ui.home.AddTravelScreenTest
import com.github.se.travelpouch.ui.home.TravelListScreen
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Route
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.ui.theme.SampleAppTheme
import com.github.se.travelpouch.ui.travel.EditTravelSettingsScreen
import com.github.se.travelpouch.ui.travel.ParticipantListScreen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class  EndToEndTest {


    @Composable
    fun TravelPouchApp2() {
        val navController = rememberNavController()
        val navigationActions = NavigationActions(navController)
        val listTravelViewModel: ListTravelViewModel = viewModel(factory = ListTravelViewModel.Factory)
        val documentViewModel: DocumentViewModel = viewModel(factory = DocumentViewModel.Factory)
        val activityModelView: ActivityViewModel = viewModel(factory = ActivityViewModel.Factory)
        val eventsViewModel: EventViewModel = viewModel(factory = EventViewModel.Factory)

        NavHost(navController = navController, startDestination = Route.DEFAULT) {
            navigation(
                startDestination = Screen.TRAVEL_LIST,
                route = Route.DEFAULT,
            ) {
                composable(Screen.AUTH) { SignInScreen(navigationActions) }

                composable(Screen.TRAVEL_LIST) { TravelListScreen(navigationActions, listTravelViewModel) }
                composable(Screen.TRAVEL_ACTIVITIES) {
                    TravelActivitiesScreen(navigationActions, activityModelView)
                }
                composable(Screen.ADD_ACTIVITY) { AddActivityScreen(navigationActions, activityModelView) }
                composable(Screen.ADD_TRAVEL) { AddTravelScreen(listTravelViewModel, navigationActions) }
                composable(Screen.EDIT_TRAVEL_SETTINGS) {
                    EditTravelSettingsScreen(listTravelViewModel, navigationActions)
                }
                composable(Screen.PARTICIPANT_LIST) {
                    ParticipantListScreen(listTravelViewModel, navigationActions)
                }
                composable(Screen.DOCUMENT_LIST) {
                    DocumentList(
                        documentViewModel,
                        navigationActions,
                        onNavigateToDocumentPreview = { navigationActions.navigateTo(Screen.DOCUMENT_PREVIEW) })
                }
                composable(Screen.DOCUMENT_PREVIEW) { DocumentPreview(documentViewModel, navigationActions) }
                composable(Screen.TIMELINE) { TimelineScreen(eventsViewModel) }
            }
        }}


@get:Rule
val composeTestRule = createComposeRule()

@Before
fun setUp() {
    }

@Test
fun displayAllComponents() {
    composeTestRule.setContent { TravelPouchApp2()}

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("createTravelFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTravelFab").performClick()

}


}