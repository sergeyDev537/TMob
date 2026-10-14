package com.nbrb.tmob.ui.screens.charts

import com.nbrb.tmob.domain.entity.ChartPeriod
import com.nbrb.tmob.domain.entity.Currency
import kotlinx.coroutines.flow.StateFlow

interface ChartsComponent {

    val model: StateFlow<ChartsStore.State>

    fun onBackClick()

    fun onCurrencySelected(currency: Currency)

    fun onPeriodSelected(period: ChartPeriod)

    fun onRefreshClick()
}
