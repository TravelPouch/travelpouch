package com.github.se.travelpouch.ui.dashboard

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.Alignment
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
import com.github.se.travelpouch.utils.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Date
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

  val context = LocalContext.current

  val selectedActivity = activityViewModel.selectedActivity.collectAsState()

  var title by remember { mutableStateOf(selectedActivity.value!!.title) }
  var description by remember { mutableStateOf(selectedActivity.value!!.description) }
  var location by remember { mutableStateOf(selectedActivity.value!!.location.name) }
  var date by remember {
    mutableStateOf(convertDateToString(selectedActivity.value!!.date.toDate()))
  }

  val dateTimeUtils = DateTimeUtils("dd/MM/yyyy")

  Scaffold(
      modifier = Modifier.testTag("EditActivityScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Edit Activity", Modifier.testTag("travelTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.SWIPER) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              OutlinedTextField(
                  value = title,
                  onValueChange = { title = it },
                  enabled = true,
                  label = { Text("Title") },
                  modifier = Modifier.fillMaxWidth().testTag("titleField"))

              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  enabled = true,
                  label = { Text("Description") },
                  modifier = Modifier.fillMaxWidth().testTag("descriptionField"))

              OutlinedTextField(
                  value = location,
                  onValueChange = {},
                  enabled = true,
                  label = { Text("Location") },
                  modifier = Modifier.fillMaxWidth().testTag("locationField"))

              // Date Input
              OutlinedTextField(
                  value = date,
                  onValueChange = {
                    if (it.isDigitsOnly() && it.length <= 8) {
                      date = it
                    }
                  },
                  enabled = true,
                  label = { Text("Date") },
                  placeholder = { Text("DD/MM/YYYY") },
                  visualTransformation = DateVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier.fillMaxWidth().testTag("dateField"),
                  trailingIcon = {
                    IconButton(
                        onClick = {
                          dateTimeUtils.showDatePicker(context) { selectedDate ->
                            date =
                                selectedDate.replace(
                                    "/", "") // Use the DatePickerDialog to select a date
                          }
                        },
                        modifier = Modifier.testTag("datePickerButton")) {
                          Icon(
                              imageVector = Icons.Default.DateRange,
                              contentDescription = "Select Date")
                        }
                  })

              Button(
                  enabled =
                      location.isNotBlank() &&
                          title.isNotBlank() &&
                          description.isNotBlank() &&
                          date.isNotBlank(),
                  onClick = {

                    // Correctly format dateText from ddMMyyyy to dd/MM/yyyy
                    val formattedDateText =
                        if (date.length == 8) {
                          "${date.substring(0, 2)}/${date.substring(2, 4)}/${date.substring(4, 8)}"
                        } else {
                          date // If the length is incorrect, use it as is (to handle any edge
                          // cases)
                        }

                    val finalDate = dateTimeUtils.convertStringToTimestamp(formattedDateText)

                    if (finalDate == null) {
                      Log.e("EditActivityScreen", "Invalid date or format")
                      Toast.makeText(
                              context,
                              "Invalid date or time format. Please use DD/MM/YYYY or HH:mm.",
                              Toast.LENGTH_SHORT)
                          .show()
                    } else {
                      try {
                        Log.d(
                            "EditActivityScreen",
                            "All inputs are valid, proceeding to add Activity")

                        val activity =
                            Activity(
                                selectedActivity.value!!.uid,
                                title,
                                description,
                                selectedActivity.value!!.location,
                                finalDate,
                                mapOf())

                        activityViewModel.updateActivity(activity, context)
                        navigationActions.navigateTo(Screen.SWIPER)
                      } catch (e: java.text.ParseException) {
                        Toast.makeText(
                                context,
                                "Error editing Activity. Please try again.",
                                Toast.LENGTH_SHORT)
                            .show()
                      }
                    }
                  },
                  modifier = Modifier.testTag("saveButton")) {
                    Text("Save")
                  }

              Button(
                  enabled = true,
                  onClick = {
                    activityViewModel.deleteActivityById(selectedActivity.value!!, context)
                    navigationActions.navigateTo(Screen.SWIPER)
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
