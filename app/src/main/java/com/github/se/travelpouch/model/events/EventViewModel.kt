package com.github.se.travelpouch.model.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EventViewModel(private val repository: EventRepository) : ViewModel() {

  private val events_ = MutableStateFlow<List<Event>>(emptyList())
  val events: StateFlow<List<Event>> = events_.asStateFlow()

  init {
    repository.init { getEvents() }
  }

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  fun getEvents() {
    repository.getEvents(onSuccess = { events_.value = it }, onFailure = {})
  }

  fun addEvent(event: Event) {
    repository.addEvent(event = event, onSuccess = { getEvents() }, onFailure = {})
  }

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventViewModel(EventRepositoryFirebase(Firebase.firestore)) as T
          }
        }
  }
}
