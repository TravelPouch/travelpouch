package com.github.se.travelpouch.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.travelpouch.R
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen

@Composable
fun OnboardingScreen(navigationActions: NavigationActions, profileModelView: ProfileModelView) {
  val onboardingPages =
      listOf(
          OnboardingPage(
              title = "Track Your Travels",
              description = "Easily organize and keep track of your travel activities.",
              imageResId = R.drawable.google_logo // Replace with your actual drawable resource
              ),
          OnboardingPage(
              title = "Manage Events",
              description = "Stay updated with all your travel-related events.",
              imageResId = R.drawable.google_logo // Replace with your actual drawable resource
              ),
          OnboardingPage(
              title = "Save Documents",
              description = "Store important travel documents securely within the app.",
              imageResId = R.drawable.google_logo // Replace with your actual drawable resource
              ))

  var pageIndex by remember { mutableStateOf(0) }

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceAround) {
        // Display each onboarding page based on index
        OnboardingPageContent(onboardingPages[pageIndex])

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          if (pageIndex < onboardingPages.size - 1) {
            Button(
                onClick = {
                  navigationActions.navigateTo(
                      Screen.TRAVEL_LIST) // TODO change the onboarding value
                }) {
                  Text("Skip")
                }
            Button(onClick = { pageIndex++ }) { Text("Next") }
          } else {
            Button(
                onClick = {
                  profileModelView.profile.value.needsOnboarding = false
                  navigationActions.navigateTo(Screen.TRAVEL_LIST)
                }) {
                  Text("Get Started")
                }
          }
        }
      }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = page.imageResId),
            contentDescription = null,
            modifier = Modifier.size(200.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(page.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(page.description, fontSize = 16.sp)
      }
}

// Data class to define each onboarding page
data class OnboardingPage(val title: String, val description: String, val imageResId: Int)
