package hu.bme.aut.android.befitt.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import hu.bme.aut.android.befitt.model.Profile
import java.io.ByteArrayOutputStream

class PrefRepository(val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("PROFILE_PREFERENCE", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    fun saveProfile(profile: Profile) {
        editor.putString("name", profile.name)
        editor.putInt("age", profile.age)
        editor.putInt("height", profile.height)
        editor.putFloat("weight", profile.weight)
        editor.putInt("minDistance", profile.minDistance)
        editor.commit()
    }

    fun saveImage(bitmap: Bitmap) {
        editor.putString("image", encodeBitmap(bitmap))
        editor.commit()
    }

    fun encodeBitmap(bitmap: Bitmap) : String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun decodeBitmap(base64: String) : Bitmap {
        val bytes = Base64.decode(base64, 0)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun getProfile() = Profile(
        name = sharedPreferences.getString("name", "NAME")!!,
        age = sharedPreferences.getInt("age", 0),
        height = sharedPreferences.getInt("height", 0),
        weight = sharedPreferences.getFloat("weight", 0f),
        minDistance = sharedPreferences.getInt("minDistance", 0)
    )

    fun getImage() : Bitmap? {
        val str = sharedPreferences.getString("image", "")!!
        if (str.isEmpty())
            return null
        else
            return decodeBitmap(str)
    }

    fun clearData() {
        editor.clear()
        editor.commit()
    }
}