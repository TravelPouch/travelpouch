package com.github.se.travelpouch.model.activity

import android.util.Log
import com.github.se.travelpouch.model.Location
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ActivityRepositoryFirebase(private val db: FirebaseFirestore) : ActivityRepository {

    private val collectionPath = "activities"

    /**
     * This function returns an unused unique identifier for a new event.
     *
     * @return (String) : an unused unique identifier
     */
    override fun getNewUid(): String {
        return db.collection(collectionPath).document().id
    }

    /**
     * The initialisation function of the repository.
     *
     * @param (() -> Unit) : the function to apply when the authentication goes without a trouble
     */
    override fun init(onSuccess: () -> Unit) {
        Firebase.auth.addAuthStateListener {
            if (it.currentUser != null) {
                onSuccess()
            }
        }
    }

    override fun updateActivity(
        activity: Activity,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("TravelRepositoryFirestore", "updateTravel")
        performFirestoreOperation(
            db.collection(collectionPath).document(activity.uid).set(activity), onSuccess, onFailure)
    }

    override fun deleteActivityById(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("ActivityRepositoryFirestore", "deleteTravelById")
        performFirestoreOperation(
            db.collection(collectionPath).document(id).delete(), onSuccess, onFailure)
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
    override fun getActivity(onSuccess: (List<Activity>) -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("EventRepository", "getEvents")
        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { result ->
                val events = result?.mapNotNull { documentToEvent(it) } ?: emptyList()
                onSuccess(events)
            }
            .addOnFailureListener { e ->
                Log.e("EventRepository", "Error getting documents", e)
                onFailure(e)
            }
    }

    /**
     * This function adds an event to the collection of events in Firebase.
     *
     * @param event (Event) : the event we want to add on Firebase
     * @param onSuccess (() -> Unit) : the function called when the event is correctly added to the
     *   database
     * @param onFailure ((Exception) -> Unit) : the function called when an error occurs during the
     *   adding an event to the database
     */
    override fun addActivity(
        activity: Activity,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        performFirestoreOperation(
            db.collection(collectionPath).document(activity.uid).set(activity), onSuccess, onFailure)
    }

    /**
     * This function is a helper function that safely performs a Firebase operation. A task has
     * listeners added to it. If the task is successful, we apply onSuccess. Otherwise we perform
     * onFailure.
     *
     * @param task (Task<Void>) : a task to perform
     * @param onSuccess (() -> Unit) : the function called when the event is correctly added to the
     *   database
     * @param onFailure ((Exception) -> Unit) : the function called when an error occurs during the
     *   adding an event to the database
     */
    private fun performFirestoreOperation(
        task: Task<Void>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        task
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e("EventRepositoryFirestore", "Error performing Firestore operation", e)
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
    private fun documentToEvent(document: DocumentSnapshot): Activity? {
        return try {
            val uid = document.id
            val title = document.getString("title")
            val description = document.getString("description")
            val date = document.getTimestamp("date")
            val documents = document.get("documentsNeeded") as? Map<String, Int>
            val locationData = document.get("location") as? Map<*, *>
            val location =
                locationData?.let {
                    Location(
                        latitude = it["latitude"] as? Double ?: 0.0,
                        longitude = it["longitude"] as? Double ?: 0.0,
                        insertTime = it["insertTime"] as? Timestamp ?: Timestamp(0, 0),
                        name = it["name"] as? String ?: "")
                }

            Activity(
                uid = uid,
                title = title!!,
                description = description!!,
                date = date!!,
                documentsNeeded = documents!!,
                location = location!!)
        } catch (e: Exception) {
            Log.e("ActivityRepository", "Error converting document to Activity", e)
            null
        }
    }
}