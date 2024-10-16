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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityModelView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddActivity(activityModelView: ActivityModelView){

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Placeholder") }

    val context = LocalContext.current

    val placeholerLocation = Location(
        0.0,
        0.0,
        Timestamp(0, 0),
        "name"
    )

    Scaffold {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("Title") }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Description") }
            )

            OutlinedTextField(
                value = dateText,
                onValueChange = {
                    if(it.isDigitsOnly() && it.length <= 8){
                        dateText = it
                    }
                },
                label = { Text("Date") },
                placeholder = { Text("01/01/1970") },
                visualTransformation = DateVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = location,
                onValueChange = {  },
                label = { Text("Location") },
                placeholder = { Text("Location") }
            )

            Button(
                enabled = location.isNotBlank() && title.isNotBlank() && description.isNotBlank()
                        && dateText.isNotBlank(),
                onClick = {
                    try{

                        val dateFormat =
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                isLenient = false // strict date format
                            }
                        val date = dateFormat.parse(dateText)
                        val calendar =
                            GregorianCalendar().apply {
                                time = date!!
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }
                        val finalDate = Timestamp(calendar.time)

                        val activity = Activity(
                            activityModelView.getNewUid(),
                            title,
                            description,
                            placeholerLocation,
                            finalDate,
                            mapOf()
                        )

                        activityModelView.addActivity(activity)



                    }catch (e: java.text.ParseException){
                        Toast.makeText(
                            context, "Invalid format, date must be DD/MM/YYYY.", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            ){
                Text("Save")
            }
        }
    }
}

class DateVisualTransformation: VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val transfomedText = text.toString().mapIndexed { index, c ->
            when(index){
                1 -> "$c/"
                3 -> "$c/"
                else -> c
            }
        }.joinToString("")

        return TransformedText(
            text = AnnotatedString(text = transfomedText),
            offsetMapping = DateOffsetMapping()
        )
    }
}

// 00 00 0000

class DateOffsetMapping: OffsetMapping{
    override fun originalToTransformed(offset: Int): Int {
        return when(offset){
            0 -> 0
            1 -> 1
            2 -> 3
            3 -> 4
            4 -> 6
            5 -> 7
            6 -> 8
            7 -> 9
            8 -> 10
            else -> 10
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        return when(offset){
            0 -> 0
            1 -> 1
            2 -> 2
            3 -> 2
            4 -> 3
            5 -> 4
            6 -> 4
            7 -> 5
            8 -> 6
            9 -> 7
            10 -> 8
            else -> 8
        }
    }

}

