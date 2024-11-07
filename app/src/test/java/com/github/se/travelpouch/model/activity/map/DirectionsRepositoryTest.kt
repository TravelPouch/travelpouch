package com.github.se.travelpouch.model.activity.map

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

class DirectionsRepositoryTest {

  private lateinit var mockClient: OkHttpClient
  private lateinit var mockCall: Call
  private lateinit var directionsRepository: DirectionsRepository

  @Before
  fun setUp() {
    // Mock OkHttpClient and Call
    mockClient = mock(OkHttpClient::class.java)
    mockCall = mock(Call::class.java)
    directionsRepository = DirectionsRepository(mockClient)
  }

  @Test
  fun testGetDirections_successfulResponse() {
    // Mocking the response body
    val responseBody =
        """
            {
                "status": "OK",
                "routes": [
                    {
                        "overview_polyline": {
                            "points": "encodedPolylineString"
                        }
                    }
                ]
            }
            """
            .trimIndent()

    val mockResponse = mock(Response::class.java)
    val mockResponseBody = responseBody.toResponseBody("application/json".toMediaTypeOrNull())

    // Mock behavior of OkHttpClient and Call
    `when`(mockClient.newCall(any())).thenReturn(mockCall)
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body).thenReturn(mockResponseBody)

    // Simulate a successful response
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    // Callback results
    var directionsResponse: DirectionsResponse? = null
    var failureException: Exception? = null

    directionsRepository.getDirections(
        "origin",
        "destination",
        "driving",
        "apiKey",
        { response -> directionsResponse = response },
        { exception -> failureException = exception })

    // Assert results
    assertTrue(failureException == null)
    assertTrue(directionsResponse?.routes?.isNotEmpty() == true)
    val route = directionsResponse!!.routes[0]
    assertEquals("encodedPolylineString", route.overviewPolyline.points)
  }

  @Test
  fun testGetDirections_statusNotOk() {
    // Mocking the response with a status that is not "OK"
    val responseBody =
        """
        {
            "status": "NOT_FOUND",
            "routes": []
        }
        """
            .trimIndent()

    val mockResponse = mock(Response::class.java)
    val mockResponseBody = responseBody.toResponseBody("application/json".toMediaTypeOrNull())

    // Mock behavior of OkHttpClient and Call
    `when`(mockClient.newCall(any())).thenReturn(mockCall)
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body).thenReturn(mockResponseBody)

    // Simulate a successful response but with a status of "NOT_FOUND"
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    // Callback results
    var failureException: Exception? = null

    directionsRepository.getDirections(
        "origin",
        "destination",
        "driving",
        "apiKey",
        {},
        { exception -> failureException = exception })

    // Assert that an exception was thrown with the correct message
    assertTrue(failureException != null)
    assertEquals("API request failed with status: NOT_FOUND", failureException?.message)
  }

  @Test
  fun testGetDirections_failureResponse() {
    // Simulate a network failure
    val ioException = IOException("Network failure")
    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onFailure(mockCall, ioException)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    // Callback results
    var directionsResponse: DirectionsResponse? = null
    var failureException: Exception? = null

    directionsRepository.getDirections(
        "origin",
        "destination",
        "driving",
        "apiKey",
        { response -> directionsResponse = response },
        { exception -> failureException = exception })

    // Assert results
    assertTrue(directionsResponse == null)
    assertTrue(failureException is IOException)
    assertEquals("Network failure", failureException?.message)
  }

  @Test
  fun testGetDirections_responseNotSuccessful() {
    // Mocking the response
    val mockResponse = mock(Response::class.java)
    `when`(mockResponse.isSuccessful).thenReturn(false)
    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    // Simulate an unsuccessful response
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    // Callback results
    var failureException: Exception? = null

    directionsRepository.getDirections(
        "origin",
        "destination",
        "driving",
        "apiKey",
        {},
        { exception -> failureException = exception })

    // Assert results
    assertTrue(failureException != null)
  }

  @Test
  fun testGetDirections_emptyResponseBody() {
    // Mocking the response
    val mockResponse = mock(Response::class.java)
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body).thenReturn(null) // Simulating a null body

    // Mock behavior of OkHttpClient and Call
    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    // Simulate a response with a null body
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    // Callback results
    var directionsResponse: DirectionsResponse? = null
    var failureException: Exception? = null

    directionsRepository.getDirections(
        "origin",
        "destination",
        "driving",
        "apiKey",
        { response -> directionsResponse = response },
        { exception -> failureException = exception })

    // Assert results
    assertTrue(directionsResponse == null)
    assertTrue(failureException != null)
    assertEquals("Empty response body", failureException?.message)
  }

  @Test
  fun testGetDirections_invalidJsonResponse() {
    // Mocking the response with invalid JSON
    val responseBody = "{ invalid json }"
    val mockResponse = mock(Response::class.java)
    val mockResponseBody = responseBody.toResponseBody("application/json".toMediaTypeOrNull())

    `when`(mockClient.newCall(any())).thenReturn(mockCall)
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body).thenReturn(mockResponseBody)

    // Simulate a response with invalid JSON
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    // Callback results
    var failureException: Exception? = null

    directionsRepository.getDirections(
        "origin",
        "destination",
        "driving",
        "apiKey",
        {},
        { exception -> failureException = exception })

    // Assert results
    assertTrue(failureException != null)
  }
}