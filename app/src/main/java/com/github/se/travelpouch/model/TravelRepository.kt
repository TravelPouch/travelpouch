package com.github.se.travelpouch.model

import com.github.se.travelpouch.model.profile.Profile

interface TravelRepository {

  fun getNewUid(): String

  fun getParticipantFromfsUid(
      fsUid: fsUid,
      onSuccess: (Profile?) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun checkParticipantExists(
      email: String,
      onSuccess: (Profile?) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun init(onSuccess: () -> Unit)

  fun getTravels(onSuccess: (List<TravelContainer>) -> Unit, onFailure: (Exception) -> Unit)

  fun addTravel(travel: TravelContainer, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun updateTravel(travel: TravelContainer, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteTravelById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
