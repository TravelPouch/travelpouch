// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

fun formatStorageUnit(number: Long): String {
  val units = arrayOf("B", "KiB", "MiB", "GiB")
  var size = number.toDouble()
  var unitIndex = 0

  while (size >= 1024 && unitIndex < units.size - 1) {
    size /= 1024
    unitIndex++
  }

  return String.format(null, "%.1f %s", size, units[unitIndex])
}

data class StorageDashboardStats(
    var storageLimit: Long?,
    val storageUsed: Long,
    val storageReclaimable: Long,
) {
  /**
   * Calculates the ratio of storage used to storage limit.
   *
   * @return The ratio of storage used to storage limit.
   */
  fun storageUsedRatio(): Float {
    if (storageLimit == null || storageLimit == 0L) {
      return 0f
    }
    return min((storageUsed.toFloat() / storageLimit!!.toFloat()), 1f)
  }

  /**
   * Calculates the ratio of storage reclaimable to storage limit.
   *
   * @return The ratio of storage reclaimable to storage limit.
   */
  fun storageReclaimableRatio(): Float {
    if (storageLimit == null || storageLimit == 0L) {
      return 0f
    }
    return min((storageReclaimable.toFloat() / storageLimit!!.toFloat()), 1f)
  }

  fun storageLimitToString(): String {
    return if (storageLimit != null) formatStorageUnit(storageLimit!!) else "Unlimited"
  }

  fun storageAvailable(): Long {
    if (storageLimit == null) {
      return -1
    }
    return max(0, storageLimit!! - storageUsed)
  }
}

@HiltViewModel
open class StorageDashboardViewModel @Inject constructor() : ViewModel() {
  private val _isLoading = MutableStateFlow(true)
  open val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
  private val _storageStats = MutableStateFlow<StorageDashboardStats?>(null)
  open val storageStats: StateFlow<StorageDashboardStats?> = _storageStats.asStateFlow()

  open fun setStorageStats(storageStats: StorageDashboardStats?) {
    _storageStats.value = storageStats
    _isLoading.value = false
  }

  // Temporary function to update storage stats
  fun updateStorageStats() {
    setStorageStats(
        StorageDashboardStats(
            storageLimit = 500 * 1024L * 1024L,
            storageUsed = 400 * 1024L * 1024L,
            storageReclaimable = 50 * 1024L * 1024L,
        ))
  }
}
