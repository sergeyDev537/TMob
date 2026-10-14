package com.nbrb.tmob.domain.entity

data class CurrencyBasket(
    val date: String,
    val basketValue: Double,
    val rubRate: Double,
    val usdRate: Double,
    val cnyRate: Double,
    val basketPrevDayChangePercent: Double? = null,
    val rubPrevDayChangePercent: Double? = null,
    val usdPrevDayChangePercent: Double? = null,
    val cnyPrevDayChangePercent: Double? = null,
    val basketYearChangePercent: Double? = null,
    val rubYearChangePercent: Double? = null,
    val usdYearChangePercent: Double? = null,
    val cnyYearChangePercent: Double? = null
) {
    companion object {
        const val RUB_WEIGHT = 0.6
        const val USD_WEIGHT = 0.3
        const val CNY_WEIGHT = 0.1

        const val RUB_CODE = "RUB"
        const val USD_CODE = "USD"
        const val CNY_CODE = "CNY"
    }
}
