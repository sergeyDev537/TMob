package com.nbrb.tmob.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nbrb.tmob.data.db.dbmodels.RateDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface RateDao {

    @Query("SELECT * FROM rates WHERE date = :date")
    fun getRatesByDate(date: String): Flow<List<RateDbModel>>

    @Query("SELECT * FROM rates WHERE date = :date")
    suspend fun getRatesByDateSync(date: String): List<RateDbModel>

    @Query("SELECT * FROM rates WHERE id = :id AND date = :date LIMIT 1")
    suspend fun getRateByIdAndDate(id: Int, date: String): RateDbModel?

    @Query("SELECT * FROM rates WHERE abbreviation = :abbreviation AND date = :date LIMIT 1")
    suspend fun getRateByAbbreviationAndDate(abbreviation: String, date: String): RateDbModel?

    @Query("SELECT * FROM rates WHERE id IN (:ids) AND date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getRatesByIdsAndDateRange(ids: List<Int>, startDate: String, endDate: String): Flow<List<RateDbModel>>

    @Query("SELECT * FROM rates WHERE id IN (:ids) AND date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getRatesByIdsAndDateRangeSync(ids: List<Int>, startDate: String, endDate: String): List<RateDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<RateDbModel>)
}
