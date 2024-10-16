import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.location.NominatimLocationRepository
import java.io.IOException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any

class NominatimLocationRepositoryTest {

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
    val responseBody = "[{\"lat\": 48.8566, \"lon\": 2.3522, \"display_name\": \"Paris, France\"}]"
    val mockResponse = mock(Response::class.java)
    val mockResponseBody = responseBody.toResponseBody("application/json".toMediaTypeOrNull())

    // Creating a real request to avoid null issues

    // Mock behavior of OkHttpClient and Call
    `when`(mockClient.newCall(any())).thenReturn(mockCall)
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body).thenReturn(mockResponseBody)

    // Simulate the successful response
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
    assertEquals(48.8566, location.latitude)
    assertEquals(2.3522, location.longitude)
    assertEquals("Paris, France", location.name)
  }

  @Test
  fun testSearch_failureResponse() {
    // Mocking the response body
    val responseBody = "[{\"lat\": 48.8566, \"lon\": 2.3522, \"display_name\": \"Paris, France\"}]"
    val mockResponse = mock(Response::class.java)
    val mockResponseBody = responseBody.toResponseBody("application/json".toMediaTypeOrNull())

    // Creating a real request to avoid null issues

    // Mock behavior of OkHttpClient and Call
    `when`(mockClient.newCall(any())).thenReturn(mockCall)
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body).thenReturn(mockResponseBody)

    // Simulate the successful response
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
    assertTrue(locations == null) // Aucun emplacement ne doit être retourné
    assertTrue(failureException is IOException) // Vérifie que l'exception est une IOException
    assertEquals("Failed to connect", failureException?.message) // Vérifie le message d'erreur
  }

  @Test
  fun testSearch_responseNotSuccessful() {
    // Mocking the response
    val mockResponse = mock(Response::class.java)

    `when`(mockResponse.isSuccessful).thenReturn(false)
    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    // Simuler que enqueue appelle la méthode onResponse du Callback
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    // Callback results
    var failureException: Exception? = null

    // Appel de la méthode search
    repository.search("Paris", {}, { e -> failureException = e })

    // Vérification
    assertTrue(failureException != null) // L'exception devrait être non nulle
  }

  @Test
  fun testSearch_responseBodyIsNull() {
    // Mocking the response
    val mockResponse = mock(Response::class.java)

    // Simuler la réponse non réussie avec un body null
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body).thenReturn(null) // Le corps de la réponse est null

    // Simuler le comportement du client et du Call
    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    // Simuler que enqueue appelle la méthode onResponse du Callback
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

    // Appel de la méthode search
    repository.search("Paris", { result -> locations = result }, { e -> failureException = e })

    // Vérification
    assertTrue(locations != null) // La liste de locations doit être non nulle
    assertTrue(locations!!.isEmpty()) // La liste doit être vide si le body est null
    assertTrue(failureException == null) // Aucune exception ne doit être levée
  }
}
