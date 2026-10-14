package com.nbrb.tmob.domain.entity

enum class ChartPeriod(val days: Int) {
    WEEK(days = 7),
    MONTH(days = 30),
    QUARTER(days = 90);

    companion object {
        val DEFAULT = WEEK
    }
}
