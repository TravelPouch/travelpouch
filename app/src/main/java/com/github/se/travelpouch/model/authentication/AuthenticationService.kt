package com.github.se.travelpouch.model.authentication

import android.content.ContentValues.TAG
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

interface AuthenticationService {
  fun createUser(
      email: String,
      password: String,
      onSuccess: (FirebaseUser?) -> Unit,
      onFailure: (Task<AuthResult>) -> Unit
  )

  fun login(
      email: String,
      password: String,
      onSuccess: (FirebaseUser?) -> Unit,
      onFailure: (Task<AuthResult>) -> Unit
  )
}

class FirebaseAuthenticationService(private val auth: FirebaseAuth) : AuthenticationService {
  override fun createUser(
      email: String,
      password: String,
      onSuccess: (FirebaseUser?) -> Unit,
      onFailure: (Task<AuthResult>) -> Unit
  ) {
    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
      if (task.isSuccessful) {
        // Sign in success, update UI with the signed-in user's information
        Log.d(TAG, "createUserWithEmail:success")
        val user = auth.currentUser

        onSuccess(user)
      } else {
        // If sign in fails, display a message to the user.
        onFailure(task)
      }
    }
  }

  override fun login(
      email: String,
      password: String,
      onSuccess: (FirebaseUser?) -> Unit,
      onFailure: (Task<AuthResult>) -> Unit
  ) {

    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
      if (task.isSuccessful) {
        // Sign in success, update UI with the signed-in user's information
        Log.d(TAG, "signInWithEmail:success")
        val user = auth.currentUser
        onSuccess(user)
      } else {
        // If sign in fails, display a message to the user.
        Log.w(TAG, "signInWithEmail:failure", task.exception)
        onFailure(task)
      }
    }
  }
}

class MockFirebaseAuthenticationService : AuthenticationService {
  override fun createUser(
      email: String,
      password: String,
      onSuccess: (FirebaseUser?) -> Unit,
      onFailure: (Task<AuthResult>) -> Unit
  ) {
    onSuccess(null)
  }

  override fun login(
      email: String,
      password: String,
      onSuccess: (FirebaseUser?) -> Unit,
      onFailure: (Task<AuthResult>) -> Unit
  ) {
    onSuccess(null)
  }
}
