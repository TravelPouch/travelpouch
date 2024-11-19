package com.github.se.travelpouch.ui.notification

import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationContent
import com.github.se.travelpouch.model.notifications.NotificationType
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainerMock.generateAutoObjectId
import com.github.se.travelpouch.model.travels.TravelContainerMock.generateAutoUserId
import com.github.se.travelpouch.ui.notifications.categorizeNotifications
import com.github.se.travelpouch.ui.notifications.isLastMonth
import com.github.se.travelpouch.ui.notifications.isLastWeek
import com.github.se.travelpouch.ui.notifications.isLastYear
import com.github.se.travelpouch.ui.notifications.isThisWeek
import com.google.firebase.Timestamp
import java.util.Calendar
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class NotificationCategorizationTests {

  val senderUid = generateAutoUserId()
  val receiverUid = generateAutoUserId()
  val notificationUid = generateAutoObjectId()
  val travel1Uid = generateAutoObjectId()
  val content1 =
      NotificationContent.InvitationNotification("John Doe", "Trip to Paris", Role.PARTICIPANT)
  val notificationType1 = NotificationType.INVITATION

  @Test
  fun categorizeNotifications_thisWeek() {
    val now = Timestamp.now()
    val notifications =
        listOf(
            Notification(
                notificationUid = notificationUid,
                senderUid = senderUid,
                receiverUid = receiverUid,
                travelUid = travel1Uid,
                content = content1,
                notificationType = notificationType1,
                timestamp = now))
    val result = categorizeNotifications(notifications)
    assertEquals(1, result["This week"]?.size)
  }

  @Test
  fun categorizeNotifications_lastWeek() {
    val now = Timestamp(Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -1) }.time)
    val notifications =
        listOf(
            Notification(
                notificationUid = notificationUid,
                senderUid = senderUid,
                receiverUid = receiverUid,
                travelUid = travel1Uid,
                content = content1,
                notificationType = notificationType1,
                timestamp = now))
    val result = categorizeNotifications(notifications)
    assertEquals(1, result["Last week"]?.size)
  }

  @Test
  fun categorizeNotifications_lastMonth() {
    val now = Timestamp(Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time)
    val notifications =
        listOf(
            Notification(
                notificationUid = notificationUid,
                senderUid = senderUid,
                receiverUid = receiverUid,
                travelUid = travel1Uid,
                content = content1,
                notificationType = notificationType1,
                timestamp = now))
    val result = categorizeNotifications(notifications)
    assertEquals(1, result["Last month"]?.size)
  }

  @Test
  fun categorizeNotifications_lastYear() {
    val now = Timestamp(Calendar.getInstance().apply { add(Calendar.YEAR, -1) }.time)
    val notifications =
        listOf(
            Notification(
                notificationUid = notificationUid,
                senderUid = senderUid,
                receiverUid = receiverUid,
                travelUid = travel1Uid,
                content = content1,
                notificationType = notificationType1,
                timestamp = now))
    val result = categorizeNotifications(notifications)
    assertEquals(1, result["Last year"]?.size)
  }

  @Test
  fun categorizeNotifications_emptyList() {
    val notifications = emptyList<Notification>()
    val result = categorizeNotifications(notifications)
    assertEquals(0, result["This week"]?.size)
    assertEquals(0, result["Last week"]?.size)
    assertEquals(0, result["Last month"]?.size)
    assertEquals(0, result["Last year"]?.size)
  }

    @Test
    fun isThisWeek_test() {
        val now = Calendar.getInstance()
        var date = now.time
        assertTrue(isThisWeek(date, now))

        // Subtract one week from now
        now.add(Calendar.WEEK_OF_YEAR, -1)
        date = now.time
        assertFalse(isThisWeek(date, Calendar.getInstance()))

        // Subtract one more week (now two weeks ago)
        now.add(Calendar.WEEK_OF_YEAR, -1)
        date = now.time
        assertFalse(isThisWeek(date, Calendar.getInstance()))
    }


    @Test
    fun isLastWeek_test() {
        val now = Calendar.getInstance()
        var date = now.time
        assertFalse(isLastWeek(date, now))  // Test for current week

        // Subtract one week from now
        now.add(Calendar.WEEK_OF_YEAR, -1)
        date = now.time
        assertTrue(isLastWeek(date, Calendar.getInstance()))  // Test for last week

        // Subtract one more week (now two weeks ago)
        now.add(Calendar.WEEK_OF_YEAR, -1)
        date = now.time
        assertFalse(isLastWeek(date, Calendar.getInstance()))  // Test for two weeks ago
    }

    @Test
    fun isLastMonth_test() {
        val now = Calendar.getInstance()
        var date = now.time
        assertFalse(isLastMonth(date, now))  // Test for current month

        // Subtract one month from now
        now.add(Calendar.MONTH, -1)
        date = now.time
        assertTrue(isLastMonth(date, Calendar.getInstance()))  // Test for last month

        // Subtract one more month (now two months ago)
        now.add(Calendar.MONTH, -1)
        date = now.time
        assertFalse(isLastMonth(date, Calendar.getInstance()))  // Test for two months ago
    }


    @Test
  fun isLastYear_test() {
    val now = Calendar.getInstance()
    var date = now.time
    assertFalse(isLastYear(date, now))

    now.add(Calendar.YEAR, -1)
    date = now.time
    assertTrue(isLastYear(date, Calendar.getInstance()))

    now.add(Calendar.YEAR, -1)
    date = now.time
    assertFalse(isLastYear(date, Calendar.getInstance()))
  }
}
