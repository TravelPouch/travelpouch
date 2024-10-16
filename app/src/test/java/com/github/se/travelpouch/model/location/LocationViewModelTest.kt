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
    // Quand on appelle setQuery avec une chaîne de caractères
    viewModel.setQuery("Paris")

    // Vérification que la valeur du query a bien été mise à jour
    assertEquals("Paris", viewModel.query.first())
  }

  @Test
  fun testSetQuery_withEmptyQuery_doesNotSearch() = runTest {
    // Quand on appelle setQuery avec une chaîne vide
    viewModel.setQuery("")

    // Vérifie que la méthode search n'est jamais appelée
    verify(mockRepository, never()).search(anyString(), anyOrNull(), anyOrNull())
  }
}
