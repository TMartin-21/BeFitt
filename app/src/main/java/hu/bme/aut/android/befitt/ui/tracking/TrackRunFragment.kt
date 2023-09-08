package hu.bme.aut.android.befitt.ui.tracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import hu.bme.aut.android.befitt.R
import hu.bme.aut.android.befitt.databinding.FragmentTrackRunBinding
import hu.bme.aut.android.befitt.model.Statistics
import hu.bme.aut.android.befitt.service.LocationService
import hu.bme.aut.android.befitt.service.LocationService.Companion.ACTION_PAUSE
import hu.bme.aut.android.befitt.service.LocationService.Companion.ACTION_START
import hu.bme.aut.android.befitt.service.LocationService.Companion.ACTION_STOP
import hu.bme.aut.android.befitt.ui.profile.ProfileFragment
import hu.bme.aut.android.befitt.ui.statistics.StatViewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.random.Random

@RuntimePermissions
class TrackRunFragment : Fragment() {
    private lateinit var binding: FragmentTrackRunBinding
    private lateinit var mContext: Context
    private lateinit var statView: StatViewModel
    private var serviceStarted: Boolean = false
    private var newRunStarted: Boolean = true

    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private lateinit var startTime: String
    private lateinit var endTime: String
    private var time = 0.0

    private var lastLocation: LatLng? = null
    private lateinit var rout: ArrayList<ArrayList<LatLng>>
    private lateinit var speed: ArrayList<Float>
    private var distance: Int = 0
    private val MET_TABLE = hashMapOf<Int, Float>()
    private val CONVERSION_RATIO = 0.44704

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.clear()
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(mContext, R.raw.style_night)
        )

        googleMap.setMinZoomPreference(5f)
        for (r in rout){
            googleMap.setMinZoomPreference(15f)
            val polylineOptions = PolylineOptions().addAll(r)
            val polyline = googleMap.addPolyline(polylineOptions)
            polyline.color = Color.RED
            polyline.width = 10f
        }

        val location: LatLng
        if (rout.lastOrNull()?.lastOrNull() == null){
            location = lastLocation ?: LatLng(47.0, 19.0)
        } else {
            rout.last().last().also { location = it }
        }

        googleMap.addMarker(MarkerOptions().position(location).title("Marker in hungary"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
    }

    private val locationReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val currentPath = intent.getSerializableExtra(LocationService.KEY_ROUT)!!
            rout = currentPath as ArrayList<ArrayList<LatLng>>

            val currentLocation = intent.getParcelableExtra<Location>(LocationService.KEY_LOCATION)!!
            lastLocation = LatLng(currentLocation.latitude, currentLocation.longitude)

            speed.add(currentLocation.speed)
            binding.statCard.tvSpeedValue.text = currentLocation.speed.toString()

            binding.statCard.tvPositionValue.text = "(${currentLocation.latitude}, ${currentLocation.longitude})"

            distance = intent.getIntExtra(LocationService.KEY_DISTANCE, 0)
            binding.statCard.tvDistanceValue.text = "$distance m"

            time = intent.getDoubleExtra(LocationService.TIME_EXTRA, 0.0)
            binding.statCard.tvTimeValue.text = getTimeStringFromDouble(time)

            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
        }
    }

    private fun getTimeInSec(time: Double) : Int {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60
        return (hours * 60 * 60 + minutes * 60 + seconds)
    }

    private fun getTimeStringFromDouble(time: Double) : String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60
        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hours: Int, minutes: Int, seconds: Int) =
        String.format("%02d:%02d:%02d", hours, minutes, seconds)

    private fun initFab(speedDial: SpeedDialView){
        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_start_label, R.drawable.ic_start)
                .setFabSize(FloatingActionButton.SIZE_NORMAL)
                .create()
        )
        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_stop_label, R.drawable.ic_stop)
                .setFabSize(FloatingActionButton.SIZE_NORMAL)
                .create()
        )
        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_music_label, R.drawable.ic_music)
                .setFabSize(FloatingActionButton.SIZE_NORMAL)
                .create()
        )
    }

    private fun replaceActionItem(id: Int, icon: Int) =
        binding.speedDial.replaceActionItem(
            SpeedDialActionItem
                .Builder(id, icon)
                .setFabSize(FloatingActionButton.SIZE_NORMAL)
                .create(),
            0
        )

    private fun initMetValues() {
        MET_TABLE[4] = 11.5f
        MET_TABLE[5] = 8.3f
        MET_TABLE[6] = 9.8f
        MET_TABLE[7] = 11f
        MET_TABLE[8] = 11.8f
        MET_TABLE[9] = 12.8f
        MET_TABLE[10] = 14.5f
        MET_TABLE[11] = 16f
        MET_TABLE[12] = 19f
        MET_TABLE[13] = 19.8f
        MET_TABLE[14] = 23f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speed = arrayListOf()
        rout = arrayListOf(arrayListOf())
        initMetValues()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackRunBinding.inflate(layoutInflater)
        statView = ViewModelProvider(this)[StatViewModel::class.java]

        if (savedInstanceState == null) {
            initFab(binding.speedDial)
        }

        binding.speedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when(actionItem.id){
                R.id.fab_start_label -> {
                    sendCommandToService(ACTION_START)
                    if (!serviceStarted){
                        serviceStarted = true
                    }
                    if (newRunStarted) {
                        startTime = LocalDateTime.now().format(formatter)
                        distance = 0
                        newRunStarted = false
                    }
                    replaceActionItem(R.id.fab_pause_label, R.drawable.ic_pause)
                    return@OnActionSelectedListener true
                }
                R.id.fab_pause_label -> {
                    sendCommandToService(ACTION_PAUSE)
                    replaceActionItem(R.id.fab_start_label, R.drawable.ic_start)
                    return@OnActionSelectedListener true
                }
                R.id.fab_stop_label -> {
                    sendCommandToService(ACTION_STOP)
                    if (serviceStarted){
                        endTime = LocalDateTime.now().format(formatter)
                        statView.insertStat(getStat())
                        serviceStarted = false
                        newRunStarted = true
                    }
                    if (binding.speedDial.actionItems[0].id == R.id.fab_pause_label){
                        replaceActionItem(R.id.fab_start_label, R.drawable.ic_start)
                    }
                    return@OnActionSelectedListener true
                }
                R.id.fab_music_label -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("spotify:album")
                    intent.putExtra(
                        Intent.EXTRA_REFERRER,
                        Uri.parse("android-app://")
                    )
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Snackbar.make(binding.root, "Download Spotify!", Snackbar.LENGTH_LONG).show()
                    }
                    return@OnActionSelectedListener true
                }
                else -> { return@OnActionSelectedListener true }
            }
        })
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("serviceStarted", serviceStarted)
        outState.putBoolean("newRunStarted", newRunStarted)
        if (this::startTime.isInitialized){
            outState.putString("startTime", startTime)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null){
            serviceStarted = savedInstanceState.getBoolean("serviceStarted")
            newRunStarted = savedInstanceState.getBoolean("newRunStarted")
            startTime = savedInstanceState.getString("startTime").toString()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun sendCommandToService(action: String) {
        Intent(requireContext(), LocationService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    private fun getBurnedCalories() : Int {
        val t = getTimeInSec(time)
        val v = (distance / (t * 1f)) / CONVERSION_RATIO
        val burnedCaloriesPerMin: Float =
            (MET_TABLE[v.roundToInt()]?.times(3.5f) ?: 0f) * ProfileFragment.profile.weight / 200
        return ((t / 60f) * burnedCaloriesPerMin).roundToInt()
    }

    private fun getStat() = Statistics(
        id = Random.nextInt(),
        rout = Statistics.fromArrayList(rout),
        startDate = startTime,
        endDate = endTime,
        time = getTimeStringFromDouble(time),
        avarageSpeed = distance / (getTimeInSec(time) * 1f),
        distance = distance,
        burnedCalories = getBurnedCalories()
    )

    override fun onStart() {
        super.onStart()
        registerReceiverWithPermissionCheck()
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    fun registerReceiver() {
        LocalBroadcastManager.getInstance(mContext)
            .registerReceiver(locationReceiver, IntentFilter(LocationService.BR_NEW_LOCATION))
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(mContext)
            .unregisterReceiver(locationReceiver)
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }
}