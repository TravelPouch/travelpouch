package com.github.se.travelpouch.model.events

import android.util.Log
import com.github.se.travelpouch.model.FirebasePaths
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * The class representing the communication scheme between our project and the database that will
 * allow us to store and retrieve events.
 *
 * @param db (FirebaseFirestore) : the database used to store our events
 */
class EventRepositoryFirebase(private val db: FirebaseFirestore) : EventRepository {

  /** The path to the collection of events of the travel */
  private var collectionPath = ""

  /**
   * This function returns an unused unique document reference for a new event when we don't know to
   * which travel the event will be added.
   *
   * @param travelId (String) : The travel id to which we have to link the event
   * @return (DocumentReference) : The document reference to the new event
   */
  override fun getNewDocumentReferenceForNewTravel(travelId: String): DocumentReference {
    val newId =
        db.collection(FirebasePaths.TravelsSuperCollection)
            .document(travelId)
            .collection(FirebasePaths.events)
            .document()
            .id
    return db.collection(FirebasePaths.TravelsSuperCollection)
        .document(travelId)
        .collection(FirebasePaths.events)
        .document(newId)
  }

  /**
   * This function returns an unused unique document reference for a new event when the travel id
   * has being set.
   *
   * @return (DocumentReference) : The document reference to the new event
   */
  override fun getNewDocumentReference(): DocumentReference {
    val newId = db.collection(collectionPath).document().id
    return db.collection(collectionPath).document(newId)
  }

  /**
   * The initialisation function of the repository.
   *
   * @param (() -> Unit) : the function to apply when the authentication goes without a trouble
   */
  override fun setIdTravel(onSuccess: () -> Unit, travelId: String) {
    val p1 = FirebasePaths.TravelsSuperCollection
    val p2 = FirebasePaths.events
    collectionPath = FirebasePaths.constructPath(p1, travelId, p2)
    onSuccess()
  }

  /**
   * This function retrieves all the events of a travel from the database. If the operation succeeds
   * a function is applied on the list of events retrieved, otherwise a failure function is called.
   *
   * @param onSuccess (List<Event>) -> Unit): The function called when the retrieving of the events
   *   went successfully.
   * @param onFailure ((Exception) -> Unit): The function called when the retrieving of the events
   *   fails.
   */
  override fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
    Log.d("EventRepository", "getEvents")
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { result ->
          val events = result?.mapNotNull { documentToEvent(it) } ?: emptyList()
          onSuccess(events.sortedByDescending { it.date })
        }
        .addOnFailureListener { e ->
          Log.e("EventRepository", "Error getting documents", e)
          onFailure(e)
        }
  }

  /**
   * This function converts a document got from Firebase to an event. It returns null if an error
   * occurs.
   *
   * @param document (DocumentSnapshot) : The document from Firebase
   * @return (Event?) : If the conversion goes without a problem an event is return. Otherwise, null
   *   is returned.
   */
  private fun documentToEvent(document: DocumentSnapshot): Event? {
    return try {
      val uid = document.id
      val title = document.getString("title")
      val description = document.getString("description")
      val date = document.getTimestamp("date")
      val eventTypeString = document.getString("eventType")
      val eventType = EventType.valueOf(eventTypeString!!)

      Event(
          uid = uid,
          title = title!!,
          description = description!!,
          date = date!!,
          eventType = eventType)
    } catch (e: Exception) {
      Log.e("EventRepository", "Error converting document to Event", e)
      null
    }
  }
}
