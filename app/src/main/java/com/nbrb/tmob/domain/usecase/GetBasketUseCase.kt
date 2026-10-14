package com.nbrb.tmob.domain.usecase

import com.nbrb.tmob.domain.entity.CurrencyBasket
import com.nbrb.tmob.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBasketUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    operator fun invoke(date: String): Flow<CurrencyBasket> {
        return repository.getCurrencyBasket(date)
    }
}
