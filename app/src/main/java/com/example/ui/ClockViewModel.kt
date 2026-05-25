package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Alarm
import com.example.data.CampDatabase
import com.example.data.CampRepository
import com.example.data.Reminder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClockViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CampDatabase.getDatabase(application, viewModelScope)
    private val repository = CampRepository(database.alarmDao(), database.reminderDao())

    val alarms: StateFlow<List<Alarm>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time states
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    private val _activeRingingAlarm = MutableStateFlow<Alarm?>(null)
    val activeRingingAlarm: StateFlow<Alarm?> = _activeRingingAlarm.asStateFlow()

    private var lastTriggeredMinute = -1

    init {
        // Ticking clock loop
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                _currentTimeMillis.value = now
                checkAlarmsForTrigger(now)
                delay(1000)
            }
        }
    }

    private fun checkAlarmsForTrigger(timeInMillis: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        // Reset our safety trigger at second 0
        if (second == 0) {
            // allows retrigger
        }

        // Trigger alarm
        if (minute != lastTriggeredMinute && second <= 2) {
            val enabledAlarms = alarms.value.filter { it.isEnabled }
            val matchingAlarm = enabledAlarms.find { it.hour == hour && it.minute == minute }
            if (matchingAlarm != null) {
                _activeRingingAlarm.value = matchingAlarm
                lastTriggeredMinute = minute
            }
        }
    }

    fun dismissAlarm() {
        _activeRingingAlarm.value = null
    }

    fun snoozeAlarm() {
        val currentAlarm = _activeRingingAlarm.value
        if (currentAlarm != null) {
            _activeRingingAlarm.value = null
            // Silence trigger for 1 min
            viewModelScope.launch {
                delay(60000)
                _activeRingingAlarm.value = currentAlarm
            }
        }
    }

    // Alarm Actions
    fun addAlarm(hour: Int, minute: Int, label: String, isRepeatDaily: Boolean) {
        viewModelScope.launch {
            repository.insertAlarm(
                Alarm(
                    hour = hour,
                    minute = minute,
                    label = label,
                    isEnabled = true,
                    isRepeatDaily = isRepeatDaily
                )
            )
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.updateAlarm(alarm.copy(isEnabled = !alarm.isEnabled))
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    // Reminder Actions
    fun addReminder(title: String, characterName: String, time: String, note: String, category: String) {
        viewModelScope.launch {
            repository.insertReminder(
                Reminder(
                    title = title,
                    characterName = characterName,
                    targetTime = time,
                    note = note,
                    category = category,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleReminderCompletion(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun clearCompletedReminders() {
        viewModelScope.launch {
            repository.clearCompletedReminders()
        }
    }

    // Date/Time formatting helper
    fun getFormattedTime(timeMs: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timeMs))
    }

    fun getFormattedDate(timeMs: Long): String {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timeMs))
    }

    fun getCampQuote(timeMs: Long): CampQuote {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMs
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 5..10 -> CampQuote(
                character = "Keitaro Nagame",
                message = "Selamat pagi, Campers! 🪵 Ayo bangun dan rapikan tempat tidur sebelum inspeksi pagi Keitaro dimulai!",
                avatarColorHex = 0xFFF57C00 // Amber-Orange
            )
            in 11..14 -> CampQuote(
                character = "Hiro Akiba",
                message = "Sudah keroncongan ya perutnya? 🍲 Hiro sedang masak kari spesial perkemahan! Jangan terlambat ke meja makan!",
                avatarColorHex = 0xFF4CAF50 // Green
            )
            in 15..17 -> CampQuote(
                character = "Natsumi Hamasaki",
                message = "Waktunya petualangan luar ruangan! 🏕️ Mari kita siapkan tenda tambahan dan cari kayu bakar bersama Natsumi!",
                avatarColorHex = 0xFFFBC02D // Yellow / Gold
            )
            in 18..21 -> CampQuote(
                character = "Yoichi Yukimura",
                message = "Duduklah di sampingku dekat api unggun ini. 🔥 Dengarkan petikan gitar Yoichi, bersulang marshmallow malam ini.",
                avatarColorHex = 0xFF29B6F6 // Light Blue
            )
            else -> CampQuote(
                character = "Taiga Akatoki",
                message = "Ssttt... 🌌 Kudengar Kepala Sekolah Goro sedang berpatroli! Matikan lampu kabinmu dan tidurlah segera!",
                avatarColorHex = 0xFFAB47BC // Violet
            )
        }
    }
}

data class CampQuote(
    val character: String,
    val message: String,
    val avatarColorHex: Long
)
