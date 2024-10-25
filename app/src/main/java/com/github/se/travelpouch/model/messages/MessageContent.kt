package com.github.se.travelpouch.model.messages

import com.github.se.travelpouch.model.Role

/**
 * Sealed class representing different types of message content.
 */
sealed class MessageContent {

    /**
     * Data class representing an invitation message.
     *
     * @property inviterName The name of the person who sent the invitation.
     * @property travelTitle The title of the travel event.
     * @property role The role assigned to the invitee.
     */
    data class InvitationMessage(
        val inviterName: String,
        val travelTitle: String,
        val role: Role
    ) : MessageContent() {
        /**
         * Converts the invitation message to a display string.
         *
         * @return A string representation of the invitation message.
         */
        override fun toDisplayString(): String {
            return "$inviterName invited you to join the travel $travelTitle as a ${role.name}."
        }

        /**
         * Generates action buttons for the invitation message.
         *
         * @return A list of action buttons for the invitation message.
         */
        fun toActionButtons(): List<MessageAction> {
            return listOf(
                MessageAction("Accept", ActionType.ACCEPT_INVITATION),
                MessageAction("Decline", ActionType.DECLINE_INVITATION)
            )
        }
    }

    /**
     * Data class representing a pending invitation reminder.
     *
     * @property inviterName The name of the person who sent the invitation.
     * @property travelTitle The title of the travel event.
     */
    data class PendingInvitationReminder(
        val inviterName: String,
        val travelTitle: String
    ) : MessageContent() {

        /**
         * Converts the pending invitation reminder to a display string.
         *
         * @return A string representation of the pending invitation reminder.
         */
        override fun toDisplayString(): String {
            return "You have not yet accepted $inviterName's invitation for the '$travelTitle' trip."
        }

        /**
         * Generates action buttons for the pending invitation reminder.
         *
         * @return A list of action buttons for the pending invitation reminder.
         */
        fun toActionButtons(): List<MessageAction> {
            return listOf(
                MessageAction("Accept", ActionType.ACCEPT_INVITATION),
                MessageAction("Decline", ActionType.DECLINE_INVITATION)
            )
        }
    }

    /**
     * Data class representing a role change message.
     *
     * @property userName The name of the user whose role has changed.
     * @property travelTitle The title of the travel event.
     * @property role The new role assigned to the user.
     */
    data class RoleChangeMessage(
        val userName: String,
        val travelTitle: String,
        val role: Role
    ) : MessageContent() {
        /**
         * Converts the role change message to a display string.
         *
         * @return A string representation of the role change message.
         */
        override fun toDisplayString(): String {
            return "You have added $userName to the '$travelTitle' trip as a $role. The request is being processed."
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

/**
 * Enum class representing different types of actions for messages.
 */
enum class ActionType {
    ACCEPT_INVITATION,
    DECLINE_INVITATION
}

/**
 * Data class representing an action button for a message.
 *
 * @property label The label of the action button.
 * @property actionType The type of action the button represents.
 */
data class MessageAction(
    val label: String,
    val actionType: ActionType
)