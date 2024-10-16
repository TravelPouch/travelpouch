package com.github.se.travelpouch.model.activity

import com.github.se.travelpouch.model.Location
import com.google.firebase.Timestamp

data class Activity(
    val uid: String,
    val title: String,
    val description: String,
    val location: Location,
    val date: Timestamp,
    val documentsNeeded: Map<String, Int>
)