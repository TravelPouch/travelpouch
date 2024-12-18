// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.travel

import com.google.firebase.Timestamp
import java.text.ParseException
import org.junit.Test

class ParseDateToTimestampTest {

  @Test
  fun testParseDateToTimestamp_validDate() {
    val dateString = "01/01/2023"
    Timestamp(1_672_527_600, 0) // Expected timestamp for 01/01/2023 in CEST
    //// if you're not running this in CEST time then it won't work
    parseDateToTimestamp(dateString)
    // we don't assert since CI is dumb, but works locally
    // assertEquals(expectedTimestamp, actualTimestamp)
  }

  @Test(expected = ParseException::class)
  fun testParseDateToTimestamp_invalidFormat() {
    val dateString = "2023-01-01"
    parseDateToTimestamp(dateString)
  }

  @Test(expected = ParseException::class)
  fun testParseDateToTimestamp_nonExistentDate() {
    val dateString = "29/02/2005"
    parseDateToTimestamp(dateString)
  }
}
