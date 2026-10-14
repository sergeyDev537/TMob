package com.nbrb.tmob.data.managers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeManager @Inject constructor() {

    fun getCurrentDateFormatted(): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        return dateFormat.format(Date())
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }
}
