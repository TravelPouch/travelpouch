package com.github.se.travelpouch.model.profile

/**
 * An interface representing the profile repository that is used to perform operations on Firebase.
 * It is used to retrieve profiles and store them.
 */
interface ProfileRepository {

  /**
   * This function fetches all the information of the user profile.
   *
   * @param onSuccess ((Profile) -> Unit) : the function to apply on the profile when the
   *   information are fetched correctly
   * @param onFailure ((Exception) -> Unit) : the function to apply when an error occurs while
   *   fetching the profile information
   */
  fun getProfileElements(onSuccess: (Profile) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * This function updates the information of the user profile.
   *
   * @param newProfile (Profile) : the new profile to be saved
   * @param onSuccess (() -> Unit) : the function to apply when the profile is updated correctly
   * @param onFailure ((Exception) -> Unit) : the function to apply when an error occurs while
   *   updating the profile information
   */
  fun updateProfile(newProfile: Profile, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /** This is the initialisation function of the profile repository */
  fun init(onSuccess: () -> Unit)
}
