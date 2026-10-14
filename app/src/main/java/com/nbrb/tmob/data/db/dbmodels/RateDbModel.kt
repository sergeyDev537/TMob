package com.nbrb.tmob.data.db.dbmodels

import androidx.room.Entity

@Entity(tableName = "rates", primaryKeys = ["id", "date"])
data class RateDbModel(
    val id: Int,
    val date: String,
    val code: String,
    val abbreviation: String,
    val scale: Int,
    val name: String,
    val officialRate: Double
)
