package com.github.se.travelpouch.model

import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
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
          mapOf(Participant("SGzOL8yn0JmAVaTdvG9v") to Role.OWNER))

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
  fun getTravels_callsRepository() {
    listTravelViewModel.getTravels()
    verify(travelRepository).getTravels(anyOrNull(), anyOrNull())
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
  fun deleteTravelById_successfulDelete_updatesTravels() {
    val travelList = listOf(travel)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(1) as () -> Unit
          onSuccess()
          null
        }
        .whenever(travelRepository)
        .deleteTravelById(anyOrNull(), anyOrNull(), anyOrNull())

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<TravelContainer>) -> Unit
          onSuccess(travelList)
          null
        }
        .whenever(travelRepository)
        .getTravels(anyOrNull(), anyOrNull())

    listTravelViewModel.deleteTravelById(travel.fsUid)

    assertThat(listTravelViewModel.travels.value, `is`(travelList))
  }

  @Test
  fun deleteTravelById_failureDelete_doesNotUpdateTravels() {
    val initialTravels = listTravelViewModel.travels.value
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
}
