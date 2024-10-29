package com.github.se.travelpouch.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigationActions: NavigationActions,
    profileModelView: ProfileModelView
){
    val profile = profileModelView.profile.collectAsState()

    Scaffold(
        modifier = Modifier.testTag("travelActivitiesScreen"),
        topBar = {
            TopAppBar(
                title = { Text("Profile", Modifier.testTag("ProfileBar")) },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.testTag("goBackButton")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) },
                        modifier = Modifier.testTag("settingsButton")) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                    }
                })
        },
    ) { pd ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(pd)
                .testTag("ProfileColumn")
        ){
            OutlinedTextField(
                value = profile.value.email,
                onValueChange = {},
                enabled = false,
                label = { Text("Email") }
            )

            OutlinedTextField(
                value = profile.value.username,
                onValueChange = {},
                enabled = false,
                label = { Text("Username") }
            )

            OutlinedTextField(
                value = "No Friend, sadge :(",
                onValueChange = {},
                enabled = false,
                label = { Text("Friends") }
            )
        }
    }
}