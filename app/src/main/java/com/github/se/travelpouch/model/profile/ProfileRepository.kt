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
   * This function retrieves the Firestore UID associated with the given email.
   *
   * @param email (String) : the email address to search for
   * @param onSuccess ((String?) -> Unit) : the function to apply with the UID when the information
   *   is fetched correctly. The UID can be null if no user is found.
   * @param onFailure ((Exception) -> Unit) : the function to apply when an error occurs while
   *   fetching the UID
   */
  fun getFsUidByEmail(email: String, onSuccess: (String?) -> Unit, onFailure: (Exception) -> Unit)

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
  suspend fun initAfterLogin(onSuccess: (Profile) -> Unit)

  /**
   * This function allows us to add a friend to a user.
   *
   * @param email (String) : The email of the friend to add
   * @param userProfile (Profile) : The profile of the user currently of the app
   * @param onSuccess (() -> Unit) : The function to call when the adding of a friend is successful
   * @param onFailure ((Exception) -> Unit) : The function to call when the adding of a friend
   *   failed
   */
  fun addFriend(
      email: String,
      userProfile: Profile,
      onSuccess: (Profile) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun removeFriend(
      friendFsUid: String,
      userProfile: Profile,
      onSuccess: (Profile) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
