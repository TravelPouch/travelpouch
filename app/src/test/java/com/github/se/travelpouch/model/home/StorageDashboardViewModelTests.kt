package com.github.se.travelpouch.model.home

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class StorageDashboardViewModelTests {

  @Before fun setup() {}

  @Test
  fun formatStorageUnitAllUnits() {
    assertThat(formatStorageUnit(0), `is`("0.0 B"))
    assertThat(formatStorageUnit(1), `is`("1.0 B"))
    assertThat(formatStorageUnit(1023), `is`("1023.0 B"))
    assertThat(formatStorageUnit(1024), `is`("1.0 KiB"))
    assertThat(formatStorageUnit(1024 * 1024 - 1), `is`("1024.0 KiB"))
    assertThat(formatStorageUnit(1024 * 1024), `is`("1.0 MiB"))
    assertThat(formatStorageUnit(1024 * 1024 * 1024 - 1), `is`("1024.0 MiB"))
    assertThat(formatStorageUnit(1024 * 1024 * 1024), `is`("1.0 GiB"))
    assertThat(formatStorageUnit(1024L * 1024 * 1024 * 1024 - 1), `is`("1024.0 GiB"))
    assertThat(formatStorageUnit(1024L * 1024 * 1024 * 1024), `is`("1024.0 GiB"))
  }

  @Test
  fun storageUsedRatio() {
    val stats = StorageDashboardStats(1024, 512, 0)
    assertThat(stats.storageUsedRatio(), `is`(0.5f))
  }

  @Test
  fun storageReclaimableRatio() {
    val stats = StorageDashboardStats(1024, 0, 512)
    assertThat(stats.storageReclaimableRatio(), `is`(0.5f))
  }

  @Test
  fun storageLimitToString() {
    val stats = StorageDashboardStats(1024, 0, 0)
    assertThat(stats.storageLimitToString(), `is`("1.0 KiB"))
  }

  @Test
  fun storageAvailable() {
    val stats = StorageDashboardStats(1024, 512, 0)
    assertThat(stats.storageAvailable(), `is`(512))
  }

  @Test
  fun storageAvailableUnlimited() {
    val stats = StorageDashboardStats(null, 512, 0)
    assertThat(stats.storageAvailable(), `is`(-1))
  }

  @Test
  fun storageAvailableNegative() {
    val stats = StorageDashboardStats(1024, 1025, 0)
    assertThat(stats.storageAvailable(), `is`(0))
  }

  @Test
  fun storageAvailableUnlimitedNegative() {
    val stats = StorageDashboardStats(null, 1025, 0)
    assertThat(stats.storageAvailable(), `is`(-1))
  }

  @Test
  fun storageAvailableZero() {
    val stats = StorageDashboardStats(1024, 1025, 0)
    assertThat(stats.storageAvailable(), `is`(0))
  }

  @Test
  fun storageDashboardViewModelSetStorageStats() {
    val stats = StorageDashboardStats(1024, 512, 0)
    val viewModel = StorageDashboardViewModel()
    viewModel.setStorageStats(stats)
    assertThat(viewModel.storageStats.value, `is`(stats))
  }

  @Test
  fun storageDashboardViewModelUpdateStorageStats() {
    val viewModel = StorageDashboardViewModel()
    viewModel.updateStorageStats()
    assertThat(viewModel.isLoading.value, `is`(false))
  }
}
