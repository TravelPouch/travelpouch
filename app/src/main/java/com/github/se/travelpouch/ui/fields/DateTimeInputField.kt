package com.github.se.travelpouch.ui.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun DateTimeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    visualTransformation: VisualTransformation,
    keyboardType: KeyboardType,
    onDatePickerClick: () -> Unit,
    onTimePickerClick: () -> Unit,
    isTime: Boolean = false // Flag to distinguish between date and time
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      enabled = true,
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      visualTransformation = visualTransformation,
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
      modifier = Modifier.fillMaxWidth().testTag("${label.toLowerCase()}Field"),
      trailingIcon = {
        IconButton(
            onClick = {
              if (isTime) {
                onTimePickerClick() // Show time picker
              } else {
                onDatePickerClick() // Show date picker
              }
            },
            modifier = Modifier.testTag("${label.toLowerCase()}PickerButton")) {
              Icon(
                  imageVector = if (isTime) Icons.Filled.AccessTime else Icons.Default.DateRange,
                  contentDescription = "Select $label")
            }
      })
}
