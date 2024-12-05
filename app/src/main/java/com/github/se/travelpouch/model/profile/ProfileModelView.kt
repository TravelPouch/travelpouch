package com.github.se.travelpouch.model.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The class representing the View Model between the view of the profile screen and the logic of the
 * profile repository.
 *
 * @param repository (ProfileRepository) : the repository that is used as a logic between Firebase
 *   and profiles
 */
@HiltViewModel
class ProfileModelView @Inject constructor(private val repository: ProfileRepository) :
    ViewModel() {

  private val onFailureTag = "ProfileViewModel"

  private val profile_ = MutableStateFlow<Profile>(ErrorProfile.errorProfile)
  val profile: StateFlow<Profile> = profile_.asStateFlow()

  /** The initialisation function of the profile model view. It fetches the profile of the user */
  suspend fun initAfterLogin(onSuccess: () -> Unit) {
    repository.initAfterLogin {
      profile_.value = it
      getProfile()
      onSuccess()
    }
  }

  /** This function fetches the profile information of the user */
  fun getProfile() {
    repository.getProfileElements(
        onSuccess = { profile_.value = it },
        onFailure = { Log.e(onFailureTag, "Failed to get profile", it) })
  }

  fun getFsUidByEmail(email: String, onSuccess: (String?) -> Unit, onFailure: (Exception) -> Unit) {
    repository.getFsUidByEmail(email, onSuccess, onFailure)
  }

  /**
   * This function updates the information of the user.
   *
   * @param profile (Profile) : the new profile to save
   * @param context (Context) : the context of the editing profile screen
   */
  fun updateProfile(profile: Profile, context: Context) {
    repository.updateProfile(
        profile,
        onSuccess = { getProfile() },
        onFailure = {
          Log.e(onFailureTag, "Failed to update profile", it)
          Toast.makeText(context, "An error occurred while updating profile", Toast.LENGTH_SHORT)
              .show()
        })
  }

  /**
   * This function allows us to add a friend to a user.
   *
   * @param email (String) : The email of the friend to add
   * @param onSuccess (() -> Unit) : The function to call when the adding of a friend is successful
   * @param onFailure ((Exception) -> Unit) : The function to call when the adding of a friend
   *   failed
   */
  fun addFriend(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.addFriend(
        email = email,
        userProfile = profile_.value,
        onSuccess = {
          Log.d("Friend added", "Friend addded")
          profile_.value = it
          onSuccess()
        },
        onFailure = {
          Log.d("Friend added", it.message!!)
          onFailure(it)
        })
  }

  fun removeFriend(friendUid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.removeFriend(friendUid, userProfile = profile_.value, onSuccess, onFailure)
  }
}
