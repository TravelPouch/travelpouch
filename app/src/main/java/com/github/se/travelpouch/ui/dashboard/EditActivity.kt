package com.github.se.travelpouch.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

/**
 * This function displays the screen that allows us to edit an activity
 *
 * @param navigationActions (NavigationActions) : the navigation actions that we use to navigate
 *   between screens
 * @param activityViewModel (ActivityViewModel) : the view model used to manange activities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivity(navigationActions: NavigationActions, activityViewModel: ActivityViewModel) {

  val dateFormat =
      SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        isLenient = false // strict date format
      }

  val gregorianCalendar = GregorianCalendar()
  val context = LocalContext.current

  val selectedActivity = activityViewModel.selectedActivity.collectAsState()

  var title by remember { mutableStateOf(selectedActivity.value!!.title) }
  var description by remember { mutableStateOf(selectedActivity.value!!.description) }
  var location by remember { mutableStateOf(selectedActivity.value!!.location.name) }
  var date by remember {
    mutableStateOf(convertDateToString(selectedActivity.value!!.date.toDate()))
  }

  Scaffold(
      modifier = Modifier.testTag("EditActivityScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Edit Activity", Modifier.testTag("travelTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES) },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              OutlinedTextField(
                  value = title,
                  onValueChange = { title = it },
                  enabled = true,
                  label = { Text("Title") },
                  modifier = Modifier.testTag("titleField"))

              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  enabled = true,
                  label = { Text("Description") },
                  modifier = Modifier.testTag("descriptionField"))

              OutlinedTextField(
                  value = location,
                  onValueChange = {},
                  enabled = true,
                  label = { Text("Location") },
                  modifier = Modifier.testTag("locationField"))

              OutlinedTextField(
                  value = date,
                  onValueChange = {
                    if (it.isDigitsOnly() && it.length <= 8) {
                      date = it
                    }
                  },
                  enabled = true,
                  label = { Text("Date") },
                  visualTransformation = DateVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier.testTag("dateField"))

              Button(
                  enabled =
                      location.isNotBlank() &&
                          title.isNotBlank() &&
                          description.isNotBlank() &&
                          date.isNotBlank(),
                  onClick = {
                    try {
                      val finalDate = convertStringToDate(date, dateFormat, gregorianCalendar)

                      val activity =
                          Activity(
                              selectedActivity.value!!.uid,
                              title,
                              description,
                              selectedActivity.value!!.location,
                              finalDate,
                              mapOf())

                      activityViewModel.updateActivity(activity, context)
                    } catch (e: java.text.ParseException) {
                      Toast.makeText(
                              context,
                              "Invalid format, date must be DD/MM/YYYY.",
                              Toast.LENGTH_SHORT)
                          .show()
                    }
                    navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES)
                  },
                  modifier = Modifier.testTag("saveButton")) {
                    Text("Save")
                  }

              Button(
                  enabled = true,
                  onClick = {
                    activityViewModel.deleteActivityById(selectedActivity.value!!, context)
                    navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES)
                  },
                  modifier = Modifier.testTag("deleteButton")) {
                    Text("Delete")
                  }
            }
      }
}

/**
 * This function converts a date to a String
 *
 * @param date (Date) : the date we want to convert
 * @return (String) : the string obtained from the original date
 */
private fun convertDateToString(date: Date): String {
  val df = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
  return df.format(date)
}
