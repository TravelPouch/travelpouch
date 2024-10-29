package com.github.se.travelpouch.model.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.travelpouch.model.activity.ActivityRepositoryFirebase
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileModelView(private val repository: ProfileRepository): ViewModel() {

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

    fun getNewUid(): String{
        return repository.getNewUid()
    }

    init {
        repository.init { getProfile() }
    }

    fun getProfile(){
        repository.getProfileElements(onSuccess = { profile_.value = it },
            onFailure = { Log.e(onFailureTag, "Failed to get profile", it) })
    }

    fun updateProfile(profile: Profile, context: Context){
        repository.updateProfile(profile,
            onSuccess = {
                getProfile()
            },
            onFailure = {
                Log.e(onFailureTag, "Failed to update profile", it)
                Toast.makeText(context, "An error occurred while updating profile", Toast.LENGTH_SHORT).show()
            })
    }
}