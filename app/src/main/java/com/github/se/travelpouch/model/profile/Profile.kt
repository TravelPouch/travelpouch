package com.github.se.travelpouch.model.profile

data class Profile(
    val uid: String,
    val username: String,
    val email: String,
    val friends: Map<Int, String>?
)

object ErrorProfile{
    val errorProfile = Profile("-1", "", "", null)
}