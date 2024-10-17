package com.github.se.travelpouch.model.location

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.travelpouch.model.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient

/**
 * ViewModel for managing location data and search queries.
 *
 * @property repository The repository used for location data operations.
 */
class LocationViewModel(val repository: LocationRepository) : ViewModel() {
  private val query_ = MutableStateFlow("")
  val query: StateFlow<String> = query_

  private var locationSuggestions_ = MutableStateFlow(emptyList<Location>())
  val locationSuggestions: StateFlow<List<Location>> = locationSuggestions_

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LocationViewModel(NominatimLocationRepository(OkHttpClient())) as T
          }
        }
  }

  fun setQuery(query: String) {
    query_.value = query

    if (query.isNotEmpty()) {
      repository.search(
          query,
          { locationSuggestions_.value = it },
          { error ->
            // Log the error with additional details for more granularity
            Log.e("LocationViewModel", "Error fetching location suggestions: $error")
          })
    }
  }
}
