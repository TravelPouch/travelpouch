package com.github.se.travelpouch.model

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class ListTravelViewModel(private val repository: TravelRepository) : ViewModel() {
  private val travels_ = MutableStateFlow<List<TravelContainer>>(emptyList())
  val travels: StateFlow<List<TravelContainer>> = travels_.asStateFlow()

  private val selectedTravel_ = MutableStateFlow<TravelContainer?>(null)
  open val selectedTravel: StateFlow<TravelContainer?> = selectedTravel_.asStateFlow()

  init {
    repository.init { getTravels() }
  }

  /**
   * Generates a new unique ID.
   *
   * @return A new unique ID.
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  /** Gets all Travel documents. */
  fun getTravels() {
    repository.getTravels(
        onSuccess = { travels_.value = it },
        onFailure = { e -> Log.e("ListTravelViewModel", "Failed to get travels", e) })
  }

  /**
   * Adds a Travel document.
   *
   * @param travel The Travel document to be added.
   */
  fun addTravel(travel: TravelContainer) {
    repository.addTravel(travel = travel, onSuccess = { getTravels() }, onFailure = {})
  }

  /**
   * Updates a Travel document.
   *
   * @param travel The Travel document to be updated.
   */
  fun updateTravel(travel: TravelContainer) {
    repository.updateTravel(travel = travel, onSuccess = { getTravels() }, onFailure = {})
  }

  /**
   * Deletes a Travel document.
   *
   * @param id The ID of the Travel document to be deleted.
   */
  fun deleteTravelById(id: String) {
    repository.deleteTravelById(id = id, onSuccess = { getTravels() }, onFailure = {})
  }

  /**
   * Selects a Travel document.
   *
   * This function updates the `selectedTravel_` state with the provided `travel` object.
   *
   * @param travel The Travel document to be selected.
   */
  fun selectTravel(travel: TravelContainer) {
    selectedTravel_.value = travel
  }
}
