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
   * @param fsUid (String) : The fsUid of the friend to add
   * @param userProfile (Profile) : The profile of the user currently of the app
   * @param onSuccess (() -> Unit) : The function to call when the adding of a friend is successful
   * @param onFailure ((Exception) -> Unit) : The function to call when the adding of a friend
   *   failed
   */
  fun addFriend(
      fsUid: String,
      userProfile: Profile,
      onSuccess: (Profile) -> Unit,
      onFailure: (Exception) -> Unit
  )

    /**
     * Adds a notification token to the user's profile in the Firestore database.
     *
     * @param token The notification token to be added to the profile.
     * @param user The user identifier to whom the token belongs.
     * @param onSuccess A callback function that is invoked when the token is successfully added.
     * @param onFailure A callback function that is invoked with an Exception if an error occurs during the operation.
     */
    fun addNotificationTokenToProfile(
        token: String,
        user: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )
    
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
  )

  /**
   * This function removes a friend for the user profile, and remove the user profile from the given
   * friend profile.
   *
   * @param friendFsUid (String) : the fsUid of the friend we want to remove from our friend list
   * @param userProfile (Profile) : the profile of the current user
   * @param onSuccess ((Profile) -> Unit) : The function to apply when removing the friend is
   *   successful
   * @param onFailure ((Exception) -> Unit) : The function to call when an error occurred
   */
  fun removeFriend(
      friendFsUid: String,
      userProfile: Profile,
      onSuccess: (Profile) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
