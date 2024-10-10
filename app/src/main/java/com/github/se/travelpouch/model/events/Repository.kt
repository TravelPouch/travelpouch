package com.github.se.travelpouch.model.events

interface EventRepository {
  fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit)

  fun getNewUid(): String

  fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun init(onSuccess: () -> Unit)
}
