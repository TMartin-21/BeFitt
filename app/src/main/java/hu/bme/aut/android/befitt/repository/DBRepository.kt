package hu.bme.aut.android.befitt.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import hu.bme.aut.android.befitt.database.BeFittDao
import hu.bme.aut.android.befitt.database.RoomStatistics
import hu.bme.aut.android.befitt.model.Statistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DBRepository(private val beFittDao: BeFittDao) {
    fun getAllStat(): LiveData<List<Statistics>> {
        return beFittDao.getAllStat()
            .map { roomStats ->
                roomStats.map { roomStat ->
                    roomStat.toDomainModel()
                }
            }
    }

    suspend fun insertStat(stat: Statistics) = withContext(Dispatchers.IO) {
        beFittDao.insertStat(stat.toRoomModel())
    }

    suspend fun deleteStat(stat: Statistics) = withContext(Dispatchers.IO) {
        val roomStat = beFittDao.getStatById(stat.id) ?: return@withContext
        beFittDao.deleteStat(roomStat)
    }

    suspend fun deleteAllStat() = withContext(Dispatchers.IO) {
        beFittDao.deleteAllStat()
    }

    private fun RoomStatistics.toDomainModel() = Statistics(
        id = id,
        rout = rout,
        startDate = startDate,
        endDate = endDate,
        time = time,
        avarageSpeed = avarageSpeed,
        distance = distance,
        burnedCalories = burnedCalories
    )

    private fun Statistics.toRoomModel() = RoomStatistics(
        rout = rout,
        startDate = startDate,
        endDate = endDate,
        time = time,
        avarageSpeed = avarageSpeed,
        distance = distance,
        burnedCalories = burnedCalories
    )
}