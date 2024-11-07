package com.github.se.travelpouch.model.location

import com.github.se.travelpouch.model.travels.Location

/** Repository for searching locations. */
interface LocationRepository {
  /**
   * Search for locations based on a query.
   *
   * @param query The query to search for.
   * @param onSuccess Callback that is called when the search is successful.
   * @param onFailure Callback that is called when the search fails.
   */
  fun search(query: String, onSuccess: (List<Location>) -> Unit, onFailure: (Exception) -> Unit)
}
