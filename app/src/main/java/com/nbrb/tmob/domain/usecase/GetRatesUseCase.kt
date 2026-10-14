package com.nbrb.tmob.domain.usecase

import com.nbrb.tmob.domain.entity.Rate
import com.nbrb.tmob.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRatesUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    operator fun invoke(date: String): Flow<List<Rate>> {
        return repository.getRates(date)
    }
}
