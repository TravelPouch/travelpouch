package com.github.se.travelpouch.model.messages

import com.github.se.travelpouch.model.Role

private const val INVITER_NAME_NOT_BLANK = "Inviter name cannot be blank"
private const val TRAVEL_TITLE_NOT_BLANK = "Travel title cannot be blank"

/** Sealed class representing different types of message content. */
sealed class MessageContent {

  /**
   * Data class representing an invitation message.
   *
   * @property inviterName The name of the person who sent the invitation.
   * @property travelTitle The title of the travel event.
   * @property role The role assigned to the invitee.
   */
  data class InvitationMessage(val inviterName: String, val travelTitle: String, val role: Role) :
      MessageContent() {

    init {
      require(inviterName.isNotBlank()) { INVITER_NAME_NOT_BLANK }
      require(travelTitle.isNotBlank()) { TRAVEL_TITLE_NOT_BLANK }
    }

    /**
     * Converts the invitation message to a display string.
     *
     * @return A string representation of the invitation message.
     */
    override fun toDisplayString(): String {
      return "$inviterName invited you to join the travel $travelTitle as a ${role.name}."
    }
  }

  /**
   * Data class representing a pending invitation reminder.
   *
   * @property inviterName The name of the person who sent the invitation.
   * @property travelTitle The title of the travel event.
   */
  data class PendingInvitationReminder(val inviterName: String, val travelTitle: String) :
      MessageContent() {

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
   * Data class representing a role change message.
   *
   * @property travelTitle The title of the travel event.
   * @property role The new role assigned to the user.
   */
  data class RoleChangeMessage(val travelTitle: String, val role: Role) : MessageContent() {

    init {
      require(travelTitle.isNotBlank()) { TRAVEL_TITLE_NOT_BLANK }
    }

    /**
     * Converts the role change message to a display string.
     *
     * @return A string representation of the role change message.
     */
    override fun toDisplayString(): String {
      return "Your role for the '$travelTitle' trip has been changed to ${role.name}."
    }
  }

  /**
   * Data class representing an invitation response message.
   *
   * @property userName The name of the user who responded to the invitation.
   * @property travelTitle The title of the travel event.
   * @property accepted Whether the invitation was accepted or declined.
   */
  data class InvitationResponseMessage(
      val userName: String,
      val travelTitle: String,
      val accepted: Boolean
  ) : MessageContent() {

    init {
      require(userName.isNotBlank()) { "User name cannot be blank" }
      require(travelTitle.isNotBlank()) { TRAVEL_TITLE_NOT_BLANK }
    }

    /**
     * Converts the invitation response message to a display string.
     *
     * @return A string representation of the invitation response message.
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
   * Abstract method to convert message content to a display string.
   *
   * @return A string representation of the message content.
   */
  abstract fun toDisplayString(): String
}
