package com.github.se.travelpouch.model.messages

import com.github.se.travelpouch.model.Role

sealed class MessageContent {

    data class InvitationMessage(
        val inviterName: String,
        val travelTitle: String,
        val role: Role
    ) : MessageContent() {
        override fun toDisplayString(): String {
            return "$inviterName invited you to join the travel $travelTitle as a ${role.name}."
        }

        fun toActionButtons(): List<MessageAction> {
            return listOf(
                MessageAction("Accept", ActionType.ACCEPT_INVITATION),
                MessageAction("Decline", ActionType.DECLINE_INVITATION)
            )
        }
    }


    data class PendingInvitationReminder(
        val inviterName: String,
        val travelTitle: String
    ) : MessageContent() {

        override fun toDisplayString(): String {
            return "You have not yet accepted $inviterName's invitation for the '$travelTitle' trip."
        }

        fun toActionButtons(): List<MessageAction> {
            return listOf(
                MessageAction("Accept", ActionType.ACCEPT_INVITATION),
                MessageAction("Decline", ActionType.DECLINE_INVITATION)
            )
        }
    }

    data class RoleChangeMessage(
        val userName: String,
        val travelTitle: String,
        val role: Role
    ) : MessageContent() {
        override fun toDisplayString(): String {
            return "You have added $userName to the '$travelTitle' trip as a $role. The request is being processed."
        }
    }

    data class InvitationResponseMessage(
        val userName: String,
        val travelTitle: String,
        val accepted: Boolean
    ) : MessageContent() {
        override fun toDisplayString(): String {
            return if (accepted) {
                "$userName has accepted your invitation to the '$travelTitle' trip."
            } else {
                "$userName has declined your invitation to the '$travelTitle' trip."
            }
        }
    }

    abstract fun toDisplayString(): String
}

enum class ActionType {
    ACCEPT_INVITATION,
    DECLINE_INVITATION
}

data class MessageAction(
    val label: String,
    val actionType: ActionType
)