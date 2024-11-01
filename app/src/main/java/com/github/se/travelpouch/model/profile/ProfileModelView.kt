package com.github.se.travelpouch.model.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
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
class ProfileModelView(private val repository: ProfileRepository) : ViewModel() {

  private val onFailureTag = "ProfileViewModel"

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileModelView(ProfileRepositoryFirebase(Firebase.firestore)) as T
          }
        }
  }

  private val profile_ = MutableStateFlow<Profile>(ErrorProfile.errorProfile)
  val profile: StateFlow<Profile> = profile_.asStateFlow()

  /** The initialisation function of the profile model view. It fetches the profile of the user */
  init {
    repository.init { getProfile() }
  }

  /** This function fetches the profile information of the user */
  fun getProfile() {
    repository.getProfileElements(
        onSuccess = { profile_.value = it },
        onFailure = { Log.e(onFailureTag, "Failed to get profile", it) })
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
}
