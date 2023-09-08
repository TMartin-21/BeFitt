package hu.bme.aut.android.befitt.ui.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.android.befitt.BeFittApplication
import hu.bme.aut.android.befitt.model.Statistics
import hu.bme.aut.android.befitt.repository.DBRepository
import kotlinx.coroutines.launch

class StatViewModel: ViewModel() {
    private val repository: DBRepository

    val allStat: LiveData<List<Statistics>>

    init {
        val beFittDao = BeFittApplication.beFittDatabase.beFittDao()
        repository = DBRepository(beFittDao)
        allStat = repository.getAllStat()
    }

    fun insertStat(stat: Statistics) = viewModelScope.launch {
        repository.insertStat(stat)
    }

    fun deleteStat(stat: Statistics) = viewModelScope.launch {
        repository.deleteStat(stat)
    }

    fun deleteAllStat() = viewModelScope.launch {
        repository.deleteAllStat()
    }
}