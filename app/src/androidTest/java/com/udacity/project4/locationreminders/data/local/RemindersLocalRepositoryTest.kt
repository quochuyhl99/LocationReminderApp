package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveAndRetrieveReminderTest() = runBlocking {
        val reminder1 = ReminderDTO("Reminder 1", "Description 1", "Location 1", 1.1, 1.1)
        localRepository.saveReminder(reminder1)

        val result = localRepository.getReminder(reminder1.id)

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success

        assertThat(result.data.title, `is`(reminder1.title))
        assertThat(result.data.description, `is`(reminder1.description))
        assertThat(result.data.latitude, `is`(reminder1.latitude))
        assertThat(result.data.longitude, `is`(reminder1.longitude))
        assertThat(result.data.location, `is`(reminder1.location))
    }

    @Test
    fun getRemindersDeleteRemindersTest() = runBlocking {
        val reminder1 = ReminderDTO("Reminder 1", "Description 1", "Location 1", 1.1, 1.1)
        val reminder2 = ReminderDTO("Reminder 2", "Description 2", "Location 2", 2.2, 2.2)
        val reminder3 = ReminderDTO("Reminder 3", "Description 3", "Location 3", 3.3, 3.3)

        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        localRepository.saveReminder(reminder3)

        val resultList = localRepository.getReminders()
        assertThat(resultList is Result.Success, `is`(true))

        resultList as Result.Success
        assertEquals(3, resultList.data.size)

        localRepository.deleteAllReminders()
        val emptyList = localRepository.getReminders()
        emptyList as Result.Success
        assertEquals(0, emptyList.data.size)
    }

    @Test
    fun getReminderInvalidIdReturnFalseTest() = runBlocking{
        val result = localRepository.getReminder("teste")
        assertThat(result is Result.Success, `is`(false))
    }
}