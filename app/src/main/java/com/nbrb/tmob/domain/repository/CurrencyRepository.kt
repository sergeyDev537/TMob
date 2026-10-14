package com.nbrb.tmob.domain.repository

import com.nbrb.tmob.domain.entity.ChartPeriod
import com.nbrb.tmob.domain.entity.ChartPoint
import com.nbrb.tmob.domain.entity.Currency
import com.nbrb.tmob.domain.entity.CurrencyBasket
import com.nbrb.tmob.domain.entity.Rate
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    fun getCurrencies(): Flow<List<Currency>>

    fun getRates(date: String): Flow<List<Rate>>

    suspend fun refreshRates(date: String): Result<Unit>

    fun getCurrencyBasket(date: String): Flow<CurrencyBasket>

    fun getChartDynamics(currencyId: Int, period: ChartPeriod): Flow<List<ChartPoint>>

    suspend fun getCurrentDate(): String
}
