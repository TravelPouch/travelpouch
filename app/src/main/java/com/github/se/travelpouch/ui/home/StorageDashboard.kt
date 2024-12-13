package com.github.se.travelpouch.ui.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import com.github.se.travelpouch.model.home.StorageDashboardStats
import com.github.se.travelpouch.model.home.StorageDashboardViewModel
import com.github.se.travelpouch.model.home.formatStorageUnit
import com.github.se.travelpouch.ui.navigation.NavigationActions
import kotlin.math.min
import kotlin.math.roundToLong
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDashboard(
    storageDashboardViewModel: StorageDashboardViewModel,
    navigationActions: NavigationActions
) {
  val isLoading by storageDashboardViewModel.isLoading.collectAsState()
  val storageStats by storageDashboardViewModel.storageStats.collectAsState()
  val storageLimitDialogOpened = remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(1500)
    if (storageStats == null) {
      storageDashboardViewModel.updateStorageStats()
    }
  }

  Scaffold(
      modifier = Modifier.testTag("StorageDashboard"),
      topBar = {
        TopAppBar(
            title = { Text("Storage", Modifier.testTag("StorageBar")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
  ) { pd ->
    LazyColumn(Modifier.fillMaxSize().padding(pd).padding(horizontal = 10.dp)) {
      item {
        Column(
            modifier = Modifier.fillMaxSize().animateItem(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
          Row(Modifier.padding(10.dp).align(Alignment.End)) {
            if (storageLimitDialogOpened.value) {
              StorageLimitDialog(
                  onDismissRequest = { storageLimitDialogOpened.value = false },
                  currentLimit = storageStats?.storageLimit ?: 0,
                usedStorage = storageStats?.storageUsed ?: 0,
                  onUpdate = {
                    storageDashboardViewModel.setStorageStats(
                        storageStats!!.copy(storageLimit = it))
                    storageLimitDialogOpened.value = false
                  })
            }
            Card(
                onClick = { storageLimitDialogOpened.value = true },
                modifier = Modifier.testTag("storageLimitCard")) {
                  AnimatedVisibility(!isLoading, enter = fadeIn(), exit = fadeOut()) {
                    Row(modifier = Modifier.padding(8f.dp)) {
                      Text(
                          "Storage limit: ${storageStats?.storageLimitToString() ?: "..."}",
                          modifier = Modifier.testTag("storageLimitCardText"))
                    }
                  }
                }
          }
          Spacer(Modifier.height(10.dp))
          Row(modifier = Modifier.fillMaxWidth(0.6f).align(Alignment.CenterHorizontally)) {
            CircularStorageDiagram(storageStats)
          }
          Spacer(Modifier.height(20.dp))

          AnimatedVisibility(!isLoading, enter = fadeIn(), exit = fadeOut()) {
            Row(Modifier.align(Alignment.Start).padding(10.dp)) {
              Text(
                  "Storage usage by travel",
                  fontSize = 5.em,
                  fontWeight = FontWeight.SemiBold,
                  textAlign = TextAlign.Start,
                  modifier = Modifier.testTag("storageUsageByTravelTitle"))
            }
          }
        }
      }

      items(if (storageStats == null) 0 else 10) { index ->
        TravelCard(index)

        Spacer(Modifier.height(10.dp))
      }
    }
  }
}

@Composable
fun CircularStorageDiagram(
    stats: StorageDashboardStats?,
) {
  val colorScheme = MaterialTheme.colorScheme

  val indefiniteLoadingOffset = remember { Animatable(0f) }
  val currentValue = remember { Animatable(0f) }
  val currentIndicator = remember { Animatable(0f) }
  val currentValueScale = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    if (stats == null) {
      currentValue.animateTo(0.1f, tween(1500, 0, FastOutSlowInEasing))
    }
  }
  LaunchedEffect(stats) {
    if (stats == null) {
      launch { currentValue.animateTo(0.1f, tween(1500, 0, FastOutSlowInEasing)) }
      launch {
        indefiniteLoadingOffset.animateTo(
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart))
      }
      currentIndicator.animateTo(
          targetValue = 0f,
          animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing))
    } else {
      launch {
        currentValueScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing))
      }
      launch {
        indefiniteLoadingOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing))
      }
      launch {
        currentValue.animateTo(
            targetValue = stats.storageUsedRatio(),
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing))
      }
      currentIndicator.animateTo(
          targetValue = stats.storageReclaimableRatio(),
          animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing))
    }
  }

  Box {
    Column(modifier = Modifier.align(Alignment.Center)) {
      Canvas(modifier = Modifier.aspectRatio(1f).fillMaxSize()) {
        // Define the size of the circle and the padding for text
        val size = size.minDimension
        val strokeWidth = size * 0.10f // Stroke width of the arcs
        val margin = 0.05f * size
        val arcSize = Size(width = size - 2 * margin, height = size - 2 * margin)

        drawArc(
            topLeft = Offset(margin, margin),
            color = colorScheme.primaryContainer,
            startAngle = 270f,
            sweepAngle = 360f,
            useCenter = false,
            size = arcSize,
            style = Stroke(strokeWidth, cap = StrokeCap.Round))

        val valueAngle = currentValue.value * 360f
        val indicatorAngle = currentIndicator.value * 360f
        val effectiveIndicatorAngle = min(indicatorAngle, valueAngle)

        drawArc(
            topLeft = Offset(margin, margin),
            color = colorScheme.primary,
            startAngle = 270f + indefiniteLoadingOffset.value,
            sweepAngle = valueAngle,
            useCenter = false,
            size = arcSize,
            style = Stroke(strokeWidth * 0.80f, cap = StrokeCap.Round))

        drawArc(
            topLeft = Offset(margin, margin),
            color = colorScheme.primaryContainer,
            startAngle =
                270f + valueAngle - effectiveIndicatorAngle + indefiniteLoadingOffset.value,
            sweepAngle = effectiveIndicatorAngle,
            useCenter = false,
            size = arcSize,
            style =
                Stroke(
                    strokeWidth * 0.70f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f),
                    cap = StrokeCap.Butt))
      }
    }
    Column(modifier = Modifier.align(Alignment.Center)) {
      Text(
          formatStorageUnit(((stats?.storageUsed ?: 0) * currentValueScale.value).roundToLong()),
          fontSize = 7.em,
          fontWeight = FontWeight.SemiBold,
          textAlign = TextAlign.Center)
      Text(
          "${formatStorageUnit(((stats?.storageAvailable() ?: 0) * currentValueScale.value).roundToLong())} available",
          fontSize = 4.em,
          textAlign = TextAlign.Center)
    }
  }
}

