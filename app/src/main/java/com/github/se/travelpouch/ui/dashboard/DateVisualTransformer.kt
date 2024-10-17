package com.github.se.travelpouch.ui.dashboard

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class DateVisualTransformation : VisualTransformation {
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

private class DateOffsetMapping : OffsetMapping {
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
