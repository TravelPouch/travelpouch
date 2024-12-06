package com.github.se.travelpouch.ui.home

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDashboard(navigationActions: NavigationActions) {
  Scaffold(
    modifier = Modifier.testTag("StorageDashboard"),
    topBar = {
      TopAppBar(
        title = { Text("Storage", Modifier.testTag("StorageBar")) },
        navigationIcon = {
          IconButton(
            onClick = { navigationActions.navigateTo(Screen.TRAVEL_LIST) },
            modifier = Modifier.testTag("goBackButton")) {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
              contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) },
            modifier = Modifier.testTag("settingsButton")) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
          }
        })
    },
  ) { pd ->
    LazyColumn(Modifier.fillMaxSize().padding(pd).padding(horizontal = 10.dp)) {
      item {
        Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Top,
        ) {
          Row(Modifier.padding(10.dp).align(Alignment.End)) {
            Card(onClick = {}) {
              Row(modifier = Modifier.padding(8f.dp)) {
                Text("Storage limit: 300 MiB")
              }
            }
          }
          Spacer(Modifier.height(10.dp))
          Row(modifier = Modifier.fillMaxWidth(0.6f).align(Alignment.CenterHorizontally)) {
            Box(modifier = Modifier.weight(10f)) {
              Column(modifier = Modifier.align(Alignment.Center)) {
                CircularStorageDiagram(0.18f, 0.40f)
              }
              Column(modifier = Modifier.align(Alignment.Center)) {
                Text("44 MiB",
                  fontSize = 7.em,
                  fontWeight = FontWeight.SemiBold,
                  textAlign = TextAlign.Center)
                Text("284 MiB available",
                  fontSize = 4.em,
                  textAlign = TextAlign.Center)

              }
            }
          }
          Spacer(Modifier.height(20.dp))

          Row(Modifier.align(Alignment.Start).padding(10.dp)) {
            Text("Storage used by travel",
              fontSize = 5.em,
              fontWeight = FontWeight.SemiBold,
              textAlign = TextAlign.Start)
          }
        }
      }
      items(10) { index ->
        Card(Modifier.fillMaxWidth()) {
          Row(Modifier.padding(8.dp)) {
            Text("Travel $index", Modifier.align(Alignment.CenterVertically))
            Spacer(Modifier.fillMaxWidth().weight(0.1f))
            Text("${(index+1) * 244918795735983 % 27471} MiB", Modifier.align(Alignment.CenterVertically))
            IconButton(
              onClick = { },
              modifier = Modifier.testTag("editLimitButton")) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }
          }
        }

        Spacer(Modifier.height(10.dp))
      }

    }
  }
}


@Composable
fun CircularStorageDiagram(
  currentValue: Float,
  indicatorValue: Float,
) {

  val colorScheme = MaterialTheme.colorScheme
  val loadingValue = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    loadingValue.animateTo(1f, tween(1500, 0, FastOutSlowInEasing))
  }

  Canvas(modifier = Modifier.aspectRatio(1f).fillMaxSize()) {
    // Define the size of the circle and the padding for text
    val size = size.minDimension
    val strokeWidth = size * 0.10f // Stroke width of the arcs
    val margin = 0.05f * size
    val arcSize = Size(width = size-2*margin, height = size-2*margin)

    drawArc(
      topLeft = Offset(margin, margin),
      color = colorScheme.primaryContainer,
      startAngle = 270f,
      sweepAngle = 360f,
      useCenter = false,
      size = arcSize,
      style = Stroke(strokeWidth, cap = StrokeCap.Round)
    )

    // Arc for used storage
    val valueAngle = currentValue * 360f
    val indicatorAngle = indicatorValue * 360f

    drawArc(
      topLeft = Offset(margin, margin),
      color = colorScheme.background,
      startAngle = 270f,
      sweepAngle = indicatorAngle,
      useCenter = false,
      size = arcSize,
      style = Stroke(strokeWidth*0.80f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 30f), 0f), cap = StrokeCap.Butt)
    )

    drawArc(
      topLeft = Offset(margin, margin),
      color = colorScheme.primary,
      startAngle = 270f,
      sweepAngle = valueAngle * loadingValue.value,
      useCenter = false,
      size = arcSize,
      style = Stroke(strokeWidth*0.80f, cap = StrokeCap.Round)
    )

  }
}

@Preview(showBackground = true)
@Composable
fun PreviewCircularStorageDiagram() {
  CircularStorageDiagram(currentValue = 0.17f, indicatorValue = 0.60f)
}