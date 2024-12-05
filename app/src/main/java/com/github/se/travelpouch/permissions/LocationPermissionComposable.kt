package com.github.se.travelpouch.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Composable that requests location permissions and reacts to the permission state.
 *
 * @param onPermissionGranted Callback to execute when permissions are granted.
 * @param onPermissionDenied Callback to execute when permissions are denied.
 */
@Composable
fun LocationPermissionComposable(onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit) {
  val context = LocalContext.current

  // Mutable state to track permissions status
  var permissionsGranted by remember { mutableStateOf(false) }

  // ActivityResultLauncher to request permissions
  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Check the result of each permission request
            permissionsGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
          }

  // Check permissions on each use or launch of a feature
  LaunchedEffect(Unit) {
    val fineLocationGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    val coarseLocationGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    if (fineLocationGranted || coarseLocationGranted) {
      permissionsGranted = true
    } else {
      permissionsGranted = false
      permissionLauncher.launch(
          arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }

  // React to the permission state
  if (permissionsGranted) {
    onPermissionGranted()
  } else {
    onPermissionDenied()
  }
}
