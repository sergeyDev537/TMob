package com.nbrb.tmob.data.db.dbmodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class CurrencyDbModel(
    @PrimaryKey val id: Int,
    val parentId: Int,
    val code: String,
    val abbreviation: String,
    val name: String,
    val quotName: String,
    val scale: Int,
    val periodicity: Int,
    val dateStart: String,
    val dateEnd: String
)
