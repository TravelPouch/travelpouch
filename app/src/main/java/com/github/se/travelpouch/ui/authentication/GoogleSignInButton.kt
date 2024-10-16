package com.github.se.travelpouch.ui.authentication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.travelpouch.R

/**
 * A composable function that creates a button for signing in with Google.
 *
 * @param onSignInClick A lambda function to be called when the button is clicked.
 */
@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth().padding(8.dp).height(48.dp).testTag("loginButtonRow")) {
        Button(
            onClick = onSignInClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Color.LightGray),
            modifier = Modifier.fillMaxWidth().testTag("loginButton")) {
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = "Google Logo",
                  modifier = Modifier.size(30.dp).padding(end = 8.dp).testTag("googleLogo"))
              Text(
                  text = "Sign in with Google",
                  color = Color.Gray,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Medium)
            }
      }
}
