import com.github.se.travelpouch.model.UserInfo
import com.github.se.travelpouch.model.generateAutoId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UserInfoUnitTest {

  @Test
  fun testUserInfoCreation() {
    val fsUid = generateAutoId()
    val name = "Test User"
    val userTravelList = listOf(generateAutoId(), generateAutoId())
    val email = "testuser@example.com"

    val userInfo = UserInfo(fsUid, name, userTravelList, email)

    assertEquals(fsUid, userInfo.fsUid)
    assertEquals(name, userInfo.name)
    assertEquals(userTravelList, userInfo.userTravelList)
    assertEquals(email, userInfo.email)
  }

  @Test
  fun testUserInfoInvalidFsUid() {
    val invalidFsUid = "invalidUid"

    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          UserInfo(invalidFsUid, "Test User", listOf(generateAutoId()), "testuser@example.com")
        }

    assertEquals("Invalid fsUid format for fsUid", exception.message)
  }

  @Test
  fun testUserInfoToMap() {
    val fsUid = generateAutoId()
    val name = "Test User"
    val userTravelList = listOf(generateAutoId(), generateAutoId())
    val email = "testuser@example.com"

    val userInfo = UserInfo(fsUid, name, userTravelList, email)
    val map = userInfo.toMap()

    assertEquals(fsUid, map["fsUid"])
    assertEquals(name, map["name"])
    assertEquals(userTravelList, map["userTravelList"])
    assertEquals(email, map["email"])
  }
}
