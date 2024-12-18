// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.travels

import com.github.se.travelpouch.di.travelCollection
import com.github.se.travelpouch.model.profile.Profile
import com.google.firebase.firestore.DocumentReference

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

  override fun getTravelById(
      id: String,
      onSuccess: (TravelContainer?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun addTravel(
      travel: TravelContainer,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      documentReference: DocumentReference
  ) {
    travelCollection[travel.fsUid] = travel
    onSuccess()
  }

  override fun updateTravel(
      travel: TravelContainer,
      modeOfUpdate: TravelRepository.UpdateMode,
      fsUidOfAddedParticipant: String?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      eventDocumentReference: DocumentReference?
  ) {
    travelCollection[travel.fsUid] = travel
    onSuccess()
  }

  override fun deleteTravelById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    com.github.se.travelpouch.di.travelCollection.remove(id)
    onSuccess()
  }
}
