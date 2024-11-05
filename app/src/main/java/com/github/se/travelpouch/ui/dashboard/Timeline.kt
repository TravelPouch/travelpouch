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

/**
 * A data class representing a circle
 *
 * @param radius (Dp) : the radius of circle
 * @param backgroundColor (Color) : the colour of the circle
 */
data class CircleParameters(val radius: Dp, val backgroundColor: Color)

/**
 * A data class representing the line joining to circles
 *
 * @param strokeWidth (Dp) : The width of the line
 * @param brush (Brush) : the way the line is drawn
 */
data class LineParameters(val strokeWidth: Dp, val brush: Brush)

/**
 * The Timeline screen, representing the all the events that occurred during a travel, like the
 * joining of a new participant, the uploading of a new document, ...
 *
 * @param eventsViewModel (EventViewModel) : the view model used to manage the events
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TimelineScreen(eventsViewModel: EventViewModel = viewModel(factory = EventViewModel.Factory)) {

  var itemMoreRightOfScreen = false
  val events = eventsViewModel.events.collectAsState()

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

/**
 * This function describes how a card of the timeline is represented on the screen
 *
 * @param event (Event) : the event to display on the screen
 * @param modifier (Modifier) : the modifier to apply to the card wrapping the event
 */
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

/**
 * This function maps the type of the event to a color.
 *
 * @param type (EventType) : the type of the event
 * @return (Color) : The colour of the event type
 */
fun mapEventTypeToColor(type: EventType): Color {
  return when (type) {
    EventType.OTHER_EVENT -> Color.LightGray.copy(alpha = 0.3f)
    EventType.NEW_DOCUMENT -> Color.Green.copy(alpha = 0.3f)
    EventType.START_OF_JOURNEY -> Color.Blue.copy(alpha = 0.3f)
    EventType.NEW_PARTICIPANT -> Color.Red.copy(alpha = 0.3f)
  }
}

/**
 * This is a wrapper function that displays simultaneously the card of the event, with a circle of
 * the same colour next to it. Moreover, this function prepares the drawing of a gradient line
 * between two events, if the event we are currently drawing is not the last one on the list.
 *
 * @param contentStartOffset (Dp) : The padding from the left of the screen
 * @param spacerBetweenNodes (Dp) : the spacing to apply between nodes
 * @param circleParameters (CircleParameters) : The parameters to apply when drawing a circle for
 *   this wrapper
 * @param lineParameters (LineParameters?) : the parameters to apply when drawing the line between
 *   two nodes. If the node we are considering is the last of a list, this parameter should be set
 *   to null
 * @param content (@Composable BoxScope.(modifier: Modifier) -> Unit) : a composable function
 *   representing what this wrapper should draw in addition tp the circle and the line
 */
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

/** The parameters of the line between two nodes. The colour of the line evolves as a gradient. */
object LineParametersDefaults {

  private val defaultStrokeWidth = 3.dp

  /**
   * This function is a sort of constructor to set the default parameters of the line to be drawn
   * between two nodes
   *
   * @param strokeWidth (Dp) : the width of the line
   * @param startColor (Color) : the starting colour of the line
   * @param endColor (Color) : the ending color of the line
   * @param startY (Float) : the starting y-coordinate when drawing the line
   * @param endY (Float) : the y-coordinate at which the line is ended
   * @return (LineParameters) : the parameters of the lines to be drawn
   */
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

/** The parameters of the circle */
object CircleParametersDefaults {

  private val defaultCircleRadius = 12.dp

  /**
   * This function is a sort of constructor that returns the default parameters of the drawing of
   * the circle.
   *
   * @param radius (Dp) : the default radius of the circle to be drawn
   * @param backgroundColor (Color) : the colour of the circle to be drawn
   * @return (CircleParameters) : the parameters of the circle to be drawn
   */
  fun circleParameters(radius: Dp = defaultCircleRadius, backgroundColor: Color = Cyan) =
      CircleParameters(radius, backgroundColor)
}
