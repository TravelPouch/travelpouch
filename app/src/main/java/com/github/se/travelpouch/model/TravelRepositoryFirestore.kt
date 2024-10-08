package com.github.se.travelpouch.model

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class TravelRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) : TravelRepository {

  private val collectionPath = "travels"

  /**
   * Initializes the repository by adding an authentication state listener. The listener triggers
   * the onSuccess callback if the user is authenticated.
   *
   * @param onSuccess The callback to call if the user is authenticated.
   */
  override fun init(onSuccess: () -> Unit) {
    firebaseAuth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  /**
   * Generates a new unique identifier for a travel document.
   *
   * @return A new unique identifier as a String.
   */
  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  /**
   * Adds a new travel document to the Firestore database.
   *
   * @param travel The travel document to add.
   * @param onSuccess The callback to call if the operation is successful.
   * @param onFailure The callback to call if the operation fails.
   */
  override fun addTravel(
      travel: TravelContainer,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("TravelRepositoryFirestore", "addTravel")
    performFirestoreOperation(
        db.collection(collectionPath).document(travel.fsUid).set(travel), onSuccess, onFailure)
  }

  /**
   * Updates an existing travel document in the Firestore database.
   *
   * @param travel The travel document to update.
   * @param onSuccess The callback to call if the operation is successful.
   * @param onFailure The callback to call if the operation fails.
   */
  override fun updateTravel(
      travel: TravelContainer,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("TravelRepositoryFirestore", "updateTravel")
    performFirestoreOperation(
        db.collection(collectionPath).document(travel.fsUid).set(travel), onSuccess, onFailure)
  }

  /**
   * Deletes a travel document from the Firestore database by its identifier.
   *
   * @param id The identifier of the travel document to delete.
   * @param onSuccess The callback to call if the operation is successful.
   * @param onFailure The callback to call if the operation fails.
   */
  override fun deleteTravelById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    Log.d("TravelRepositoryFirestore", "deleteTravelById")
    performFirestoreOperation(
        db.collection(collectionPath).document(id).delete(), onSuccess, onFailure)
  }

  /**
   * Retrieves all travel documents from the Firestore database.
   *
   * @param onSuccess The callback to call with the list of TravelContainer objects if the operation
   *   is successful.
   * @param onFailure The callback to call with the exception if the operation fails.
   */
  override fun getTravels(
      onSuccess: (List<TravelContainer>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("TravelRepositoryFirestore", "getTravels")
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val travels =
            task.result?.documents?.mapNotNull { document ->
              try {
                documentToTravel(document)
              } catch (e: Exception) {
                Log.e(
                    "TravelRepositoryFirestore", "Error converting document to TravelContainer", e)
                null
              }
            } ?: emptyList()
        onSuccess(travels)
      } else {
        task.exception?.let { e ->
          Log.e("TravelRepositoryFirestore", "Error getting documents", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Performs a Firestore operation and calls the appropriate callback based on the result.
   *
   * @param task The Firestore task to perform.
   * @param onSuccess The callback to call if the operation is successful.
   * @param onFailure The callback to call if the operation fails.
   */
  private fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener {
      if (it.isSuccessful) {
        onSuccess()
      } else {
        it.exception?.let { e ->
          Log.e("TravelRepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Converts a Firestore document to a TravelContainer object.
   *
   * @param document The Firestore document to convert.
   * @return A TravelContainer object if the conversion is successful, or null if any required field
   *   is missing or an error occurs.
   */
  private fun documentToTravel(document: DocumentSnapshot): TravelContainer? {
    return try {
      val fsUid = document.id
      val title = document.getString("title") ?: return null
      val description = document.getString("description") ?: return null
      val startTime = document.getTimestamp("startDate") ?: return null
      val endTime = document.getTimestamp("endDate") ?: return null
      val locationData = document.get("location") as? Map<*, *> ?: return null
      val location =
          Location(
              latitude = locationData["latitude"] as? Double ?: 0.0,
              longitude = locationData["longitude"] as? Double ?: 0.0,
              name = locationData["name"] as? String ?: "",
              insertTime = locationData["insertTime"] as? Timestamp ?: Timestamp(0, 0))
      val allAttachmentsData = document.get("allAttachments") as? Map<*, *> ?: return null
      val allAttachments =
          allAttachmentsData.map { (key, value) -> key as String to value as String }.toMap()
      val allParticipantsData = document.get("allParticipants") as? Map<*, *> ?: return null
      val allParticipants =
          allParticipantsData
              .map { (key, value) -> Participant(key as String) to Role.valueOf(value as String) }
              .toMap()

      TravelContainer(
          fsUid = fsUid,
          title = title,
          description = description,
          startTime = startTime,
          endTime = endTime,
          location = location,
          allAttachments = allAttachments,
          allParticipants = allParticipants)
    } catch (e: Exception) {
      Log.e("TravelRepositoryFirestore", "Error converting document to TravelContainer", e)
      null
    }
  }
}
