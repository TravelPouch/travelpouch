package com.github.se.travelpouch.model.location

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull

class LocationViewModelTest {

  private lateinit var mockRepository: LocationRepository
  private lateinit var viewModel: LocationViewModel

  @Before
  fun setUp() {
    mockRepository = mock(LocationRepository::class.java)
    viewModel = LocationViewModel(mockRepository)
  }

  @Test
  fun testSetQuery_updatesQueryValue() = runTest {
    // When setQuery is called with a string
    viewModel.setQuery("Paris")

    // Verify that the query value has been updated correctly
    assertEquals("Paris", viewModel.query.first())
  }

  @Test
  fun testSetQuery_withEmptyQuery_doesNotSearch() = runTest {
    // When setQuery is called with an empty string
    viewModel.setQuery("")

    // Verify that the search method is never called
    verify(mockRepository, never()).search(anyString(), anyOrNull(), anyOrNull())
  }
}
