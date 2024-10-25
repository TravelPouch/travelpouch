// Import necessary dependencies
package com.github.se.travelpouch.model.messages

import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.generateAutoId
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Test class for data() cases
@RunWith(Parameterized::class)
class MessageContainerInvalidDataTest(
    private val messageUid: String,
    private val senderUid: String,
    private val receiverUid: String,
    private val travelUid: String,
    private val content: MessageContent.InvitationMessage
) {

  companion object {
    @JvmStatic
    @Parameterized.Parameters(
        name =
            "{index}: Test with messageUid={0}, senderUid={1}, receiverUid={2}, travelUid={3}, content={4}")
    fun data(): Collection<Array<Any>> {
      return listOf(
          arrayOf(
              "",
              generateAutoId(),
              generateAutoId(),
              generateAutoId(),
              MessageContent.InvitationMessage("John Doe", "Trip to Paris", Role.PARTICIPANT)),
          arrayOf(
              generateAutoId(),
              "",
              generateAutoId(),
              generateAutoId(),
              MessageContent.InvitationMessage("John Doe", "Trip to Paris", Role.PARTICIPANT)),
          arrayOf(
              generateAutoId(),
              generateAutoId(),
              "",
              generateAutoId(),
              MessageContent.InvitationMessage("John Doe", "Trip to Paris", Role.PARTICIPANT)),
          arrayOf(
              generateAutoId(),
              generateAutoId(),
              generateAutoId(),
              "",
              MessageContent.InvitationMessage("John Doe", "Trip to Paris", Role.PARTICIPANT)),
          arrayOf(
              generateAutoId(),
              "6NU2zp2oGdA34s1Q1q5h",
              "6NU2zp2oGdA34s1Q1q5h",
              generateAutoId(),
              MessageContent.InvitationMessage("TheName", "Trip to Paris", Role.PARTICIPANT)),
      )
    }
  }

  @Test(expected = IllegalArgumentException::class)
  fun messageInitialization_withInvalidData_throwsException() {
    Message(
        messageUid = messageUid,
        senderUid = senderUid,
        receiverUid = receiverUid,
        travelUid = travelUid,
        content = content,
        messageType = MessageType.INVITATION)
  }
}
