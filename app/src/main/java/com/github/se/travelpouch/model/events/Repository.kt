package com.github.se.travelpouch.model.events

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
   * This function returns an unused unique identifier for a new event.
   *
   * @return (String) : an unused unique identifier
   */
  fun getNewUid(): String

  /**
   * This function adds an event to the collection of events in Firebase.
   *
   * @param event (Event) : the event we want to add on Firebase
   * @param onSuccess (() -> Unit) : the function called when the event is correctly added to the
   *   database
   * @param onFailure ((Exception) -> Unit) : the function called when an error occurs during the
   *   adding an event to the database
   */
  fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * The initialisation function of the repository.
   *
   * @param (() -> Unit) : the function to apply when the authentication goes without a trouble
   */
  fun initAfterTravelAccess(onSuccess: () -> Unit, travelId: String)
}
