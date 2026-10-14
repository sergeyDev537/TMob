package com.nbrb.tmob.domain.usecase

import com.nbrb.tmob.domain.entity.ChartPeriod
import com.nbrb.tmob.domain.entity.ChartPoint
import com.nbrb.tmob.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChartDynamicsUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    operator fun invoke(currencyId: Int, period: ChartPeriod): Flow<List<ChartPoint>> {
        return repository.getChartDynamics(currencyId, period)
    }
}
