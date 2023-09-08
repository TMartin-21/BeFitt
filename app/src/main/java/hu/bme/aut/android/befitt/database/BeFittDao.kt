package hu.bme.aut.android.befitt.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BeFittDao {
    @Insert
    fun insertStat(stat: RoomStatistics)

    @Query("SELECT * FROM statistics")
    fun getAllStat(): LiveData<List<RoomStatistics>>

    @Query("SELECT * FROM statistics WHERE id == :id")
    fun getStatById(id: Int?): RoomStatistics?

    @Query("DELETE FROM statistics")
    fun deleteAllStat()

    @Delete
    fun deleteStat(stat: RoomStatistics)
}