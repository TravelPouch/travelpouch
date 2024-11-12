package com.github.se.travelpouch.ui.authentication

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.travelpouch.FirebaseAuthenticationService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlin.time.Duration
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TestSignInWithEmailAndPassword {
  private lateinit var mockFirebaseAuth: FirebaseAuth

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    mockFirebaseAuth = mock(FirebaseAuth::class.java)
  }

  @Test
  fun testSignInWithEmailAndPasswordWorksCorrectly() =
      runTest(timeout = Duration.INFINITE) {
        val firebaseAuthSerice = FirebaseAuthenticationService(mockFirebaseAuth)
        val task: Task<AuthResult> = mock()
        val mockUser: FirebaseUser = mock()

        whenever(task.isSuccessful).thenReturn(true)
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(anyOrNull(), anyOrNull()))
            .thenReturn(task)
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockUser)

        var onSuccessCalled = false
        var onFailureCalled = false

        firebaseAuthSerice.createUser(
            "email", "password", { onSuccessCalled = true }, { onFailureCalled = true })

        val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
        verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
        onCompleteListenerCaptor.firstValue.onComplete(task)

        assert(onSuccessCalled)
        assert(!onFailureCalled)
      }

  @Test
  fun testSignInWithEmailAndPasswordIfTaskFails() =
      runTest(timeout = Duration.INFINITE) {
        val firebaseAuthSerice = FirebaseAuthenticationService(mockFirebaseAuth)
        val task: Task<AuthResult> = mock()

        var onSuccessCalled = false
        var onFailureCalled = false

        whenever(task.isSuccessful).thenReturn(false)
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(anyOrNull(), anyOrNull()))
            .thenReturn(task)

        firebaseAuthSerice.createUser(
            "email", "password", { onSuccessCalled = true }, { onFailureCalled = true })

        val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
        verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
        onCompleteListenerCaptor.firstValue.onComplete(task)

        assert(!onSuccessCalled)
        assert(onFailureCalled)
      }
}
