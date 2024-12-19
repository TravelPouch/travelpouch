// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.fields

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.model.travels.Location

@Composable
fun LocationInputField(
    locationViewModel: LocationViewModel,
    locationSuggestions: List<Location?>,
    locationQuery: MutableState<String>, // To control the input text
    showDropdown: Boolean, // To control whether the dropdown is shown
    setShowDropdown: (Boolean) -> Unit, // Function to update showDropdown
    setSelectedLocation: (Location) -> Unit // Function to update selectedLocation
) {
  Box(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = locationQuery.value,
        onValueChange = {
          locationQuery.value = it
          locationViewModel.setQuery(it) // Update the query in the ViewModel
          setShowDropdown(true) // Show the dropdown when the text changes
        },
        label = { Text("Location") },
        placeholder = { Text("Enter an Address or Location") },
        modifier = Modifier.fillMaxWidth().testTag("inputTravelLocation"))

    // Dropdown for location suggestions
    DropdownMenu(
        expanded = showDropdown && locationSuggestions.isNotEmpty(),
        onDismissRequest = { setShowDropdown(false) }, // Hide dropdown when dismissed
        properties = PopupProperties(focusable = false),
        modifier =
            Modifier.fillMaxWidth(1f).heightIn(max = 200.dp).testTag("locationDropdownMenu")) {
          locationSuggestions.filterNotNull().take(3).forEach { location ->
            DropdownMenuItem(
                text = {
                  Text(
                      text = location.name.take(30) + if (location.name.length > 30) "..." else "",
                      maxLines = 1,
                      modifier = Modifier.testTag("suggestionText_${location.name}"))
                },
                onClick = {
                  locationViewModel.setQuery(
                      location.name) // Set query to the selected location name
                  setSelectedLocation(location) // Update the selected location
                  locationQuery.value =
                      location.name // Update the input field with the selected location name
                  setShowDropdown(false) // Close dropdown after selection
                },
                modifier = Modifier.padding(8.dp).testTag("suggestion_${location.name}"))
            HorizontalDivider() // Separate items with a divider
          }

          if (locationSuggestions.size > 3) {
            DropdownMenuItem(
                text = { Text("More...") },
                onClick = { /* Optionally show more results */},
                modifier = Modifier.padding(8.dp).testTag("moreSuggestions"))
          }
        }
  }
}
