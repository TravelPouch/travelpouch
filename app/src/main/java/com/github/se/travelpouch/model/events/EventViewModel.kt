package com.github.se.travelpouch.model.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The class representing the View Model between the view of the timeline and the logic of the event
 * repository.
 *
 * @param repository (EventRepository) : the repository that is used as a logic between Firebase and
 *   events
 */
class EventViewModel(private val repository: EventRepository) : ViewModel() {

  private val events_ = MutableStateFlow<List<Event>>(emptyList())
  val events: StateFlow<List<Event>> = events_.asStateFlow()

  /** The initialisation function of the class */
  fun setIdTravel(travelId: String) {
    repository.setIdTravel({ getEvents() }, travelId)
  }

  /**
   * This function returns a new unused unique identifier.
   *
   * @return (String) : returns a new unused unique identifier
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  /** This function updates the list of events stored on firebase. */
  fun getEvents() {
    repository.getEvents(onSuccess = { events_.value = it }, onFailure = {})
  }

  /**
   * This function adds a new event in Firebase
   *
   * @param event (Event) : a new event to add in Firebase
   */
  fun addEvent(event: Event) {
    repository.addEvent(event = event, onSuccess = { getEvents() }, onFailure = {})
  }

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val firestore = Firebase.firestore
            if (BuildConfig.DEBUG)
              firestore.useEmulator("10.0.2.2", 8080)
            return EventViewModel(EventRepositoryFirebase(firestore)) as T
          }
        }
  }
}
