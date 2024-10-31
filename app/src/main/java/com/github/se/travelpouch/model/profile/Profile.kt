package com.github.se.travelpouch.model.profile

data class Profile(
    val fsUid: String,
    val username: String,
    val email: String,
    val friends: Map<Int, String>?,
    val name: String,
    val userTravelList: List<String>
)

object ErrorProfile {
  val errorProfile = Profile("-1", "", "", null, "error", emptyList())
}
