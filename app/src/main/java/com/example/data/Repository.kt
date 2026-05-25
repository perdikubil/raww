package com.example.data

import kotlinx.coroutines.flow.Flow

class CampRepository(
    private val alarmDao: AlarmDao,
    private val reminderDao: ReminderDao
) {
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()

    suspend fun insertAlarm(alarm: Alarm) {
        alarmDao.insertAlarm(alarm)
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm)
    }

    suspend fun insertReminder(reminder: Reminder) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun clearCompletedReminders() {
        reminderDao.clearCompletedReminders()
    }
}
