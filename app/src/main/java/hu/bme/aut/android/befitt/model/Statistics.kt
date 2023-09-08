package hu.bme.aut.android.befitt.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class Statistics (
    val id: Int,
    val rout: String?,
    val startDate: String,
    val endDate: String,
    val time: String,
    val avarageSpeed: Float,
    val distance: Int,
    val burnedCalories: Int
) : Serializable {
    companion object {
        fun fromArrayList(rout: ArrayList<ArrayList<LatLng>>?) : String {
            val gson = Gson()
            return gson.toJson(rout)
        }

        fun fromString(value: String) : ArrayList<ArrayList<LatLng>>? {
            val listType = object : TypeToken<ArrayList<ArrayList<LatLng>>>() {}.type
            return Gson().fromJson(value, listType)
        }
    }
}