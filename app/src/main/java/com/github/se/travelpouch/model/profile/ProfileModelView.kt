// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
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

  private var _isTokenUpdated = mutableStateOf(false)
  val isTokenUpdated: Boolean
    get() = _isTokenUpdated.value

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
   * @param fsUid (String) : The uid of the friend to add
   * @param onSuccess (() -> Unit) : The function to call when the adding of a friend is successful
   * @param onFailure ((Exception) -> Unit) : The function to call when the adding of a friend
   *   failed
   */
  fun addFriend(fsUid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.addFriend(
        fsUid = fsUid,
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

  private fun addNotificationTokenToProfile(token: String) {
    repository.addNotificationTokenToProfile(
        token,
        profile_.value.fsUid,
        { Log.d("Notification token added", "Notification token added") },
        { Log.e(onFailureTag, "Failed to add notification token", it) })
  }

  fun updateNotificationTokenIfNeeded(token: String) {
    if (!_isTokenUpdated.value) {
      addNotificationTokenToProfile(token)
      _isTokenUpdated.value = true // Mark the token as updated for this session
    }
  }

  /**
   * This function sends to notification to add a friend
   *
   * @param email (String) : The email of the sender of the notification
   * @param onSuccess ((String) -> Unit) : The function to call when the notification was sent
   * @param onFailure ((Exception) -> Unit) : The function to call when the notification failed to
   *   be sent
   */
  fun sendFriendNotification(
      email: String,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.sendFriendNotification(email, onSuccess, onFailure)
  }

  /**
   * This function removes a friend for the user profile, and remove the user profile from the given
   * friend profile. It calls the repository to do it
   *
   * @param friendUid (String) : the fsUid of the friend we want to remove from our friend list
   * @param onSuccess (() -> Unit) : The function to apply when removing the friend is successful
   * @param onFailure ((Exception) -> Unit) : The function to call when an error occurred
   */
  fun removeFriend(friendUid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.removeFriend(
        friendUid,
        userProfile = profile_.value,
        onSuccess = {
          profile_.value = it
          onSuccess()
        },
        onFailure)
  }
}
