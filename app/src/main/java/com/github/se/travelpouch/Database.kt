package com.github.se.travelpouch

import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.travels.TravelContainer

var database =
    mutableMapOf(
        "userslist" to mutableMapOf<String, Profile>(),
        "allTravels" to mutableMapOf<String, TravelContainer>())

var profileCollection = mutableMapOf<String, Profile>()
var travelCollection = mutableMapOf<String, TravelContainer>()
