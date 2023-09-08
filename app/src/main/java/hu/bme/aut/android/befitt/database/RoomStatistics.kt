package hu.bme.aut.android.befitt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
class RoomStatistics (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val rout: String? = null,
    val startDate: String,
    val endDate: String,
    val time: String,
    val avarageSpeed: Float,
    val distance: Int,
    val burnedCalories: Int
)