package hu.bme.aut.android.befitt

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.room.Room
import hu.bme.aut.android.befitt.database.BeFittDatabase

class BeFittApplication : Application() {
    companion object {
        lateinit var beFittDatabase: BeFittDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        val applicationInfo: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val apiKey = applicationInfo.metaData["MAPS_API_KEY"]

        beFittDatabase = Room.databaseBuilder(
            applicationContext,
            BeFittDatabase::class.java,
            "beFitt-database"
        ).fallbackToDestructiveMigration().build()
    }
}