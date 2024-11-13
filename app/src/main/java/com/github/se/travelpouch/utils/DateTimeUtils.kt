package com.github.se.travelpouch.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale

/** Utility class for date operations */
class DateTimeUtils(private val format: String = "dd/MM/yyyy") {

  private val gregorianCalendar = GregorianCalendar()

  // SimpleDateFormat for formatting dates
  private val formatter =
      SimpleDateFormat(format, Locale.getDefault()).apply {
        isLenient = false // Use strict date parsing
      }

  private val dateOnlyFormatter =
      SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        isLenient = false // Use strict date parsing
      }

  private val timeOnlyFormatter =
      SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        isLenient = false // Use strict date parsing
      }

  // Function to show the DatePickerDialog
  fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
              // Set the selected date in GregorianCalendar
              gregorianCalendar.set(year, month, dayOfMonth, 0, 0)
              // Format the selected date
              val formattedDate = dateOnlyFormatter.format(gregorianCalendar.time)
              onDateSelected(formattedDate)
            },
            gregorianCalendar.get(GregorianCalendar.YEAR),
            gregorianCalendar.get(GregorianCalendar.MONTH),
            gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH))
        .show()
  }

  // Function to show the TimePickerDialog
  fun showTimePicker(context: Context, onDateSelected: (String) -> Unit) {
    TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
              // Set the selected time in GregorianCalendar
              gregorianCalendar.set(GregorianCalendar.HOUR_OF_DAY, hourOfDay)
              gregorianCalendar.set(GregorianCalendar.MINUTE, minute)
              gregorianCalendar.set(GregorianCalendar.SECOND, 0)

              // Format the selected date and time
              val formattedDate = timeOnlyFormatter.format(gregorianCalendar.time)
              onDateSelected(formattedDate)
            },
            gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY),
            gregorianCalendar.get(GregorianCalendar.MINUTE),
            true)
        .show()
  }

  // Function to convert a formatted date string to a Timestamp
  fun convertStringToTimestamp(stringDate: String): Timestamp? {
    return try {
      // Parse the string date using the date format
      val date = formatter.parse(stringDate) ?: return null

      // Set the date in the GregorianCalendar
      gregorianCalendar.time = date

      // Return the date as a Timestamp
      Timestamp(gregorianCalendar.time)
    } catch (e: Exception) {
      null
    }
  }
}
