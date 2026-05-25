package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean = true,
    val isRepeatDaily: Boolean = true
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val characterName: String, // Keitaro, Yoichi, Hiro, Natsumi, Taiga, Hunter, etc.
    val isCompleted: Boolean = false,
    val targetTime: String = "", // e.g., "08:00" or simple text representation
    val note: String = "",
    val category: String = "Camp" // "Inspection", "Cooking", "Friendship", "Survival", "Night"
)

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm)

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): Alarm?
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, id ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE isCompleted = 1")
    suspend fun clearCompletedReminders()
}

@Database(entities = [Alarm::class, Reminder::class], version = 1, exportSchema = false)
abstract class CampDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: CampDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CampDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CampDatabase::class.java,
                    "camp_buddy_clock_db"
                )
                .addCallback(CampDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class CampDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDefaultQuests(database.reminderDao())
                    populateDefaultAlarms(database.alarmDao())
                }
            }
        }

        suspend fun populateDefaultQuests(dao: ReminderDao) {
            // Prepopulate adorable Camp Buddy themed reminders / quests
            dao.insertReminder(
                Reminder(
                    title = "Morning Cabin Inspection with Keitaro",
                    characterName = "Keitaro",
                    isCompleted = false,
                    targetTime = "08:00 AM",
                    note = "Clean up the sleeping bags and organize the badges. Ensure our cabin is perfectly neat!",
                    category = "Inspection"
                )
            )
            dao.insertReminder(
                Reminder(
                    title = "Kitchen Duty: Prep Lunch with Chef Hiro",
                    characterName = "Hiro",
                    isCompleted = false,
                    targetTime = "11:30 AM",
                    note = "Don't burn the visual novel scout special! Chop carrots and stir the campfire soup.",
                    category = "Cooking"
                )
            )
            dao.insertReminder(
                Reminder(
                    title = "Wilderness Survival Training with Natsumi",
                    characterName = "Natsumi",
                    isCompleted = false,
                    targetTime = "03:00 PM",
                    note = "Practice building knot ties and identifying forest maps. Earn our friendship badge!",
                    category = "Survival"
                )
            )
            dao.insertReminder(
                Reminder(
                    title = "Evening Fireplace Talk with Quiet Yoichi",
                    characterName = "Yoichi",
                    isCompleted = false,
                    targetTime = "08:30 PM",
                    note = "Listen to the guitar cords, roast marshmallows, and share camp adventure stories.",
                    category = "Friendship"
                )
            )
            dao.insertReminder(
                Reminder(
                    title = "Midnight Cabin Secret Escape with Taiga",
                    characterName = "Taiga",
                    isCompleted = false,
                    targetTime = "11:00 PM",
                    note = "Sneak past Principal Goro for a midnight starry sky exploration in the woods!",
                    category = "Night"
                )
            )
        }

        suspend fun populateDefaultAlarms(dao: AlarmDao) {
            // Initial friendly alarms
            dao.insertAlarm(Alarm(hour = 7, minute = 0, label = "Rise & Shine! Keitaro's Bugle Call", isEnabled = true))
            dao.insertAlarm(Alarm(hour = 22, minute = 30, label = "Lights Out / Cabin Curfew", isEnabled = false))
        }
    }
}
