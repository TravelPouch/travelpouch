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
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

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

  val activity2 =
      Activity(
          "uid2",
          "title2",
          "description2",
          Location(0.0, 0.0, Timestamp(0, 0), "location2"),
          Timestamp(50, 0), // Earlier timestamp
          mapOf())

  val activity3 =
      Activity(
          "uid3",
          "title3",
          "description3",
          Location(0.0, 0.0, Timestamp(0, 0), "location3"),
          Timestamp(150, 0), // Latest timestamp
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

  @Test
  fun sortedActivitiesTest() {
    // Set up a list of activities in random order
    val activitiesList = listOf(activity, activity3, activity2)

    // Mock the repository to return the activitiesList when getAllActivities is called
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<Activity>) -> Unit
          onSuccess(activitiesList) // Simulate a successful callback with the activitiesList
          null
        }
        .whenever(repository)
        .getAllActivities(anyOrNull(), anyOrNull())

    // Trigger the ViewModel to fetch activities from the repository
    activityViewModel.getAllActivities()

    // Get the sorted activities from the ViewModel
    val sortedActivities = activityViewModel.getSortedActivities()

    // Assert that the sorted list is not empty
    assertThat(sortedActivities.isNotEmpty(), `is`(true))

    // Assert that the activities are sorted by date in ascending order
    assertThat(sortedActivities[0], `is`(activity))
    assertThat(sortedActivities[1], `is`(activity2))
    assertThat(sortedActivities[2], `is`(activity3))
  }
}
