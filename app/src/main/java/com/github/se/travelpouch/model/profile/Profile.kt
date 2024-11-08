package com.github.se.travelpouch.model.profile

import androidx.core.util.PatternsCompat
import com.github.se.travelpouch.model.travels.isValidUserUid

/**
 * A data class representing a profile
 *
 * @property fsUid (String) : the unique identifier generated by firebase
 * @property username (String) : the username of the user
 * @property email (String) : the email of the user
 * @property friends (Map<Int, String>?) : the list of the friends of the user
 * @property name (String) : the name of the user
 * @property userTravelList (List<String>) : the list of travels where the user is participating or
 *   participated
 */
data class Profile(
    val fsUid: String,
    val username: String,
    val email: String,
    val friends: Map<Int, String>?,
    val name: String,
    var userTravelList: List<String>
) {
  init {
    require(fsUid.isNotBlank() && isValidUserUid(fsUid)) { "Invalid fsUid" }
    require(isValidEmail(email)) { "Invalid email" }
    require(name.isNotBlank()) { "Name cannot be blank" }
    require(username.isNotBlank()) { "username cannot be blank" }
  }
}

/** This profile represents the error profile. It is used when a profile is corrupted */
object ErrorProfile {
  val errorProfile =
      Profile("000errorerrorerrorerrorerror", "error", "error@error.ch", null, "error", emptyList())
}

fun checkProfileValidity(profile: Profile): Boolean {
  return isValidUserUid(profile.fsUid) && isValidEmail(profile.email)
}

fun isValidEmail(email: String): Boolean {
  return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
}
