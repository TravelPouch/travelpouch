package com.github.se.travelpouch.model.activity

import android.content.Context
import com.github.se.travelpouch.model.Location
import com.google.firebase.Timestamp
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull

class ActivityModelViewUnitTest {
  private lateinit var repository: ActivityRepository
  private lateinit var activityViewModel: ActivityViewModel
  private lateinit var mockContext: Context

  val activity =
      Activity(
          "uid",
          "title",
          "description",
          Location(0.0, 0.0, Timestamp(0, 0), "location"),
          Timestamp(0, 0),
          mapOf())

  @Before
  fun setUp() {
    repository = mock(ActivityRepository::class.java)
    activityViewModel = ActivityViewModel(repository)
    mockContext = mock(Context::class.java)
  }

  @Test
  fun getActivitiesTest() {
    activityViewModel.getAllActivities()
    verify(repository).getAllActivities(anyOrNull(), anyOrNull())
  }

  @Test
  fun addActivitiesTest() {
    activityViewModel.addActivity(activity, mockContext)
    verify(repository).addActivity(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun getNewUidTest() {
    `when`(repository.getNewUid()).thenReturn("uid")
    assertThat(activityViewModel.getNewUid(), `is`("uid"))
  }

  @Test
  fun deleteActivitesByIdTest() {
    activityViewModel.deleteActivityById(activity, mockContext)
    verify(repository).deleteActivityById(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun updateActivityTest() {
    activityViewModel.updateActivity(activity, mockContext)
    verify(repository).updateActivity(anyOrNull(), anyOrNull(), anyOrNull())
  }
}
