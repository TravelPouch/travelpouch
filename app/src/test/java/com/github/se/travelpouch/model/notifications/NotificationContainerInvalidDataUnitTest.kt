// Import necessary dependencies
package com.github.se.travelpouch.model.notifications

import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.generateAutoId
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Test class for data() cases
@RunWith(Parameterized::class)
class NotificationContainerInvalidDataTest(
    private val notificationUid: String,
    private val senderUid: String,
    private val receiverUid: String,
    private val travelUid: String,
    private val content: NotificationContent.InvitationNotification
) {

  companion object {
    @JvmStatic
    @Parameterized.Parameters(
        name =
            "{index}: Test with notificationUid={0}, senderUid={1}, receiverUid={2}, travelUid={3}, content={4}")
    fun data(): Collection<Array<Any>> {
      return listOf(
          arrayOf(
              "",
              generateAutoId(),
              generateAutoId(),
              generateAutoId(),
              NotificationContent.InvitationNotification("John Doe", "Trip to Paris", Role.PARTICIPANT)),
          arrayOf(
              generateAutoId(),
              "",
              generateAutoId(),
              generateAutoId(),
              NotificationContent.InvitationNotification("John Doe", "Trip to Paris", Role.PARTICIPANT)),
          arrayOf(
              generateAutoId(),
              generateAutoId(),
              "",
              generateAutoId(),
              NotificationContent.InvitationNotification("John Doe", "Trip to Paris", Role.PARTICIPANT)),
          arrayOf(
              generateAutoId(),
              generateAutoId(),
              generateAutoId(),
              "",
              NotificationContent.InvitationNotification("John Doe", "Trip to Paris", Role.PARTICIPANT)),
          arrayOf(
              generateAutoId(),
              "6NU2zp2oGdA34s1Q1q5h",
              "6NU2zp2oGdA34s1Q1q5h",
              generateAutoId(),
              NotificationContent.InvitationNotification("TheName", "Trip to Paris", Role.PARTICIPANT)),
      )
    }
  }

  @Test(expected = IllegalArgumentException::class)
  fun notificationInitialization_withInvalidData_throwsException() {
    Notification(
        notificationUid = notificationUid,
        senderUid = senderUid,
        receiverUid = receiverUid,
        travelUid = travelUid,
        content = content,
        notificationType = NotificationType.INVITATION)
  }
}
