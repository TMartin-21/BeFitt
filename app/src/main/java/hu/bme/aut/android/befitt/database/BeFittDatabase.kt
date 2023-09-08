package hu.bme.aut.android.befitt.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
    exportSchema = false,
    entities = [RoomStatistics::class]
)
abstract class BeFittDatabase : RoomDatabase() {
    abstract fun beFittDao() : BeFittDao
}