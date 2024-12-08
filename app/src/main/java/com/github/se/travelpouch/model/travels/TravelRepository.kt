package com.github.se.travelpouch.model.travels

import com.github.se.travelpouch.model.profile.Profile

interface TravelRepository {

  enum class UpdateMode {
    FIELDS_UPDATE,
    ADD_PARTICIPANT,
    REMOVE_PARTICIPANT
  }

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

  fun initAfterLogin(onSuccess: () -> Unit)

  fun getTravels(onSuccess: (List<TravelContainer>) -> Unit, onFailure: (Exception) -> Unit)

  fun getTravelById(
      id: String,
      onSuccess: (TravelContainer?) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun addTravel(travel: TravelContainer, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun updateTravel(
      travel: TravelContainer,
      modeOfUpdate: UpdateMode,
      fsUidOfAddedParticipant: String?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteTravelById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
