package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var reminders: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

    private var returnsError = false

    fun setReturnsError(value: Boolean) {
        returnsError = value
    }
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (!returnsError) {
            Result.Success(reminders)
        } else {
            Result.Error("Reminders were unable to get retrieved")
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (returnsError) {
            return Result.Error("Reminder was unable to get retrieved")
        }

        val reminder = reminders.find { it.id == id }
        return if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("Reminder not found!")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}