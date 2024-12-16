// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.notifications

import com.github.se.travelpouch.model.travels.Role

private const val INVITER_NAME_NOT_BLANK = "Inviter name cannot be blank"
private const val TRAVEL_TITLE_NOT_BLANK = "Travel title cannot be blank"
private const val USER_NAME_NOT_BLANK = "User name cannot be blank"

/** Sealed class representing different types of notification content. */
sealed class NotificationContent {

  /**
   * Data class representing an invitation notification.
   *
   * @property inviterName The name of the person who sent the invitation.
   * @property travelTitle The title of the travel event.
   * @property role The role assigned to the invitee.
   */
  data class InvitationNotification(
      val inviterName: String,
      val travelTitle: String,
      val role: Role
  ) : NotificationContent() {

    init {
      require(inviterName.isNotBlank()) { INVITER_NAME_NOT_BLANK }
      require(travelTitle.isNotBlank()) { TRAVEL_TITLE_NOT_BLANK }
    }

    /**
     * Converts the invitation notification to a display string.
     *
     * @return A string representation of the invitation notification.
     */
    override fun toDisplayString(): String {
      return "$inviterName invited you to join the travel $travelTitle as a ${role.name}."
    }
  }

  /**
   * The content of the notification sent when an invitation to be friend is sent
   *
   * @property userEmail (String) : The email of the sender of the notification
   */
  data class FriendInvitationNotification(
      val userEmail: String,
  ) : NotificationContent() {

    /** The content to display */
    override fun toDisplayString(): String {
      return "$userEmail wants to be your friend"
    }
  }

  /**
   * Data class representing a pending invitation reminder.
   *
   * @property inviterName The name of the person who sent the invitation.
   * @property travelTitle The title of the travel event.
   */
  data class PendingInvitationReminder(val inviterName: String, val travelTitle: String) :
      NotificationContent() {

    init {
      require(inviterName.isNotBlank()) { INVITER_NAME_NOT_BLANK }
      require(travelTitle.isNotBlank()) { TRAVEL_TITLE_NOT_BLANK }
    }

    /**
     * Converts the pending invitation reminder to a display string.
     *
     * @return A string representation of the pending invitation reminder.
     */
    override fun toDisplayString(): String {
      return "You have not yet accepted $inviterName's invitation for the '$travelTitle' trip."
    }
  }

  /**
   * Data class representing a role change notification.
   *
   * @property travelTitle The title of the travel event.
   * @property role The new role assigned to the user.
   */
  data class RoleChangeNotification(val travelTitle: String, val role: Role) :
      NotificationContent() {

    init {
      require(travelTitle.isNotBlank()) { TRAVEL_TITLE_NOT_BLANK }
    }

    /**
     * Converts the role change notification to a display string.
     *
     * @return A string representation of the role change notification.
     */
    override fun toDisplayString(): String {
      return "Your role for the '$travelTitle' trip has been changed to ${role.name}."
    }
  }

  /**
   * Data class representing an invitation response notification.
   *
   * @property userName The name of the user who responded to the invitation.
   * @property travelTitle The title of the travel event.
   * @property accepted Whether the invitation was accepted or declined.
   */
  data class InvitationResponseNotification(
      val userName: String,
      val travelTitle: String,
      val accepted: Boolean
  ) : NotificationContent() {

    init {
      require(userName.isNotBlank()) { USER_NAME_NOT_BLANK }
      require(travelTitle.isNotBlank()) { TRAVEL_TITLE_NOT_BLANK }
    }

    /**
     * Converts the invitation response notification to a display string.
     *
     * @return A string representation of the invitation response notification.
     */
    override fun toDisplayString(): String {
      return if (accepted) {
        "$userName has accepted your invitation to the '$travelTitle' trip."
      } else {
        "$userName has declined your invitation to the '$travelTitle' trip."
      }
    }
  }

  /**
   * The content of the notification sent when a response to a notification friend request that was
   * received
   *
   * @property email (String) : The email of the sender of the email
   * @property accepted (Boolean) : A boolean representing if the request was accepted or declined
   */
  data class FriendInvitationResponseNotification(val email: String, val accepted: Boolean) :
      NotificationContent() {

    /** The content to display */
    override fun toDisplayString(): String {
      return if (accepted) {
        "$email is now your friend."
      } else {
        "$email has declined your friend invitation."
      }
    }
  }

  /**
   * Abstract method to convert notification content to a display string.
   *
   * @return A string representation of the notification content.
   */
  abstract fun toDisplayString(): String
}
