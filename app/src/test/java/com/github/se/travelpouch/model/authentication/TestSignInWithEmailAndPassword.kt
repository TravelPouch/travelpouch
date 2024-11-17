package com.github.se.travelpouch.model.authentication

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import junit.framework.TestCase.assertFalse
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TestSignInWithEmailAndPassword {
  private lateinit var mockFirebaseAuth: FirebaseAuth

  @Before
  fun setUp() {
    mockFirebaseAuth = mock(FirebaseAuth::class.java)
  }

  @Test
  fun testSignInWithEmailAndPasswordWorksCorrectly() =
      runTest(timeout = 30.seconds) {
        val firebaseAuthService = FirebaseAuthenticationService(mockFirebaseAuth)
        val task: Task<AuthResult> = mock()
        val mockUser: FirebaseUser = mock()

        whenever(task.isSuccessful).thenReturn(true)
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(anyOrNull(), anyOrNull()))
            .thenReturn(task)
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockUser)

        var onSuccessCalled = false
        var onFailureCalled = false

        firebaseAuthService.createUser(
            "email", "password", { onSuccessCalled = true }, { onFailureCalled = true })

        val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
        verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
        onCompleteListenerCaptor.firstValue.onComplete(task)

        assert(onSuccessCalled)
        assertFalse(onFailureCalled)
      }

  @Test
  fun testSignInWithEmailAndPasswordIfTaskFails() =
      runTest(timeout = 30.seconds) {
        val firebaseAuthService = FirebaseAuthenticationService(mockFirebaseAuth)
        val task: Task<AuthResult> = mock()

        var onSuccessCalled = false
        var onFailureCalled = false

        whenever(task.isSuccessful).thenReturn(false)
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(anyOrNull(), anyOrNull()))
            .thenReturn(task)

        firebaseAuthService.createUser(
            "email", "password", { onSuccessCalled = true }, { onFailureCalled = true })

        val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
        verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
        onCompleteListenerCaptor.firstValue.onComplete(task)

        assertFalse(onSuccessCalled)
        assert(onFailureCalled)
      }

  @Test
  fun testLogInWithEmailAndPasswordWorksCorrectly() =
      runTest(timeout = 30.seconds) {
        val firebaseAuthService = FirebaseAuthenticationService(mockFirebaseAuth)
        val task: Task<AuthResult> = mock()
        val mockUser: FirebaseUser = mock()

        whenever(task.isSuccessful).thenReturn(true)
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(anyOrNull(), anyOrNull()))
            .thenReturn(task)
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockUser)

        var onSuccessCalled = false
        var onFailureCalled = false

        firebaseAuthService.login(
            "email", "password", { onSuccessCalled = true }, { onFailureCalled = true })

        val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
        verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
        onCompleteListenerCaptor.firstValue.onComplete(task)

        assert(onSuccessCalled)
        assertFalse(onFailureCalled)
      }

  @Test
  fun testLogInWithEmailAndPasswordIfTaskFails() =
      runTest(timeout = 30.seconds) {
        val firebaseAuthService = FirebaseAuthenticationService(mockFirebaseAuth)
        val task: Task<AuthResult> = mock()

        var onSuccessCalled = false
        var onFailureCalled = false

        whenever(task.isSuccessful).thenReturn(false)
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(anyOrNull(), anyOrNull()))
            .thenReturn(task)

        firebaseAuthService.login(
            "email", "password", { onSuccessCalled = true }, { onFailureCalled = true })

        val onCompleteListenerCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
        verify(task).addOnCompleteListener(onCompleteListenerCaptor.capture())
        onCompleteListenerCaptor.firstValue.onComplete(task)

        assertFalse(onSuccessCalled)
        assert(onFailureCalled)
      }
}
