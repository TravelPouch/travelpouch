package com.github.se.travelpouch.model.profile

import com.github.se.travelpouch.model.events.Event

interface ProfileRepository {

    fun getProfileElements(onSuccess: (Profile) -> Unit, onFailure: (Exception) -> Unit)

    fun updateProfile(newProfile: Profile, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun init(onSuccess: () -> Unit)

    fun getNewUid(): String
}