package com.github.se.travelpouch.model.messages

import com.github.se.travelpouch.model.Role
import org.junit.Test

class MessageContentUnitTest {

    private lateinit var messageContent: MessageContent

    @Test
    fun invitationMessage_toDisplayString() {
        messageContent = MessageContent.InvitationMessage("Alice", "Trip to Paris", Role.PARTICIPANT)
        assert(messageContent.toDisplayString() == "Alice invited you to join the travel Trip to Paris as a PARTICIPANT.")
    }

    @Test
    fun invitationMessage_invalidArgument() {
        try {
            messageContent = MessageContent.InvitationMessage(" ", "Trip to Paris", Role.PARTICIPANT)
        } catch (e: IllegalArgumentException) {
            assert(e.message == "Inviter name cannot be blank")
        }

        try {
            messageContent = MessageContent.InvitationMessage("Alice", " ", Role.PARTICIPANT)
        } catch (e: IllegalArgumentException) {
            assert(e.message == "Travel title cannot be blank")
        }
    }

    @Test
    fun pendingInvitationReminder_toDisplayString() {
        messageContent = MessageContent.PendingInvitationReminder("Alice", "Trip to Paris")
        assert(messageContent.toDisplayString() == "You have not yet accepted Alice's invitation for the 'Trip to Paris' trip.")
    }

    @Test
    fun pendingInvitationReminder_invalidArgument() {
        try {
            messageContent = MessageContent.PendingInvitationReminder(" ", "Trip to Paris")
        } catch (e: IllegalArgumentException) {
            assert(e.message == "Inviter name cannot be blank")
        }

        try {
            messageContent = MessageContent.PendingInvitationReminder("Alice", " ")
        } catch (e: IllegalArgumentException) {
            assert(e.message == "Travel title cannot be blank")
        }
    }

    @Test
    fun roleChangeMessage_toDisplayString() {
        messageContent = MessageContent.RoleChangeMessage("Trip to Paris", Role.OWNER)
        assert(messageContent.toDisplayString() == "Your role for the 'Trip to Paris' trip has been changed to OWNER.")
    }

    @Test
    fun roleChangeMessage_invalidArgument() {
        try {
            messageContent = MessageContent.RoleChangeMessage("Trip to Paris", Role.OWNER)
        } catch (e: IllegalArgumentException) {
            assert(e.message == "Travel title cannot be blank")
        }
    }

     @Test
     fun invitationResponseMessage_toDisplayString() {
         messageContent = MessageContent.InvitationResponseMessage("Alice", "Trip to Paris", true)
         assert(messageContent.toDisplayString() == "Alice has accepted your invitation to the 'Trip to Paris' trip.")

            messageContent = MessageContent.InvitationResponseMessage("Alice", "Trip to Paris", false)
            assert(messageContent.toDisplayString() == "Alice has declined your invitation to the 'Trip to Paris' trip.")
     }

    @Test
    fun invitationResponseMessage_invalidArgument() {
        try {
            messageContent = MessageContent.InvitationResponseMessage(" ", "Trip to Paris", true)
        } catch (e: IllegalArgumentException) {
            assert(e.message == "User name cannot be blank")
        }

        try {
            messageContent = MessageContent.InvitationResponseMessage("Alice", " ", true)
        } catch (e: IllegalArgumentException) {
            assert(e.message == "Travel title cannot be blank")
        }
    }

}