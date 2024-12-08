package com.github.se.travelpouch.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
  val context = LocalContext.current
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
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("OnboardingScreen"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceAround) {
        OnboardingPageContent(
            onboardingPages[pageIndex].apply {}, Modifier.testTag("OnboardingPageContent"))

        Row(
            modifier = Modifier.fillMaxWidth().testTag("NavigationButtons"),
            horizontalArrangement = Arrangement.SpaceBetween) {
              if (pageIndex < onboardingPages.size - 1) {
                Button(
                    onClick = {
                      profileModelView.profile.value.needsOnboarding = false
                      profileModelView.updateProfile(profileModelView.profile.value, context)
                      navigationActions.navigateTo(Screen.TRAVEL_LIST)
                    },
                    modifier = Modifier.testTag("SkipButton")) {
                      Text("Skip")
                    }
                Button(onClick = { pageIndex++ }, modifier = Modifier.testTag("NextButton")) {
                  Text("Next")
                }
              } else {
                Button(
                    onClick = {
                      profileModelView.profile.value.needsOnboarding = false
                      profileModelView.updateProfile(profileModelView.profile.value, context)
                      navigationActions.navigateTo(Screen.TRAVEL_LIST)
                    },
                    modifier = Modifier.testTag("GetStartedButton")) {
                      Text("Get Started")
                    }
              }
            }
      }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage, modifier: Modifier = Modifier) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = page.imageResId),
            contentDescription = null,
            modifier = Modifier.size(200.dp).testTag("OnboardingImage"))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("OnboardingTitle"))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            page.description,
            fontSize = 16.sp,
            modifier = Modifier.testTag("OnboardingDescription"))
      }
}

// Data class to define each onboarding page
data class OnboardingPage(val title: String, val description: String, val imageResId: Int)
