package com.github.se.travelpouch.model

import android.util.Log
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ListTravelViewModelTest {

  private lateinit var travelRepository: TravelRepository
  private lateinit var listTravelViewModel: ListTravelViewModel

  private val travel =
      TravelContainer(
          "6NU2zp2oGdA34s1Q1q5h",
          "Test Title",
          "Test Description",
          Timestamp.now(),
          Timestamp(Timestamp.now().seconds + 1000, 0),
          Location(
              0.0,
              0.0,
              Timestamp.now(),
              "Test Location",
          ),
          mapOf("Test Key item" to "Test Value item"),
          mapOf(Participant("SGzOL8yn0JmAVaTdvG9v12345678") to Role.OWNER))

  @Before
  fun setUp() {
    travelRepository = mock(TravelRepository::class.java)
    listTravelViewModel = ListTravelViewModel(travelRepository)
  }

  @Test
  fun getNewUid_returnsUniqueId() {
    whenever(travelRepository.getNewUid()).thenReturn("6NU2zp2oGdA34s1Q1q5h")
    assertThat(listTravelViewModel.getNewUid(), `is`("6NU2zp2oGdA34s1Q1q5h"))
  }

  @Test
  fun getNewUid_returnsDifferentIdsOnSubsequentCalls() {
    val firstUid = "uniqueId123"
    val secondUid = "uniqueId456"
    whenever(travelRepository.getNewUid()).thenReturn(firstUid, secondUid)

    val firstCallUid = listTravelViewModel.getNewUid()
    val secondCallUid = listTravelViewModel.getNewUid()

    assertNotEquals(firstCallUid, secondCallUid)
    verify(travelRepository, times(2)).getNewUid()
  }

  @Test
  fun getTravels_successfulFetch_updatesTravels() {
    val travelList = listOf(travel)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<TravelContainer>) -> Unit
          onSuccess(travelList)
          null
        }
        .whenever(travelRepository)
        .getTravels(anyOrNull(), anyOrNull())

    listTravelViewModel.getTravels()

    verify(travelRepository).getTravels(anyOrNull(), anyOrNull())

    assertThat(listTravelViewModel.travels.value, `is`(travelList))
  }

  @Test
  fun getTravels_failureFetch_doesNotUpdateTravels() {
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(1) as (Exception) -> Unit
          onFailure(Exception("Get Travels Failed Test"))
          null
        }
        .whenever(travelRepository)
        .getTravels(anyOrNull(), anyOrNull())

    listTravelViewModel.getTravels()

    assertThat(listTravelViewModel.travels.value, `is`(emptyList()))
  }

  @Test
  fun addTravel_successfulAdd_updatesTravels() {
    val travelList = listOf(travel)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(1) as () -> Unit
          onSuccess()
          null
        }
        .whenever(travelRepository)
        .addTravel(anyOrNull(), anyOrNull(), anyOrNull())

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<TravelContainer>) -> Unit
          onSuccess(travelList)
          null
        }
        .whenever(travelRepository)
        .getTravels(anyOrNull(), anyOrNull())

    listTravelViewModel.addTravel(travel)

    assertThat(listTravelViewModel.travels.value, `is`(travelList))
  }

  @Test
  fun addTravel_failureAdd_doesNotUpdateTravels() {
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(2) as (Exception) -> Unit
          onFailure(Exception("Add Travel Failed Test"))
          null
        }
        .whenever(travelRepository)
        .addTravel(anyOrNull(), anyOrNull(), anyOrNull())

    listTravelViewModel.addTravel(travel)

    assertThat(listTravelViewModel.travels.value, `is`(emptyList()))
  }

  @Test
  fun updateTravel_successfulUpdate_updatesTravels() {
    val travelList = listOf(travel)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(1) as () -> Unit
          onSuccess()
          null
        }
        .whenever(travelRepository)
        .updateTravel(anyOrNull(), anyOrNull(), anyOrNull())

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<TravelContainer>) -> Unit
          onSuccess(travelList)
          null
        }
        .whenever(travelRepository)
        .getTravels(anyOrNull(), anyOrNull())

    listTravelViewModel.updateTravel(travel)

    assertThat(listTravelViewModel.travels.value, `is`(travelList))
  }

  @Test
  fun updateTravel_failureUpdate_doesNotUpdateTravels() {
    val initialTravels = listTravelViewModel.travels.value
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(2) as (Exception) -> Unit
          onFailure(Exception("Update Travel Failed Test"))
          null
        }
        .whenever(travelRepository)
        .updateTravel(anyOrNull(), anyOrNull(), anyOrNull())

    listTravelViewModel.updateTravel(travel)

    assertThat(listTravelViewModel.travels.value, `is`(initialTravels))
  }

  @Test
  fun deleteTravelById_deleteUpdatesTravelsBasedOnResult() {
    val emptyTravelList = emptyList<TravelContainer>()
    val initialTravels = listTravelViewModel.travels.value

    // Successful delete
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(1) as () -> Unit
          onSuccess()
          null
        }
        .whenever(travelRepository)
        .deleteTravelById(anyOrNull(), anyOrNull(), anyOrNull())

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<TravelContainer>) -> Unit
          onSuccess(emptyTravelList)
          null
        }
        .whenever(travelRepository)
        .getTravels(anyOrNull(), anyOrNull())

    listTravelViewModel.deleteTravelById(travel.fsUid)
    assertThat(listTravelViewModel.travels.value, `is`(emptyTravelList))

    // Failure delete
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(2) as (Exception) -> Unit
          onFailure(Exception("Delete Travel Failed Test"))
          null
        }
        .whenever(travelRepository)
        .deleteTravelById(anyOrNull(), anyOrNull(), anyOrNull())

    listTravelViewModel.deleteTravelById(travel.fsUid)
    assertThat(listTravelViewModel.travels.value, `is`(initialTravels))
  }

  @Test
  fun selectTravel_updatesSelectedTravel() {
    listTravelViewModel.selectTravel(travel)
    assertThat(listTravelViewModel.selectedTravel.value, `is`(travel))
  }

  @Test
  fun getTravels_logsErrorOnFailure() {
    val errorMessage = "Failed to get travels"
    val exception = Exception("Get Travels Failed Test")
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(1) as (Exception) -> Unit
          onFailure(exception)
          null
        }
        .whenever(travelRepository)
        .getTravels(anyOrNull(), anyOrNull())

    mockStatic(Log::class.java).use { logMock: MockedStatic<Log> ->
      logMock.`when`<Int> { Log.e(anyString(), anyString(), any()) }.thenReturn(0)

      listTravelViewModel.getTravels()

      verify(travelRepository).getTravels(anyOrNull(), anyOrNull())
      logMock.verify { Log.e("ListTravelViewModel", errorMessage, exception) }
    }
  }

  @Test
  fun addTravel_logsErrorOnFailure() {
    val errorMessage = "Failed to add travel"
    val exception = Exception("Add Travel Failed Test")
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(2) as (Exception) -> Unit
          onFailure(exception)
          null
        }
        .whenever(travelRepository)
        .addTravel(anyOrNull(), anyOrNull(), anyOrNull())

    mockStatic(Log::class.java).use { logMock: MockedStatic<Log> ->
      logMock.`when`<Int> { Log.e(anyString(), anyString(), any()) }.thenReturn(0)

      listTravelViewModel.addTravel(travel)

      verify(travelRepository).addTravel(anyOrNull(), anyOrNull(), anyOrNull())
      logMock.verify { Log.e("ListTravelViewModel", errorMessage, exception) }
    }
  }

  @Test
  fun updateTravel_logsErrorOnFailure() {
    val errorMessage = "Failed to update travel"
    val exception = Exception("Update Travel Failed Test")
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(2) as (Exception) -> Unit
          onFailure(exception)
          null
        }
        .whenever(travelRepository)
        .updateTravel(anyOrNull(), anyOrNull(), anyOrNull())

    mockStatic(Log::class.java).use { logMock: MockedStatic<Log> ->
      logMock.`when`<Int> { Log.e(anyString(), anyString(), any()) }.thenReturn(0)

      listTravelViewModel.updateTravel(travel)

      verify(travelRepository).updateTravel(anyOrNull(), anyOrNull(), anyOrNull())
      logMock.verify { Log.e("ListTravelViewModel", errorMessage, exception) }
    }
  }

  @Test
  fun deleteTravelById_logsErrorOnFailure() {
    val errorMessage = "Failed to delete travel"
    val exception = Exception("Delete Travel Failed Test")
    doAnswer { invocation ->
          val onFailure = invocation.getArgument(2) as (Exception) -> Unit
          onFailure(exception)

          null
        }
        .whenever(travelRepository)
        .deleteTravelById(anyOrNull(), anyOrNull(), anyOrNull())
    mockStatic(Log::class.java).use { logMock: MockedStatic<Log> ->
      logMock.`when`<Int> { Log.e(anyString(), anyString(), any()) }.thenReturn(0)

      listTravelViewModel.deleteTravelById(travel.fsUid)

      verify(travelRepository).deleteTravelById(anyOrNull(), anyOrNull(), anyOrNull())
      logMock.verify { Log.e("ListTravelViewModel", errorMessage, exception) }
    }
  }
}
