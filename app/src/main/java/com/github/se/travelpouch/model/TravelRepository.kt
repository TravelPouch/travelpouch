package com.github.se.travelpouch.model

interface TravelRepository {

  fun getNewUid(): String

  fun getParticipantFromfsUid(
      fsUid: fsUid,
      onSuccess: (UserInfo?) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun init(onSuccess: () -> Unit)

  fun getTravels(onSuccess: (List<TravelContainer>) -> Unit, onFailure: (Exception) -> Unit)

  fun addTravel(travel: TravelContainer, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun updateTravel(travel: TravelContainer, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteTravelById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
