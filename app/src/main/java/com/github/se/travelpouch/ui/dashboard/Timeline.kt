package com.github.se.travelpouch.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.travelpouch.model.events.Event
import com.github.se.travelpouch.model.events.EventType
import com.github.se.travelpouch.model.events.EventViewModel
import java.util.Calendar
import java.util.GregorianCalendar

// credit to the website :
// https://medium.com/proandroiddev/a-step-by-step-guide-to-building-a-timeline-component-with-jetpack-compose-358a596847cb

enum class Paddings(val padding: Dp) {
  SPACER_BETWEEN_NODES(32.dp),
  SPACER_LESS_RIGHT(32.dp),
  SPACER_MORE_RIGHT(96.dp)
}

data class CircleParameters(val radius: Dp, val backgroundColor: Color)

data class LineParameters(val strokeWidth: Dp, val brush: Brush)

@Preview
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TimelineScreen(eventsViewModel: EventViewModel = viewModel(factory = EventViewModel.Factory)) {

  //
  var itemMoreRightOfScreen = false
  val events = eventsViewModel.events.collectAsState()

  //  val events_test =
  //      listOf(
  //          Event("1", EventType.NEW_DOCUMENT, Timestamp(0, 0), "it",
  //              "it", null, null),
  //          Event("2", EventType.START_OF_JOURNEY, Timestamp(0, 0), "it",
  //              "it", null, null),
  //          Event("3", EventType.NEW_PARTICIPANT, Timestamp(0, 0), "it",
  //              "it", null, null),
  //          Event("3", EventType.OTHER_EVENT, Timestamp(0, 0), "it",
  //              "it", null, null))

  Scaffold(
      modifier = Modifier.testTag("timelineScreen"),
  ) {
    if (events.value.isNotEmpty()) {
      LazyColumn(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("timelineColumn"),
          contentPadding = PaddingValues(vertical = 16.dp),
      ) {
        val size = events.value.size

        item {
          Box(
              modifier = Modifier.fillMaxSize().padding(20.dp),
              contentAlignment = Alignment.Center) {
                Text(
                    text = "Your travel Milestone",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("screenTitle"))
              }
        }
        items(size) { index ->
          val color = mapEventTypeToColor(events.value[index].eventType)
          val nextColor =
              if (index < size - 1) mapEventTypeToColor(events.value[index + 1].eventType) else null

          TimelineNode(
              contentStartOffset =
                  if (itemMoreRightOfScreen) {
                    Paddings.SPACER_MORE_RIGHT.padding
                  } else {
                    Paddings.SPACER_LESS_RIGHT.padding
                  },
              spacerBetweenNodes = Paddings.SPACER_BETWEEN_NODES.padding,
              circleParameters = CircleParametersDefaults.circleParameters(backgroundColor = color),
              lineParameters =
                  if (nextColor != null)
                      LineParametersDefaults.linearGradient(
                          startColor = color, endColor = nextColor)
                  else null) { modifier ->
                TimelineItem(events.value[index], modifier)
              }

          itemMoreRightOfScreen = !itemMoreRightOfScreen
        }
      }
    } else {
      Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "Loading...",
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("loadingText"))
      }
    }
  }
}

@Composable
fun TimelineItem(event: Event, modifier: Modifier) {
  Card(
      modifier = modifier.width(250.dp).height(100.dp).testTag("eventCard"),
      colors = CardDefaults.cardColors(containerColor = mapEventTypeToColor(event.eventType))) {
        Text(event.eventType.toString(), modifier = Modifier.testTag("eventType"))
        Text(event.title, modifier = Modifier.testTag("eventTitle"))

        val calendar = GregorianCalendar()
        calendar.time = event.date.toDate()

        Text(
            "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
          calendar.get(
              Calendar.YEAR
          )
      }",
            modifier = Modifier.testTag("eventDate"))
      }
}

fun mapEventTypeToColor(type: EventType): Color {
  return when (type) {
    EventType.OTHER_EVENT -> Color.LightGray.copy(alpha = 0.3f)
    EventType.NEW_DOCUMENT -> Color.Green.copy(alpha = 0.3f)
    EventType.START_OF_JOURNEY -> Color.Blue.copy(alpha = 0.3f)
    EventType.NEW_PARTICIPANT -> Color.Red.copy(alpha = 0.3f)
  }
}

@Composable
fun TimelineNode(
    contentStartOffset: Dp,
    spacerBetweenNodes: Dp,
    circleParameters: CircleParameters,
    lineParameters: LineParameters?,
    content: @Composable BoxScope.(modifier: Modifier) -> Unit
) {
  Box(
      modifier =
          Modifier.wrapContentSize()
              .drawBehind {
                val circleRadiusInPx = circleParameters.radius.toPx()
                drawCircle(
                    color = circleParameters.backgroundColor,
                    radius = circleRadiusInPx,
                    center = Offset(circleRadiusInPx, circleRadiusInPx))

                lineParameters?.let {
                  drawLine(
                      brush = lineParameters.brush,
                      start = Offset(x = circleRadiusInPx, y = circleRadiusInPx * 2),
                      end = Offset(x = circleRadiusInPx, y = this.size.height),
                      strokeWidth = lineParameters.strokeWidth.toPx())
                }
              }
              .testTag("boxContainingEvent")) {
        content(Modifier.padding(start = contentStartOffset, bottom = spacerBetweenNodes))
      }
}

object LineParametersDefaults {

  private val defaultStrokeWidth = 3.dp

  fun linearGradient(
      strokeWidth: Dp = defaultStrokeWidth,
      startColor: Color,
      endColor: Color,
      startY: Float = 0.0f,
      endY: Float = Float.POSITIVE_INFINITY
  ): LineParameters {
    val brush =
        Brush.verticalGradient(colors = listOf(startColor, endColor), startY = startY, endY = endY)
    return LineParameters(strokeWidth, brush)
  }
}

object CircleParametersDefaults {

  private val defaultCircleRadius = 12.dp

  fun circleParameters(radius: Dp = defaultCircleRadius, backgroundColor: Color = Cyan) =
      CircleParameters(radius, backgroundColor)
}
