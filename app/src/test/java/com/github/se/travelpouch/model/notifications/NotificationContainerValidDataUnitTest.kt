package com.github.se.travelpouch.model.notifications

import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainerMock.generateAutoObjectId
import com.github.se.travelpouch.model.TravelContainerMock.generateAutoUserId
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationContainerLastCaseTest {

  @Test
  fun notificationInitialization_ValidData() {
    val notificationUid = generateAutoObjectId()
    val senderUid = generateAutoUserId()
    val receiverUid = generateAutoUserId()
    val travelUid = generateAutoObjectId()
    val content =
        NotificationContent.InvitationNotification("John Doe", "Trip to Paris", Role.PARTICIPANT)
    try {
      val myBeautifulNotification =
          Notification(
              notificationUid = notificationUid,
              senderUid = senderUid,
              receiverUid = receiverUid,
              travelUid = travelUid,
              content = content,
              notificationType = NotificationType.INVITATION)
      assertEquals(notificationUid, myBeautifulNotification.notificationUid)
      assertEquals(senderUid, myBeautifulNotification.senderUid)
      assertEquals(receiverUid, myBeautifulNotification.receiverUid)
      assertEquals(travelUid, myBeautifulNotification.travelUid)
      assertEquals(content, myBeautifulNotification.content)
      assert(true)
    } catch (e: IllegalArgumentException) {
      assert(false)
    }
    assert(true)
  }

  @Test
  fun toMap_withValidData_returnsCorrectMap() {
    val notificationUid = generateAutoObjectId()
    val senderUid = generateAutoUserId()
    val receiverUid = generateAutoUserId()
    val travelUid = generateAutoObjectId()
    val content =
        NotificationContent.InvitationNotification("John Doe", "Trip to Paris", Role.PARTICIPANT)
    val notification =
        Notification(
            notificationUid = notificationUid,
            senderUid = senderUid,
            receiverUid = receiverUid,
            travelUid = travelUid,
            content = content,
            notificationType = NotificationType.INVITATION)

    val map = notification.toMap()

    assertEquals(notificationUid, map["notificationUid"])
    assertEquals(senderUid, map["senderUid"])
    assertEquals(receiverUid, map["receiverUid"])
    assertEquals(travelUid, map["travelUid"])
    assertEquals(content, map["content"])
    assertEquals(NotificationType.INVITATION, map["notificationType"])
    assertEquals(notification.timestamp, map["timestamp"])
    assertEquals(NotificationStatus.UNREAD, map["status"])
  }

  @Test
  fun enumValues_containsAllExpectedValues_notificationType() {
    val expectedValues = listOf("INVITATION", "ACCEPTED", "DECLINED", "ROLE_UPDATE")
    val actualValues = NotificationType.values().map { it.name }
    assertEquals(expectedValues, actualValues)
  }

  @Test
  fun valueOf_withValidName_returnsCorrectEnum() {
    assertEquals(NotificationType.INVITATION, NotificationType.valueOf("INVITATION"))
    assertEquals(NotificationType.ACCEPTED, NotificationType.valueOf("ACCEPTED"))
    assertEquals(NotificationType.DECLINED, NotificationType.valueOf("DECLINED"))
    assertEquals(NotificationType.ROLE_UPDATE, NotificationType.valueOf("ROLE_UPDATE"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_withInvalidName_throwsException() {
    NotificationType.valueOf("INVALID")
  }

  @Test
  fun enumValues_containsAllExpectedValues_notificationStatus() {
    val expectedValues = listOf("READ", "UNREAD")
    val actualValues = NotificationStatus.values().map { it.name }
    assertEquals(expectedValues, actualValues)
  }

  @Test
  fun valueOf_withValidName_returnsCorrectEnum_notificationStatus() {
    assertEquals(NotificationStatus.READ, NotificationStatus.valueOf("READ"))
    assertEquals(NotificationStatus.UNREAD, NotificationStatus.valueOf("UNREAD"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_withInvalidName_throwsException_notificationStatus() {
    NotificationStatus.valueOf("INVALID")
  }
}