@Composable
fun TravelCard(index: Int) {
  Card(Modifier.fillMaxWidth()) {
    Row(Modifier.padding(8.dp)) {
      Text("Travel $index", Modifier.align(Alignment.CenterVertically))
      Spacer(Modifier.fillMaxWidth().weight(0.1f))
      Text("${(index+1) * 244918795735983 % 27471} MiB", Modifier.align(Alignment.CenterVertically))
      IconButton(onClick = {}, modifier = Modifier.testTag("editLimitButton")) {
        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
      }
    }
  }
}

@Composable
fun StorageLimitDialog(
    onDismissRequest: () -> Unit = {},
    currentLimit: Long,
    usedStorage: Long,
    onUpdate: (Long) -> Unit
) {
  Dialog(onDismissRequest = onDismissRequest) {
    val text = remember { mutableStateOf("") }
    var selectedUnitIndex by remember { mutableIntStateOf(0) }
    val unitOptions = listOf("MiB", "GiB")
    val focusRequester = remember { FocusRequester() }
    val longOrNull = text.value.toLongOrNull()
    val newValue = if (longOrNull != null) longOrNull * (1024L * 1024L shl selectedUnitIndex * 10) else 0
    val validInput = newValue >= usedStorage && longOrNull != null
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Box(
        modifier =
            Modifier.fillMaxWidth(1f)
                .height(250.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .testTag("storageLimitDialogBox")) {
          Column(modifier = Modifier.padding(10.dp).fillMaxSize()) {
            Text(
                "Set storage limit",
                Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold,
                fontSize = 5.em)
            Spacer(Modifier.height(20.dp))
            Text(
                "Current limit: ${formatStorageUnit(currentLimit)}",
                modifier = Modifier.testTag("storageLimitDialogCurrentLimitText"))
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
              TextField(
                  value = text.value,
                  modifier =
                      Modifier.weight(0.5f)
                          .focusRequester(focusRequester)
                          .testTag("storageLimitDialogTextField"),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  onValueChange = { text.value = it },
                  label = { Text("New limit") })
              Spacer(Modifier.width(5.dp))
              SingleChoiceSegmentedButtonRow(Modifier.weight(0.5f)) {
                unitOptions.forEachIndexed { index, label ->
                  SegmentedButton(
                      shape =
                          SegmentedButtonDefaults.itemShape(
                              index = index, count = unitOptions.size),
                      onClick = { selectedUnitIndex = index },
                      selected = index == selectedUnitIndex,
                      label = { Text(label) })
                }
              }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                  val value = text.value.toLongOrNull()
                  if (value != null) {
                    onUpdate(value * (1024L * 1024L shl selectedUnitIndex * 10))
                  }
                  onDismissRequest()
                },
                enabled = validInput,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(10.dp)
                        .testTag("storageLimitDialogSaveButton")) {
                  Text("Update")
                }
          }
        }
  }
}
