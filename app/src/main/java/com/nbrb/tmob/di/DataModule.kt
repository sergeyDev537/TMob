package com.nbrb.tmob.di

import com.nbrb.tmob.data.db.dao.CurrencyDao
import com.nbrb.tmob.data.db.dao.RateDao
import com.nbrb.tmob.data.impl.CurrencyRepositoryImpl
import com.nbrb.tmob.data.managers.TimeManager
import com.nbrb.tmob.data.network.NbrbApiService
import com.nbrb.tmob.domain.repository.CurrencyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Singleton
    @Provides
    fun provideCurrencyRepository(
        apiService: NbrbApiService,
        currencyDao: CurrencyDao,
        rateDao: RateDao,
        timeManager: TimeManager,
    ): CurrencyRepository = CurrencyRepositoryImpl(
        apiService = apiService,
        currencyDao = currencyDao,
        rateDao = rateDao,
        timeManager = timeManager
    )
}
