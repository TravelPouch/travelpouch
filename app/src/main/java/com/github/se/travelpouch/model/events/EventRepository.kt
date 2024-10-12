package com.github.se.travelpouch.model.events

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class EventRepositoryFirebase(private val db: FirebaseFirestore) : EventRepository {

  private val collectionPath = "events" // want to do subcollections

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
    Log.d("EventRepository", "getEvents")
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val events =
            task.result?.mapNotNull { document -> documentToEvent(document) } ?: emptyList()
        onSuccess(events)
      } else {
        task.exception?.let { e ->
          Log.e("EventRepository", "Error getting documents", e)
          onFailure(e)
        }
      }
    }
  }

  override fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    performFirestoreOperation(
        db.collection(collectionPath).document(event.uid).set(event), onSuccess, onFailure)
  }

  private fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener { result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e ->
          Log.e("TodosRepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  private fun documentToEvent(document: DocumentSnapshot): Event? {
    return try {
      val uid = document.id
      val title = document.getString("title")
      val description = document.getString("description")
      val date = document.getTimestamp("date")
      val documents = document.get("listUploadedDocuments") as? Map<String, Int>
      val eventTypeString = document.getString("eventType")
      val eventType = EventType.valueOf(eventTypeString!!)
      val uidParticipant = document.getString("uidParticipant")

      Event(
          uid = uid,
          title = title!!,
          description = description!!,
          date = date!!,
          eventType = eventType,
          uidParticipant = uidParticipant,
          listUploadedDocuments = documents)
    } catch (e: Exception) {
      Log.e("EventRepository", "Error converting document to Event", e)
      null
    }
  }
}
