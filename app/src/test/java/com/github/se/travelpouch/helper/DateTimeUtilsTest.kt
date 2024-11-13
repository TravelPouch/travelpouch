package com.github.se.travelpouch.helper

import com.github.se.travelpouch.utils.DateTimeUtils
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DateTimeUtilsTest {

  private lateinit var dateTimeUtils: DateTimeUtils

  @Before
  fun setUp() {
    // Initialize DateTimeUtils with the default date format
    dateTimeUtils = DateTimeUtils("dd/MM/yyyy")
  }

  @Test
  fun convertStringToTimestampShouldReturnCorrectTimestampForValidDate() {
    // Given a valid date string
    val validDate = "09/11/2024"

    // When converting to Timestamp
    val timestamp = dateTimeUtils.convertStringToTimestamp(validDate)

    // Then the timestamp should not be null and should match the expected date
    val calendar =
        GregorianCalendar(Locale.getDefault()).apply {
          set(Calendar.YEAR, 2024)
          set(Calendar.MONTH, Calendar.NOVEMBER)
          set(Calendar.DAY_OF_MONTH, 9)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
    val expectedTimestamp = Timestamp(calendar.time)

    assertEquals(expectedTimestamp.seconds, timestamp?.seconds)
  }

  @Test
  fun convertStringToTimestampShouldReturnNullForInvalidDateFormat() {
    // Given an invalid date string
    val invalidDate = "invalid-date"

    // When converting to Timestamp
    val timestamp = dateTimeUtils.convertStringToTimestamp(invalidDate)

    // Then the timestamp should be null
    assertNull(timestamp)
  }

  @Test
  fun convertStringToTimestampShouldReturnNullForIncompleteDate() {
    // Given an incomplete date string
    val incompleteDate = "09/11"

    // When converting to Timestamp
    val timestamp = dateTimeUtils.convertStringToTimestamp(incompleteDate)

    // Then the timestamp should be null
    assertNull(timestamp)
  }

  @Test
  fun convertStringToTimestampShouldReturnNullForEmptyDateString() {
    // Given an empty date string
    val emptyDate = ""

    // When converting to Timestamp
    val timestamp = dateTimeUtils.convertStringToTimestamp(emptyDate)

    // Then the timestamp should be null
    assertNull(timestamp)
  }

  @Test
  fun convertStringToTimestampShouldReturnNullForDateOutOfRange() {
    // Given a date string with an out-of-range date
    val outOfRangeDate = "32/01/2024"

    // When converting to Timestamp
    val timestamp = dateTimeUtils.convertStringToTimestamp(outOfRangeDate)

    // Then the timestamp should be null
    assertNull(timestamp)
  }
}
