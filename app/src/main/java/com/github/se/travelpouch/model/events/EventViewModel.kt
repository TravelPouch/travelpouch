// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.events

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
@HiltViewModel
class EventViewModel @Inject constructor(private val repository: EventRepository) : ViewModel() {
  private val events_ = MutableStateFlow<List<Event>>(emptyList())
  val events: StateFlow<List<Event>> = events_.asStateFlow()

  /** The initialisation function of the class */
  fun setIdTravel(travelId: String) {
    repository.setIdTravel({ getEvents() }, travelId)
  }

  /**
   * This function returns an unused unique document reference for a new event when we don't know to
   * which travel the event will be added.
   *
   * @param travelId (String) : The travel id to which we have to link the event
   * @return (DocumentReference) : The document reference to the new event
   */
  fun getNewDocumentReferenceForNewTravel(travelId: String): DocumentReference {
    return repository.getNewDocumentReferenceForNewTravel(travelId)
  }

  /**
   * This function returns an unused unique document reference for a new event when the travel id
   * has being set.
   *
   * @return (DocumentReference) : The document reference to the new event
   */
  fun getNewDocumentReference(): DocumentReference {
    return repository.getNewDocumentReference()
  }

  /** This function updates the list of events stored on firebase. */
  fun getEvents() {
    repository.getEvents(onSuccess = { events_.value = it }, onFailure = {})
  }
}
