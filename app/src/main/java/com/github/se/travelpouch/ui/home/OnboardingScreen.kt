package com.github.se.travelpouch.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
              description =
                  "Easily organize and keep track of your travel adventures all in one place",
              imageResId =
                  R.drawable.intro_illustration // Replace with your actual drawable resource
              ),
          OnboardingPage(
              title = "Manage Events",
              description =
                  "Never miss a moment. Manage and stay updated on all your travel plans and events",
              imageResId =
                  R.drawable.never_miss_a_moment // Replace with your actual drawable resource
              ),
          OnboardingPage(
              title = "Save Documents",
              description =
                  "Securely store and access your essential travel documents anytime, anywhere.",
              imageResId = R.drawable.secure_storage // Replace with your actual drawable resource
              ),
          OnboardingPage(
              title = "Your turn",
              description = "Let's get you started by creating your first travel!",
              imageResId = null // Replace with your actual drawable resource
              ))
  var pageIndex by remember { mutableStateOf(0) }

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("OnboardingScreen"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceAround) {

        // Add a progress bar at the top
        ProgressIndicator(pageIndex, onboardingPages.size, Modifier.testTag("ProgressBar"))

        // Display the content of the current page
        OnboardingPageContent(
            onboardingPages[pageIndex].apply {}, Modifier.testTag("OnboardingPageContent"))

        // Add navigation buttons at the bottom
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
                  // TODO add a previous button
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

// Display the content of each onboarding page
@Composable
fun OnboardingPageContent(page: OnboardingPage, modifier: Modifier = Modifier) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top,
      modifier = modifier.fillMaxWidth()) {
        if (page.imageResId != null) {
          Image(
              painter = painterResource(id = page.imageResId),
              contentDescription = null,
              modifier = Modifier.size(300.dp).testTag("OnboardingImage").padding(top = 16.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("OnboardingTitle"))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            page.description,
            fontSize = 16.sp,
            modifier = Modifier.testTag("OnboardingDescription"),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
      }
}

// Progress indicator to show the current page in the onboarding process
@Composable
fun ProgressIndicator(currentPage: Int, totalPages: Int, modifier: Modifier = Modifier) {
  Box(
      contentAlignment = Alignment.Center,
      modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              for (i in 0 until totalPages) {
                Box(
                    modifier =
                        Modifier.padding(horizontal = 4.dp)
                            .size(if (i == currentPage) 12.dp else 8.dp)
                            .background(
                                if (i == currentPage) androidx.compose.ui.graphics.Color.Blue
                                else androidx.compose.ui.graphics.Color.Gray,
                                shape = RoundedCornerShape(50)))
              }
            }
      }
}

// Data class to define each onboarding page
data class OnboardingPage(val title: String, val description: String, val imageResId: Int?)
