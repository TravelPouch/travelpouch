// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.helper

import java.io.IOException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class NetworkManagerTest {

  private lateinit var mockClient: OkHttpClient
  private lateinit var mockCall: Call
  private lateinit var networkManager: NetworkManager

  @Before
  fun setUp() {
    // Mock OkHttpClient and Call
    mockClient = mock(OkHttpClient::class.java)
    mockCall = mock(Call::class.java)
    networkManager = NetworkManager(mockClient)
  }

  @Test
  fun testExecuteRequest_successfulResponse() {
    val responseBody = "Success response"
    val mockResponse = mock(Response::class.java)
    val mockResponseBody = responseBody.toResponseBody("application/json".toMediaTypeOrNull())

    `when`(mockClient.newCall(any())).thenReturn(mockCall)
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body).thenReturn(mockResponseBody)

    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    var successResult: String? = null
    var failureException: Exception? = null

    networkManager.executeRequest(
        Request.Builder().url("http://test.com").build(),
        onSuccess = { response -> successResult = response },
        onFailure = { exception -> failureException = exception })

    assertTrue(failureException == null)
    assertEquals("Success response", successResult)
  }

  @Test
  fun testExecuteRequest_failureResponse() {
    val ioException = IOException("Network failure")
    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onFailure(mockCall, ioException)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    var successResult: String? = null
    var failureException: Exception? = null

    networkManager.executeRequest(
        Request.Builder().url("http://test.com").build(),
        onSuccess = { response -> successResult = response },
        onFailure = { exception -> failureException = exception })

    assertTrue(successResult == null)
    assertTrue(failureException is IOException)
    assertEquals("Network failure", failureException?.message)
  }

  @Test
  fun testExecuteRequest_responseNotSuccessful() {
    val mockResponse = mock(Response::class.java)
    `when`(mockResponse.isSuccessful).thenReturn(false)
    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    var successResult: String? = null
    var failureException: Exception? = null

    networkManager.executeRequest(
        Request.Builder().url("http://test.com").build(),
        onSuccess = { response -> successResult = response },
        onFailure = { exception -> failureException = exception })

    assertTrue(successResult == null)
    assertTrue(failureException != null)
    assertEquals("Unexpected code $mockResponse", failureException?.message)
  }

  @Test
  fun testExecuteRequest_emptyResponseBody() {
    val mockResponse = mock(Response::class.java)
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body).thenReturn(null) // Simulate null body
    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    doAnswer { invocation ->
          val callback = invocation.arguments[0] as Callback
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    var successResult: String? = null
    var failureException: Exception? = null

    networkManager.executeRequest(
        Request.Builder().url("http://test.com").build(),
        onSuccess = { response -> successResult = response },
        onFailure = { exception -> failureException = exception })

    assertTrue(successResult == null)
    assertTrue(failureException != null)
    assertEquals("Empty response body", failureException?.message)
  }
}
