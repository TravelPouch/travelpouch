package com.github.se.travelpouch.ui.endToEnd

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.TravelPouchApp
import com.github.se.travelpouch.ui.theme.SampleAppTheme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Rule
import org.junit.Test


class EndToEndTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun endToEndTest(){

//        val uid = "abcdefghijklmnopqrstuvwxyz12"
//        val customToken = FirebaseAuth.getInstance().
//
//
//        mAuth.signInWithCustomToken(mCustomToken)
//            .addOnCompleteListener(
//                this,
//                OnCompleteListener<AuthResult?> { task ->
//                    if (task.isSuccessful) {
//                        // Sign in success, update UI with the signed-in user's information
//                        Log.d(TAG, "signInWithCustomToken:success")
//                        val user: FirebaseUser = mAuth.getCurrentUser()
//                        updateUI(user)
//                    } else {
//                        // If sign in fails, display a message to the user.
//                        Log.w(TAG, "signInWithCustomToken:failure", task.exception)
//                        Toast.makeText(
//                            this@CustomAuthActivity, "Authentication failed.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        updateUI(null)
//                    }
//                })

        composeTestRule.setContent {
            SampleAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("MainScreenContainer"),
                ) {
                    TravelPouchApp()
                }
            }

        }

        composeTestRule.onNodeWithText("Sign in with Google").performClick()
    }
}