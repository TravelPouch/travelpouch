package com.github.se.travelpouch.model.events

import com.google.firebase.Timestamp

// we need four collections : one for documents, one for participants, one for travels, one for
// events
data class Event(
    val uid: String,
    val eventType: EventType,
    val date: Timestamp,
    val title: String,
    val description: String,
    val uidParticipant: String?,
    val listUploadedDocuments: Map<String, Int>?
)

enum class EventType {
  START_OF_JOURNEY,
  NEW_PARTICIPANT,
  NEW_DOCUMENT,
  OTHER_EVENT
}
