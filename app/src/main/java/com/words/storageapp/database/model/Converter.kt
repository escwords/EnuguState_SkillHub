package com.words.storageapp.database.model

import androidx.room.TypeConverter
import java.util.*

class Converter {

    @TypeConverter
    fun dateToTimeStamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun timeStampToDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }
}