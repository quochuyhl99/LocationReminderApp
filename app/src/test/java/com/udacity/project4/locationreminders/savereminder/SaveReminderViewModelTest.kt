package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import junit.framework.TestCase.*

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [29])
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),  fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun onClear_shouldClearLiveDataValues() {
        // Set some initial values
        viewModel.reminderTitle.value = "Title"
        viewModel.reminderDescription.value = "Description"
        viewModel.reminderSelectedLocationStr.value = "Location"
        viewModel.selectedPOI.value = PointOfInterest(LatLng(0.0, 0.0), "placeTest", "nameTest")
        viewModel.latitude.value = 1.0
        viewModel.longitude.value = 2.0

        // Call onClear()
        viewModel.onClear()

        // Check that all LiveData values are cleared
        assertNull(viewModel.reminderTitle.value)
        assertNull(viewModel.reminderDescription.value)
        assertNull(viewModel.reminderSelectedLocationStr.value)
        assertNull(viewModel.selectedPOI.value)
        assertNull(viewModel.latitude.value)
        assertNull(viewModel.longitude.value)
    }

    @Test
    fun validateAndSaveReminder_withValidData_shouldSaveReminder() = mainCoroutineRule.runBlockingTest {
        // Create a valid reminder data item
        val reminderData = ReminderDataItem("Title", "Description", "Location", 1.0, 2.0, "id")

        // Call validateAndSaveReminder()
        viewModel.validateAndSaveReminder(reminderData)

        // Check that the reminder is saved in the data source
        val savedReminderResult = fakeDataSource.getReminder("id")
        assertTrue(savedReminderResult is com.udacity.project4.locationreminders.data.dto.Result.Success)

        val savedReminder = (savedReminderResult as Result.Success).data

        assertEquals("Title", savedReminder.title)
        assertEquals("Description", savedReminder.description)
        assertEquals("Location", savedReminder.location)
        assertEquals(1.0, savedReminder.latitude)
        assertEquals(2.0, savedReminder.longitude)

        // Check that a toast message is shown
        assertEquals("Reminder Saved !", viewModel.showToast.getOrAwaitValue())
    }

    @Test
    fun validateAndSaveReminder_showNoTitleError() {
        // Create an invalid reminder data item (missing title)
        val reminderData = ReminderDataItem("", "Description", "Location", 1.0, 2.0, "id")

        // Call validateAndSaveReminder()
        viewModel.validateAndSaveReminder(reminderData)

        // Check that an error snackbar is shown
        assertEquals(R.string.err_enter_title, viewModel.showSnackBarInt.getOrAwaitValue())
    }
}