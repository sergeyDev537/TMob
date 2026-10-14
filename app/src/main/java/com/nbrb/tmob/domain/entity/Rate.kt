package com.nbrb.tmob.domain.entity

data class Rate(
    val id: Int,
    val code: String,
    val abbreviation: String,
    val name: String,
    val scale: Int,
    val officialRate: Double,
    val date: String,
    val dynamicsVsPreviousDay: Double? = null
) {
    val ratePerUnit: Double
        get() = if (scale > 0) officialRate / scale else officialRate
}
