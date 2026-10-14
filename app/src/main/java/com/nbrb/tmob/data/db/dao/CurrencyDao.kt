package com.nbrb.tmob.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nbrb.tmob.data.db.dbmodels.CurrencyDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM currencies")
    fun getCurrencies(): Flow<List<CurrencyDbModel>>

    @Query("SELECT * FROM currencies")
    suspend fun getCurrenciesSync(): List<CurrencyDbModel>

    @Query("SELECT * FROM currencies WHERE id = :id LIMIT 1")
    suspend fun getCurrencyById(id: Int): CurrencyDbModel?

    @Query("SELECT * FROM currencies WHERE parentId = :parentId")
    suspend fun getCurrenciesByParentId(parentId: Int): List<CurrencyDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<CurrencyDbModel>)
}
