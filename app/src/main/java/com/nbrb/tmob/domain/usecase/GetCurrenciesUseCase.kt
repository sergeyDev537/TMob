package com.nbrb.tmob.domain.usecase

import com.nbrb.tmob.domain.entity.Currency
import com.nbrb.tmob.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrenciesUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    operator fun invoke(): Flow<List<Currency>> {
        return repository.getCurrencies()
    }
}
