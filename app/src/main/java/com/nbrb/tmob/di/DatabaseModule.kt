package com.nbrb.tmob.di

import android.content.Context
import androidx.room.Room
import com.nbrb.tmob.data.db.AppDatabase
import com.nbrb.tmob.data.db.dao.CurrencyDao
import com.nbrb.tmob.data.db.dao.RateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    companion object {
        private const val DB_NAME = "tmob_database.db"
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    @Singleton
    fun provideCurrencyDao(database: AppDatabase): CurrencyDao {
        return database.currencyDao()
    }

    @Provides
    @Singleton
    fun provideRateDao(database: AppDatabase): RateDao {
        return database.rateDao()
    }
}
