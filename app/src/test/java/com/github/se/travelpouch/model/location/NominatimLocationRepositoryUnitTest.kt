// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
import com.github.se.travelpouch.model.location.NominatimLocationRepository
import com.github.se.travelpouch.model.travels.Location
import java.io.IOException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class NominatimLocationRepositoryUnitTest {

  private lateinit var mockClient: OkHttpClient
  private lateinit var repository: NominatimLocationRepository
  private lateinit var mockCall: Call

  @Before
  fun setUp() {
    // Mock OkHttpClient and Call
    mockClient = mock(OkHttpClient::class.java)
    mockCall = mock(Call::class.java)
    repository = NominatimLocationRepository(mockClient)
  }

  @Test
  fun testSearch_successfulResponse() {
    // Mocking the response body
    val responseBody =
        """
        [
            {
              "place_id": 82297359,
              "licence": "Data © OpenStreetMap contributors, ODbL 1.0. http://osm.org/copyright",
              "osm_type": "relation",
              "osm_id": 71525,
              "lat": "48.8534951",
              "lon": "2.3483915",
              "class": "boundary",
              "type": "administrative",
              "place_rank": 12,
              "importance": 0.884566363022883,
              "addresstype": "city",
              "name": "Paris",
              "display_name": "Paris, Île-de-France, France métropolitaine, France",
              "boundingbox": [
                "48.8155755",
                "48.9021560",
                "2.2241220",
                "2.4697602"
              ]
            }
        ]
        """
            .trimIndent()

    val mockResponse = mock(Response::class.java)
    val mockResponseBody = responseBody.toResponseBody("application/json".toMediaTypeOrNull())

    // Mock behavior of OkHttpClient and Call
    `when`(mockClient.newCall(any())).thenReturn(mockCall)
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body).thenReturn(mockResponseBody)

    // Simulating the successful response
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    // Callback results
    var locations: List<Location>? = null
    var failureException: Exception? = null

    repository.search("Paris", { result -> locations = result }, { e -> failureException = e })

    // Assert results
    assertTrue(failureException == null)
    // assertTrue(locations?.isNotEmpty() == true)
    val location = locations!![0]
    assertEquals(48.8534951, location.latitude)
    assertEquals(2.3483915, location.longitude)
    assertEquals("Paris, Île-de-France, France métropolitaine, France", location.name)
  }

  @Test
  fun testSearch_failureResponse() {
    // Mock behavior of OkHttpClient and Call
    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    // Simulating a failed connection
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onFailure(mockCall, IOException("Failed to connect"))
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    // Callback results
    var locations: List<Location>? = null
    var failureException: Exception? = null

    repository.search("Paris", { result -> locations = result }, { e -> failureException = e })

    // Assert results
    assertTrue(locations == null) // No locations should be returned
    assertTrue(failureException is IOException) // Ensures the exception is an IOException
    assertEquals("Failed to connect", failureException?.message) // Checks the error message
  }

  @Test
  fun testSearch_responseNotSuccessful() {
    // Mocking the response
    val mockResponse = mock(Response::class.java)

    `when`(mockResponse.isSuccessful).thenReturn(false)
    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    // Simulate enqueue calling the onResponse method of the Callback
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    // Callback results
    var failureException: Exception? = null

    // Calling the search method
    repository.search("Paris", {}, { e -> failureException = e })

    // Verification
    assertTrue(failureException != null) // The exception should not be null
  }

  @Test
  fun testSearch_responseBodyIsNull() {
    // Mocking the response
    val mockResponse = mock(Response::class.java)

    // Simulate a successful response with a null body
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body).thenReturn(null) // The response body is null

    // Mock behavior of OkHttpClient and Call
    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    // Simulate enqueue calling the onResponse method of the Callback
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    // Callback results
    var locations: List<Location>? = null
    var failureException: Exception? = null

    // Calling the search method
    repository.search("Paris", { result -> locations = result }, { e -> failureException = e })

    // Verification
    assertTrue(locations == null) // The locations list should not be null
    assertTrue(failureException != null)
    assertEquals("Empty response body", failureException?.message)
  }
}
