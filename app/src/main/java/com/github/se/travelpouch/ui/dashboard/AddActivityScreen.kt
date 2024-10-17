package com.github.se.travelpouch.ui.dashboard

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddActivityScreen(activityModelView: ActivityViewModel) {
  val dateFormat =
      SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        isLenient = false // strict date format
      }

  val gregorianCalendar = GregorianCalendar()

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var dateText by remember { mutableStateOf("") }
  var location by remember { mutableStateOf("Placeholder") }

  val context = LocalContext.current

  val placeholerLocation = Location(0.0, 0.0, Timestamp(0, 0), "name")

  Scaffold(modifier = Modifier.testTag("AddActivityScreen")) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
          OutlinedTextField(
              value = title,
              onValueChange = { title = it },
              label = { Text("Title") },
              placeholder = { Text("Title") },
              modifier = Modifier.testTag("titleField"))

          OutlinedTextField(
              value = description,
              onValueChange = { description = it },
              label = { Text("Description") },
              placeholder = { Text("Description") },
              modifier = Modifier.testTag("descriptionField"))

          OutlinedTextField(
              value = dateText,
              onValueChange = {
                if (it.isDigitsOnly() && it.length <= 8) {
                  dateText = it
                }
              },
              label = { Text("Date") },
              placeholder = { Text("01/01/1970") },
              visualTransformation = DateVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.testTag("dateField"))

          OutlinedTextField(
              value = location,
              onValueChange = {},
              label = { Text("Location") },
              placeholder = { Text("Location") },
              modifier = Modifier.testTag("locationField"))

          Button(
              enabled =
                  location.isNotBlank() &&
                      title.isNotBlank() &&
                      description.isNotBlank() &&
                      dateText.isNotBlank(),
              onClick = {
                try {
                  val finalDate = convertStringToDate(dateText, dateFormat, gregorianCalendar)

                  val activity =
                      Activity(
                          activityModelView.getNewUid(),
                          title,
                          description,
                          placeholerLocation,
                          finalDate,
                          mapOf())

                  activityModelView.addActivity(activity, context)
                } catch (e: java.text.ParseException) {
                  Toast.makeText(
                          context, "Invalid format, date must be DD/MM/YYYY.", Toast.LENGTH_SHORT)
                      .show()
                }
              },
              modifier = Modifier.testTag("saveButton")) {
                Text("Save")
              }
        }
  }
}

fun convertStringToDate(
    stringDate: String,
    dateFormat: SimpleDateFormat,
    gregorianCalendar: GregorianCalendar
): Timestamp {
  val finalDateString =
      stringDate.substring(0, 2) + "/" + stringDate.substring(2, 4) + "/" + stringDate.substring(4)

  val date = dateFormat.parse(finalDateString)
  val calendar =
      gregorianCalendar.apply {
        time = date!!
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
      }

  return Timestamp(calendar.time)
}
