package com.github.se.travelpouch.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing travel-related data and operations.
 *
 * @property repository The repository used for accessing travel data.
 */
open class ListTravelViewModel(private val repository: TravelRepository) : ViewModel() {
  private val travels_ = MutableStateFlow<List<TravelContainer>>(emptyList())
  val travels: StateFlow<List<TravelContainer>> = travels_.asStateFlow()

  private val selectedTravel_ = MutableStateFlow<TravelContainer?>(null)
  open val selectedTravel: StateFlow<TravelContainer?> = selectedTravel_.asStateFlow()

  private val participants_ = MutableStateFlow<Map<fsUid, UserInfo>>(emptyMap())
  val participants: StateFlow<Map<fsUid, UserInfo>> = participants_.asStateFlow()

  private var lastFetchedTravel: TravelContainer? = null
  private var lastFetchedParticipants: Map<fsUid, Role> = emptyMap()

  init {
    repository.init { getTravels() }
  }

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ListTravelViewModel(TravelRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

  /**
   * Generates a new unique ID.
   *
   * @return A new unique ID.
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  private fun getParticipantFromfsUid(fsUid: fsUid) {
    repository.getParticipantFromfsUid(
        fsUid = fsUid,
        onSuccess = { user ->
          user?.let {
            participants_.value += (fsUid to user)
            Log.d("ListTravelViewModel", "${user.name} is not null")
          } ?: Log.d("ListTravelViewModel", "$fsUid is null")
        },
        onFailure = { Log.e("ListTravelViewModel", "Failed to get participant", it) })
  }

  /** Gets all Travel documents. */
  fun getTravels() {
    repository.getTravels(
        onSuccess = { travels_.value = it },
        onFailure = { Log.e("ListTravelViewModel", "Failed to get travels", it) })
  }

  /**
   * Adds a Travel document.
   *
   * @param travel The Travel document to be added.
   */
  fun addTravel(travel: TravelContainer) {
    repository.addTravel(
        travel = travel,
        onSuccess = { getTravels() },
        onFailure = { Log.e("ListTravelViewModel", "Failed to add travel", it) })
  }

  /**
   * Updates a Travel document.
   *
   * @param travel The Travel document to be updated.
   */
  fun updateTravel(travel: TravelContainer) {
    repository.updateTravel(
        travel = travel,
        onSuccess = { getTravels() },
        onFailure = { Log.e("ListTravelViewModel", "Failed to update travel", it) })
  }

  /**
   * Deletes a Travel document.
   *
   * @param id The ID of the Travel document to be deleted.
   */
  fun deleteTravelById(id: String) {
    repository.deleteTravelById(
        id = id,
        onSuccess = { getTravels() },
        onFailure = { Log.e("ListTravelViewModel", "Failed to delete travel", it) })
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
  /**
   * Fetches information for all participants of the selected travel.
   *
   * This function checks if the participants' information for the currently selected travel has
   * already been fetched. If not, it clears the previous participants' data and fetches the
   * information for each participant in the selected travel.
   */
  fun fetchAllParticipantsInfo() {
    val tempTravel = selectedTravel_.value
    val currentParticipants = tempTravel?.allParticipants?.mapKeys { it.key.fsUid } ?: emptyMap()

    if (tempTravel != lastFetchedTravel || currentParticipants != lastFetchedParticipants) {
      lastFetchedTravel = tempTravel
      lastFetchedParticipants = currentParticipants

      tempTravel?.allParticipants?.keys?.let { participantKeys ->
        viewModelScope.launch {
          participants_.value = emptyMap() // Clear previous participants
          participantKeys.forEach { participant ->
            launch { getParticipantFromfsUid(participant.fsUid) }
          }
        }
      }
    } else {
      Log.d("ListTravelViewModel", "No need to fetch participants, already fetched")
      Log.d(
          "ListTravelViewModel",
          "lastFetchedTravel: $lastFetchedTravel and tempTravel: $tempTravel")
      Log.d(
          "ListTravelViewModel",
          "lastFetchedParticipants: $lastFetchedParticipants and currentParticipants: $currentParticipants")
    }
  }
}
