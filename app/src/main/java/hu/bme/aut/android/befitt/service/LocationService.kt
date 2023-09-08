package hu.bme.aut.android.befitt.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import hu.bme.aut.android.befitt.MainActivity
import hu.bme.aut.android.befitt.R
import hu.bme.aut.android.befitt.ui.profile.ProfileFragment
import java.util.*
import kotlin.math.roundToInt

class LocationService : LifecycleService() {
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val BR_NEW_LOCATION = "BR_NEW_LOCATION"

        const val KEY_ROUT = "KEY_ROUT"
        const val KEY_LOCATION = "KEY_LOCATION"
        const val KEY_DISTANCE = "KEY_DISTANCE"

        const val TIME_EXTRA = "TIME_EXTRA"

        private const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "ForegroundService"

        val isTracking = MutableLiveData<Boolean>()

        private var pathPoints = arrayListOf(arrayListOf<LatLng>())
    }

    private var locationHelper: LocationHelper? = null
    private var timer = Timer()
    private var distance: Int = 0
    private var deleteFirstLocation = true
    private var serviceIntent = Intent()

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!){
                if (deleteFirstLocation && pathPoints.lastOrNull() != null && pathPoints.last().count() == 1){
                    pathPoints.last().clear()
                    deleteFirstLocation = false
                }

                val lastLocation = result.lastLocation ?: return
                result.locations.let { locations ->
                    for (location in locations){
                        addPathPoints(location)
                        updateNotification("Location: (${location.latitude}, ${location.longitude})")
                    }
                }

                calculateDistance()
                if (distance >= ProfileFragment.profile.minDistance){
                    updateNotification("Minimum distance achived!")
                }

                serviceIntent.action = BR_NEW_LOCATION
                serviceIntent.putExtra(KEY_ROUT, pathPoints)
                serviceIntent.putExtra(KEY_LOCATION, lastLocation)
                serviceIntent.putExtra(KEY_DISTANCE, distance)
                LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(serviceIntent)
            }
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            super.onLocationAvailability(availability)
            updateNotification("Location available: ${availability.isLocationAvailable}")
        }
    }

    private fun calculateDistance() {
        val sum = FloatArray(1) { 0f }
        if (pathPoints.lastOrNull() != null && pathPoints.last().count() >= 2){
            val i1 = pathPoints.last().count() - 1
            val i2 = i1 - 1
            Location.distanceBetween(
                pathPoints.last()[i2].latitude,
                pathPoints.last()[i2].longitude,
                pathPoints.last()[i1].latitude,
                pathPoints.last()[i1].longitude,
                sum
            )
        }
        distance += sum.last().roundToInt()
    }

    private fun addPathPoints(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.last().add(pos)
        }
    }

    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking){
            if (locationHelper == null){
                val helper = LocationHelper(applicationContext, locationCallback)
                helper.startLocationMonitoring()
                locationHelper = helper
            } else {
                locationHelper!!.startLocationMonitoring()
            }
        } else {
            locationHelper?.stopLocationMonitoring()
        }
    }

    override fun onCreate() {
        super.onCreate()
        isTracking.postValue(false)
        pathPoints = arrayListOf(arrayListOf())
        distance = 0

        isTracking.observe(this) {
            updateLocationTracking(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START -> {
                    val time = serviceIntent.getDoubleExtra(TIME_EXTRA, 0.0)
                    timer = Timer()
                    timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
                    startService()
                }
                ACTION_PAUSE -> {
                    timer.cancel()
                    pauseService()
                }
                ACTION_STOP -> {
                    timer.cancel()
                    stopService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private inner class TimeTask(private var time: Double): TimerTask() {
        override fun run() {
            time++
            serviceIntent.putExtra(TIME_EXTRA, time)
            LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(serviceIntent)
        }
    }

    private fun startService() {
        startForeground(NOTIFICATION_ID, createNotification("Starting location service ..."))
        isTracking.postValue(true)
        Toast.makeText(this, "Rout tracking started", Toast.LENGTH_LONG).show()
    }

    private fun pauseService() {
        isTracking.postValue(false)
        updateNotification("Rout tracking is paused")
        pathPoints.add(arrayListOf())
        deleteFirstLocation = true
        Toast.makeText(this, "Rout tracking paused", Toast.LENGTH_LONG).show()
    }

    private fun stopService() {
        isTracking.postValue(false)
        serviceIntent = Intent()
        stopForeground(true)
        stopSelf()
        Toast.makeText(this, "Rout tracking stopped", Toast.LENGTH_LONG).show()
    }

    private fun createNotification(text: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK

        createNotificationChannel()

        val contentIntent = PendingIntent.getActivity(this,
            NOTIFICATION_ID,
            notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Service Location")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setVibrate(longArrayOf(1000, 2000, 3000))
            .setContentIntent(contentIntent)
            .build()
    }

    private fun updateNotification(text: String){
        val notification = createNotification(text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}