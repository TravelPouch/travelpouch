package com.github.se.travelpouch.ui.notifications

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.ui.navigation.BottomNavigationMenu
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.TopLevelDestinations

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotificationsScreen(navigationActions: NavigationActions, notificationViewModel: NotificationViewModel, profileModelView: ProfileModelView) {

    profileModelView.getProfile()

    val profile = profileModelView.profile.collectAsState()
    notificationViewModel.loadNotificationsForUser(profile.value.fsUid)
    val notifications by notificationViewModel.notifications.observeAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Notifications",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                ) },
                modifier = Modifier.padding(8.dp)
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Padding to avoid overlap with top bar
            ) {
                // ForEach implementation (not recommended for large lists)
                notifications.forEach { notification ->
                    item {
                        NotificationItem(notification = notification)
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationMenu(
                navigationActions = navigationActions,
                tabList = listOf(
                    TopLevelDestinations.NOTIFICATION, TopLevelDestinations.TRAVELS, TopLevelDestinations.CALENDAR)
            )
        }
    )
}

@Composable
fun NotificationItem(notification: Notification, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(onClick = onClick), // Handle item clicks
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title
            Text(
                text = notification.content.toDisplayString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EA) // A vibrant color for the title
            )
            Spacer(modifier = Modifier.height(4.dp)) // Space between title and message
            // Message
            Text(
                text = notification.content.toDisplayString(),
                fontSize = 14.sp,
                color = Color.Gray // Subtle color for the message
            )
            Spacer(modifier = Modifier.height(4.dp)) // Space between message and timestamp
            // Timestamp
            Text(
                text = notification.timestamp.toString(),
                fontSize = 12.sp,
                color = Color.Gray // Subtle color for the timestamp
            )
        }
    }
}

