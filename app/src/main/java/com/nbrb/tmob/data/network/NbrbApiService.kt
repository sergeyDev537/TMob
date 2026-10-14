package com.nbrb.tmob.data.network

import com.nbrb.tmob.data.network.dto.CurrencyDto
import com.nbrb.tmob.data.network.dto.RateDto
import com.nbrb.tmob.data.network.dto.RateShortDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NbrbApiService {

    @GET("exrates/currencies")
    suspend fun getCurrencies(): List<CurrencyDto>

    @GET("exrates/rates")
    suspend fun getRates(
        @Query("ondate") date: String? = null,
        @Query("periodicity") periodicity: Int = 0
    ): List<RateDto>

    @GET("exrates/rates/dynamics/{cur_id}")
    suspend fun getDynamics(
        @Path("cur_id") curId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): List<RateShortDto>
}
