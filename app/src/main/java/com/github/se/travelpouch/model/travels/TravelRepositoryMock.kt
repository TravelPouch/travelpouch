package com.github.se.travelpouch.model.travels

import android.util.Log
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.travelCollection

class TravelRepositoryMock : TravelRepository {

  private var currentUserUid = ""

  override fun getNewUid(): String {
    return TravelContainerMock.generateAutoObjectId()
  }

  override fun getParticipantFromfsUid(
      fsUid: fsUid,
      onSuccess: (Profile?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun checkParticipantExists(
      email: String,
      onSuccess: (Profile?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun initAfterLogin(onSuccess: () -> Unit) {
    Log.d("ENDTOEND-FINAL", "in the mock travel repo")
    currentUserUid = "qwertzuiopasdfghjklyxcvbnm12"
    onSuccess()
  }

  override fun getTravels(
      onSuccess: (List<TravelContainer>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val allTravels = travelCollection.values.toList()
    val travelsOfUsers = allTravels.filter { it.listParticipant.contains(currentUserUid) }
    onSuccess(travelsOfUsers)
  }

  override fun addTravel(
      travel: TravelContainer,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    travelCollection[travel.fsUid] = travel
    onSuccess()
  }

  override fun updateTravel(
      travel: TravelContainer,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    travelCollection[travel.fsUid] = travel
    onSuccess()
  }

  override fun deleteTravelById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    travelCollection.remove(id)
    onSuccess()
  }
}