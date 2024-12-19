// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.dashboard

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** This class is a date visual transformer for an date entry in a text field */
class DateVisualTransformation : VisualTransformation {
  /**
   * This function describes how the string is transformed.
   *
   * @param text (AnnotatedString) : the string to transform
   * @return (TransformedText) : the resulting text
   */
  override fun filter(text: AnnotatedString): TransformedText {
    val transformedText =
        text
            .toString()
            .mapIndexed { index, c ->
              when (index) {
                1 -> "$c/"
                3 -> "$c/"
                else -> c
              }
            }
            .joinToString("")

    return TransformedText(
        text = AnnotatedString(text = transformedText), offsetMapping = DateOffsetMapping())
  }
}

/**
 * This class describes how the offset works between the original string and the modified string.
 */
private class DateOffsetMapping : OffsetMapping {
  /**
   * This function describes the mapping from an original index to a resulting string.
   *
   * @param offset (Int) : the offset in the original string
   * @return (Int) : the offset in the resulting string
   */
  override fun originalToTransformed(offset: Int): Int {
    return when (offset) {
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

  /**
   * This function describes the mapping from an index of a resulting string to the original string.
   *
   * @param offset (Int) : the offset in the resulting string
   * @return (Int) : the offset in the original string
   */
  override fun transformedToOriginal(offset: Int): Int {
    return when (offset) {
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
