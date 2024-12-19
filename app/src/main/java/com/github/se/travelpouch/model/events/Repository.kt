// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.events

import com.google.firebase.firestore.DocumentReference

/**
 * An interface representing the event repository that is used to perform operations on Firebase. It
 * is used to retrieve events and store them.
 */
interface EventRepository {

  /**
   * This function retrieves all the events of a travel from the database. If the operation succeeds
   * a function is applied on the list of events retrieved, otherwise a failure function is called.
   *
   * @param onSuccess (List<Event>) -> Unit): The function called when the retrieving of the events
   *   went successfully.
   * @param onFailure ((Exception) -> Unit): The function called when the retrieving of the events
   *   fails.
   */
  fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * This function returns an unused unique document reference for a new event when we don't know to
   * which travel the event will be added.
   *
   * @param travelId (String) : The travel id to which we have to link the event
   * @return (DocumentReference) : The document reference to the new event
   */
  fun getNewDocumentReferenceForNewTravel(travelId: String): DocumentReference

  /**
   * This function returns an unused unique document reference for a new event when the travel id
   * has being set.
   *
   * @return (DocumentReference) : The document reference to the new event
   */
  fun getNewDocumentReference(): DocumentReference

  /**
   * The initialisation function of the repository.
   *
   * @param (() -> Unit) : the function to apply when the authentication goes without a trouble
   */
  fun setIdTravel(onSuccess: () -> Unit, travelId: String)
}
