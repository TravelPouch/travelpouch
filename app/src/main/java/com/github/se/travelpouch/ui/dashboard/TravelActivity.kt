package com.github.se.travelpouch.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityModelView
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarItemView
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.GregorianCalendar

data class BottomNavigationItem(
    val title: String,
    val icon: ImageVector
)

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelActivitiesScreen(
    navigationActions: NavigationActions? = null,
    activityModelView: ActivityModelView? = null
) {

    val activites_test = listOf(
        Activity(
            "1",
            "title1",
            "description1",
            Location(
                0.0,
                0.0,
                Timestamp(0, 0),
                "lcoation1"
            ),
            Timestamp(0, 0),
            mapOf<String, Int>()
        ),

        Activity(
            "2",
            "title2",
            "description2",
            Location(
                0.0,
                0.0,
                Timestamp(0, 0),
                "lcoation2"
            ),
            Timestamp(0, 0),
            mapOf<String, Int>()
        )
    )
    //val listOfActivities = activityModelView.activities.collectAsState()
    val listOfDestinations = listOf(
        BottomNavigationItem("Activities", Icons.Default.Home),
        BottomNavigationItem("Map", Icons.Default.Place)
    )

    Scaffold(
        modifier = Modifier.testTag("travelActivitiesScreen"),
        topBar = {
            TopAppBar(
                title = { Text("Travel", Modifier.testTag("travelTitle")) },
                navigationIcon = {
                    IconButton(onClick = {}, modifier = Modifier.testTag("goBackButton")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }

                    IconButton(onClick = {}){
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null
                        )
                    }
                })
        },

        bottomBar = {
            NavigationBar {
                listOfDestinations.forEach { NavigationBarItem(
                    onClick = {},
                    icon = { Icon(it.icon, contentDescription = null) },
                    selected = false,
                    label = { Text(it.title) }
                ) }
            }
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    ) { pd ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(pd)
        ) {
            if (activites_test.isEmpty()) {
                item {
                    Text(
                        text = "No activities planned for this trip",
                        modifier = Modifier.testTag("emptyTravel"))
                }
            } else {
                items(activites_test.size) { idx ->
                    ActivityItem(activites_test[idx])
                }
            }
        }
    }
}

@Composable
fun ActivityItem(activity: Activity, onClick: () -> Unit = {}) {
    val calendar = GregorianCalendar()
    calendar.time = activity.date.toDate()

    Card(modifier = Modifier
        .testTag("activityItem")
        .clickable(onClick = onClick)
        .fillMaxSize()) {
        Text(activity.title)
        Text(activity.location.name)
        Text("${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}")
    }
}