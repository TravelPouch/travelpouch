package com.github.se.travelpouch.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import junit.framework.TestCase.assertEquals
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class NavigationActionsTest {

    private lateinit var navigationDestination: NavDestination
    private lateinit var navHostController: NavHostController
    private lateinit var navigationActions: NavigationActions

    @Before
    fun setUp() {
        navigationDestination = mock(NavDestination::class.java)
        navHostController = mock(NavHostController::class.java)
        navigationActions = NavigationActions(navHostController)
    }


    @Test
    fun navigateToCallsController() {
        navigationActions.navigateTo(Screen.AUTH)
        verify(navHostController).navigate(Screen.AUTH)
    }

    @Test
    fun goBackCallsController() {
        navigationActions.goBack()
        verify(navHostController).popBackStack()
    }

    @Test
    fun currentRouteCallsController() {
        `when`(navHostController.currentDestination).thenReturn(navigationDestination)
        `when`(navigationDestination.route).thenReturn(Route.AUTH)

        // Appeler la méthode currentRoute
        val currentRoute = navigationActions.currentRoute()

        // Vérifier que la route actuelle est bien "Auth"
        assertEquals("Auth", currentRoute)
    }

}