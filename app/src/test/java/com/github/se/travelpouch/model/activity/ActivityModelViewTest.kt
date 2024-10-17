package com.github.se.travelpouch.model.activity

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

class ActivityModelViewTest {
  private lateinit var repository: ActivityRepository
  private lateinit var activityViewModel: ActivityModelView

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
    activityViewModel = ActivityModelView(repository)
  }

  @Test
  fun getActivitiesTest() {
    activityViewModel.getActivities()
    verify(repository).getActivity(anyOrNull(), anyOrNull())
  }

  @Test
  fun addActivitiesTest() {
    activityViewModel.addActivity(activity)
    verify(repository).addActivity(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun getNewUidTest() {
    `when`(repository.getNewUid()).thenReturn("uid")
    assertThat(activityViewModel.getNewUid(), `is`("uid"))
  }

  @Test
  fun deleteActivitesByIdTest() {
    activityViewModel.deleteActivityById(activity)
    verify(repository).deleteActivityById(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun updateActivityTest() {
    activityViewModel.updateActivity(activity)
    verify(repository).updateActivity(anyOrNull(), anyOrNull(), anyOrNull())
  }
}
