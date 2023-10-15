package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveAndGetReminderTest() = runBlockingTest {
        val reminder = ReminderDTO("Hello", "Say Hello to Friends", "Rio de Janeiro", 1.1, 1.1)

        //Save Reminder
        database.reminderDao().saveReminder(reminder)

        //Get Reminder
        val result = database.reminderDao().getReminderById(reminder.id)


        assertThat(result as ReminderDTO, notNullValue())
        assertThat(result.id, `is`(reminder.id))
        assertThat(result.title, `is`(reminder.title))
        assertThat(result.description, `is`(reminder.description))
        assertThat(result.location, `is`(reminder.location))
        assertThat(result.latitude, `is`(reminder.latitude))
        assertThat(result.longitude, `is`(reminder.longitude))

    }

    @Test
    fun getReminderByInvalidIdTest() = runBlockingTest {
        // Attempt to retrieve a non-existent reminder by ID
        val invalidId = "invalid_id"
        val result = database.reminderDao().getReminderById(invalidId)

        // Verify that the result is null, indicating data not found
        assertThat(result, nullValue())
    }

    @Test
    fun saveAndDeleteAllRemindersTest() = runBlockingTest {

        val reminder1 = ReminderDTO("Reminder 1", "Description 1", "Location 1", 1.1, 1.1)
        val reminder2 = ReminderDTO("Reminder 2", "Description 2", "Location 2", 2.2, 2.2)
        val reminder3 = ReminderDTO("Reminder 3", "Description 3", "Location 3", 3.3, 3.3)

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        val reminderList = database.reminderDao().getReminders()
        assertEquals(reminderList.size, 3)

        database.reminderDao().deleteAllReminders()
        val emptyList = database.reminderDao().getReminders()
        assertEquals(emptyList.size, 0)
    }


}