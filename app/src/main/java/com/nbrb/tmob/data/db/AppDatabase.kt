package com.nbrb.tmob.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nbrb.tmob.data.db.dao.CurrencyDao
import com.nbrb.tmob.data.db.dao.RateDao
import com.nbrb.tmob.data.db.dbmodels.CurrencyDbModel
import com.nbrb.tmob.data.db.dbmodels.RateDbModel

@Database(
    entities = [
        CurrencyDbModel::class,
        RateDbModel::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    abstract fun rateDao(): RateDao
}
