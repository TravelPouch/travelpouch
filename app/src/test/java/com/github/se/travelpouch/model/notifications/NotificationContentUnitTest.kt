package com.github.se.travelpouch.model.notifications

import com.github.se.travelpouch.model.travels.Role
import org.junit.Test

class NotificationContentUnitTest {

  private lateinit var notificationContent: NotificationContent

  @Test
  fun invitationNotification_toDisplayString() {
    notificationContent =
        NotificationContent.InvitationNotification("Alice", "Trip to Paris", Role.PARTICIPANT)
    assert(
        notificationContent.toDisplayString() ==
            "Alice invited you to join the travel Trip to Paris as a PARTICIPANT.")
  }

  @Test
  fun invitationNotification_invalidArgument() {
    try {
      notificationContent =
          NotificationContent.InvitationNotification(" ", "Trip to Paris", Role.PARTICIPANT)
      assert(false)
    } catch (e: IllegalArgumentException) {
      assert(e.message == "Inviter name cannot be blank")
      assert(true)
    }

    try {
      notificationContent =
          NotificationContent.InvitationNotification("Alice", " ", Role.PARTICIPANT)
      assert(false)
    } catch (e: IllegalArgumentException) {
      assert(e.message == "Travel title cannot be blank")
      assert(true)
    }
  }

  @Test
  fun pendingInvitationReminder_toDisplayString() {
    notificationContent = NotificationContent.PendingInvitationReminder("Alice", "Trip to Paris")
    assert(
        notificationContent.toDisplayString() ==
            "You have not yet accepted Alice's invitation for the 'Trip to Paris' trip.")
  }

  @Test
  fun pendingInvitationReminder_invalidArgument() {
    try {
      notificationContent = NotificationContent.PendingInvitationReminder(" ", "Trip to Paris")
      assert(false)
    } catch (e: IllegalArgumentException) {
      assert(e.message == "Inviter name cannot be blank")
      assert(true)
    }

    try {
      notificationContent = NotificationContent.PendingInvitationReminder("Alice", " ")
      assert(false)
    } catch (e: IllegalArgumentException) {
      assert(e.message == "Travel title cannot be blank")
      assert(true)
    }
  }

  @Test
  fun roleChangeNotification_toDisplayString() {
    notificationContent = NotificationContent.RoleChangeNotification("Trip to Paris", Role.OWNER)
    assert(
        notificationContent.toDisplayString() ==
            "Your role for the 'Trip to Paris' trip has been changed to OWNER.")
  }

  @Test
  fun roleChangeNotification_invalidArgument() {
    try {
      notificationContent = NotificationContent.RoleChangeNotification(" ", Role.OWNER)
      assert(false)
    } catch (e: IllegalArgumentException) {
      assert(e.message == "Travel title cannot be blank")
      assert(true)
    }
  }

  @Test
  fun invitationResponseNotification_toDisplayString() {
    notificationContent =
        NotificationContent.InvitationResponseNotification("Alice", "Trip to Paris", true)
    assert(
        notificationContent.toDisplayString() ==
            "Alice has accepted your invitation to the 'Trip to Paris' trip.")

    notificationContent =
        NotificationContent.InvitationResponseNotification("Alice", "Trip to Paris", false)
    assert(
        notificationContent.toDisplayString() ==
            "Alice has declined your invitation to the 'Trip to Paris' trip.")
  }

  @Test
  fun invitationResponseNotification_invalidArgument() {
    try {
      notificationContent =
          NotificationContent.InvitationResponseNotification(" ", "Trip to Paris", true)
      assert(false)
    } catch (e: IllegalArgumentException) {
      assert(e.message == "User name cannot be blank")
      assert(true)
    }

    try {
      notificationContent = NotificationContent.InvitationResponseNotification("Alice", " ", true)
      assert(false)
    } catch (e: IllegalArgumentException) {
      assert(e.message == "Travel title cannot be blank")
      assert(true)
    }
  }

  @Test
  fun assertFriendInvitationDisplaysCorrectly() {
    val notificationContentBis = NotificationContent.FriendInvitationNotification("email")
    val string = notificationContentBis.toDisplayString()
    assert(string == "email wants to be your friend")
  }

  @Test
  fun assertFriendInvitationResponseDisplaysCorrectly() {
    val notificationContentBis =
        NotificationContent.FriendInvitationResponseNotification("email", true)
    val string = notificationContentBis.toDisplayString()
    assert(string == "email is now your friend.")

    val notificationContentBis2 =
        NotificationContent.FriendInvitationResponseNotification("email", false)
    val string2 = notificationContentBis2.toDisplayString()
    assert(string2 == "email has declined your friend invitation.")
  }
}
