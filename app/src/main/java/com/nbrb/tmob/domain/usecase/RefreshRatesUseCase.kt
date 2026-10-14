package com.nbrb.tmob.domain.usecase

import com.nbrb.tmob.domain.repository.CurrencyRepository
import javax.inject.Inject

class RefreshRatesUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(date: String): Result<Unit> {
        return repository.refreshRates(date)
    }
}
