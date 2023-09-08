package hu.bme.aut.android.befitt.ui.statistics

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import hu.bme.aut.android.befitt.R
import hu.bme.aut.android.befitt.databinding.FragmentStatDetailBinding
import hu.bme.aut.android.befitt.model.Statistics

class StatDetailFragment : Fragment() {
    private var selectedStat: Statistics? = null
    private lateinit var binding: FragmentStatDetailBinding
    private lateinit var mContext: Context
    private lateinit var rout: ArrayList<ArrayList<LatLng>>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(mContext, R.raw.style_night)
        )

        googleMap.setMinZoomPreference(15f)

        for (r in rout){
            val polylineOptions = PolylineOptions().addAll(r)
            val polyline = googleMap.addPolyline(polylineOptions)
            polyline.color = Color.RED
            polyline.width = 10f
        }

        val location = rout.lastOrNull()?.lastOrNull() ?: LatLng(47.0, 19.0)

        googleMap.addMarker(MarkerOptions().position(location).title("Marker in hungary"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
    }

    companion object {
        private const val KEY_STAT = "KEY_STAT"

        fun newInstance(stat: Statistics): StatDetailFragment {
            val args = Bundle()
            args.putSerializable(KEY_STAT, stat)

            val result = StatDetailFragment()
            result.arguments = args
            return result
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            selectedStat = args.getSerializable(KEY_STAT) as Statistics
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvStart.text = selectedStat?.startDate
        binding.tvEnd.text = selectedStat?.endDate
        binding.tvTime.text = selectedStat?.time.toString()
        binding.tvSpeed.text = "${selectedStat?.avarageSpeed} m/s"
        binding.tvDistance.text = "${selectedStat?.distance} m"
        binding.tvBurnedCalories.text = "${selectedStat?.burnedCalories} Kcal"
        rout = selectedStat?.rout?.let { Statistics.fromString(it) } ?: arrayListOf(arrayListOf())

        val mapFragment = childFragmentManager.findFragmentById(R.id.statMap) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

}