package com.github.se.travelpouch.model.messages

import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.generateAutoId
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageContainerLastCaseTest {

  @Test
  fun messageInitialization_ValidData() {
    val messageUid = generateAutoId()
    val senderUid = generateAutoId()
    val receiverUid = generateAutoId()
    val travelUid = generateAutoId()
    val content = MessageContent.InvitationMessage("John Doe", "Trip to Paris", Role.PARTICIPANT)
    try {
      val myBeautifulMessage =
          Message(
              messageUid = messageUid,
              senderUid = senderUid,
              receiverUid = receiverUid,
              travelUid = travelUid,
              content = content,
              messageType = MessageType.INVITATION)
      assertEquals(messageUid, myBeautifulMessage.messageUid)
      assertEquals(senderUid, myBeautifulMessage.senderUid)
      assertEquals(receiverUid, myBeautifulMessage.receiverUid)
      assertEquals(travelUid, myBeautifulMessage.travelUid)
      assertEquals(content, myBeautifulMessage.content)
      assert(true)
    } catch (e: IllegalArgumentException) {
      assert(false)
    }
    assert(true)
  }

  @Test
  fun toMap_withValidData_returnsCorrectMap() {
    val messageUid = generateAutoId()
    val senderUid = generateAutoId()
    val receiverUid = generateAutoId()
    val travelUid = generateAutoId()
    val content = MessageContent.InvitationMessage("John Doe", "Trip to Paris", Role.PARTICIPANT)
    val message =
        Message(
            messageUid = messageUid,
            senderUid = senderUid,
            receiverUid = receiverUid,
            travelUid = travelUid,
            content = content,
            messageType = MessageType.INVITATION)

    val map = message.toMap()

    assertEquals(messageUid, map["messageId"])
    assertEquals(senderUid, map["senderId"])
    assertEquals(receiverUid, map["receiverId"])
    assertEquals(travelUid, map["travelId"])
    assertEquals(content, map["content"])
    assertEquals(MessageType.INVITATION, map["messageType"])
    assertEquals(message.timestamp, map["timestamp"])
    assertEquals(MessageStatus.UNREAD, map["status"])
  }

  @Test
  fun enumValues_containsAllExpectedValues_MessageType() {
    val expectedValues = listOf("INVITATION", "ACCEPTED", "DECLINED", "ROLE_UPDATE")
    val actualValues = MessageType.values().map { it.name }
    assertEquals(expectedValues, actualValues)
  }

  @Test
  fun valueOf_withValidName_returnsCorrectEnum() {
    assertEquals(MessageType.INVITATION, MessageType.valueOf("INVITATION"))
    assertEquals(MessageType.ACCEPTED, MessageType.valueOf("ACCEPTED"))
    assertEquals(MessageType.DECLINED, MessageType.valueOf("DECLINED"))
    assertEquals(MessageType.ROLE_UPDATE, MessageType.valueOf("ROLE_UPDATE"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_withInvalidName_throwsException() {
    MessageType.valueOf("INVALID")
  }

  @Test
  fun enumValues_containsAllExpectedValues_MessageStatus() {
    val expectedValues = listOf("READ", "UNREAD")
    val actualValues = MessageStatus.values().map { it.name }
    assertEquals(expectedValues, actualValues)
  }

  @Test
  fun valueOf_withValidName_returnsCorrectEnum_MessageStatus() {
    assertEquals(MessageStatus.READ, MessageStatus.valueOf("READ"))
    assertEquals(MessageStatus.UNREAD, MessageStatus.valueOf("UNREAD"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_withInvalidName_throwsException_MessageStatus() {
    MessageStatus.valueOf("INVALID")
  }
}
