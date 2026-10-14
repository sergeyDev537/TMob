package com.nbrb.tmob.domain.usecase

import com.nbrb.tmob.domain.repository.CurrencyRepository
import javax.inject.Inject

class GetCurrentDateUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(): String {
        return repository.getCurrentDate()
    }
}
