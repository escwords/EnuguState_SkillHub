package com.words.storageapp.util.utilities

import android.content.Context
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.words.storageapp.domain.RecentData
import com.words.storageapp.domain.StartData
import timber.log.Timber

fun getJsonFromAsset(context: Context, fileName: String): List<StartData> {
    return try {
        context.assets.open(fileName).use { inputStream ->
            JsonReader(inputStream.reader()).use { jsonReader -> //JsonReader should be from com.google.gson.stream.JsonReader
                val skillsType = object : TypeToken<List<StartData>>() {}.type
                Gson().fromJson(jsonReader, skillsType)
            }
        }
    } catch (ex: Exception) {
        Timber.e(ex, "Error parsing startData Json")
        emptyList()
    }
}

fun getDistance(
    geoLocation: GeoLocation,
    c: Double, d: Double
): Double {
    val eGeoLocation = GeoLocation(c, d)
    return GeoFireUtils.getDistanceBetween(geoLocation, eGeoLocation)
}

fun getJsonFromAssetB(context: Context, fileName: String): List<RecentData> {
    return try {
        context.assets.open(fileName).use { inputStream ->
            JsonReader(inputStream.reader()).use { jsonReader -> //JsonReader should be from com.google.gson.stream.JsonReader
                val skillsType = object : TypeToken<List<RecentData>>() {}.type
                Gson().fromJson(jsonReader, skillsType)
            }
        }
    } catch (ex: Exception) {
        Timber.e(ex, "Error parsing startData Json")
        emptyList()
    }
}

class ItemClickListener<D>(val clickAction: (D) -> Unit) {
    fun onClick(dataItem: D) = clickAction(dataItem)
}
