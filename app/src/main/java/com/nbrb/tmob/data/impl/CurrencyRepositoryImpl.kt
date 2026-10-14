package com.nbrb.tmob.data.impl

import com.nbrb.tmob.data.db.dao.CurrencyDao
import com.nbrb.tmob.data.db.dao.RateDao
import com.nbrb.tmob.data.db.dbmodels.RateDbModel
import com.nbrb.tmob.data.managers.TimeManager
import com.nbrb.tmob.data.mappers.toDbModel
import com.nbrb.tmob.data.mappers.toDomain
import com.nbrb.tmob.data.network.NbrbApiService
import com.nbrb.tmob.domain.entity.CurrencyBasket
import com.nbrb.tmob.domain.entity.ChartPeriod
import com.nbrb.tmob.domain.entity.ChartPoint
import com.nbrb.tmob.domain.entity.Currency
import com.nbrb.tmob.domain.entity.Rate
import com.nbrb.tmob.domain.repository.CurrencyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val apiService: NbrbApiService,
    private val currencyDao: CurrencyDao,
    private val rateDao: RateDao,
    private val timeManager: TimeManager
) : CurrencyRepository {

    override fun getCurrencies(): Flow<List<Currency>> {
        return currencyDao.getCurrencies()
            .map { dbModels -> dbModels.map { it.toDomain() } }
            .onStart {
                try {
                    if (currencyDao.getCurrenciesSync().isEmpty()) {
                        fetchAndStoreCurrencies()
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override fun getRates(date: String): Flow<List<Rate>> {
        val previousDate = getPreviousDayDate(date)
        return combine(
            rateDao.getRatesByDate(date),
            rateDao.getRatesByDate(previousDate)
        ) { currentDbRates, previousDbRates ->
            val previousMap = previousDbRates.associateBy { it.abbreviation }
            currentDbRates.map { currentRate ->
                val prevRateModel = previousMap[currentRate.abbreviation]
                val prevPerUnit = prevRateModel?.let { if (it.scale > 0) it.officialRate / it.scale else it.officialRate }
                currentRate.toDomain(previousDayRate = prevPerUnit)
            }
        }.onStart {
            try {
                refreshRatesIfNeeded(date)
                refreshRatesIfNeeded(previousDate)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
    }

    override suspend fun refreshRates(date: String): Result<Unit> {
        return try {
            fetchAndStoreRatesForDate(date)
            val previousDate = getPreviousDayDate(date)
            fetchAndStoreRatesForDate(previousDate)
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override fun getCurrencyBasket(date: String): Flow<CurrencyBasket> {
        val previousDate = getPreviousDayDate(date)
        val dec31Date = getPreviousYearDec31Date(date)

        return combine(
            rateDao.getRatesByDate(date),
            rateDao.getRatesByDate(previousDate),
            rateDao.getRatesByDate(dec31Date)
        ) { currentRates, prevRates, yearRates ->
            val currTriple = calculateBasketTriple(currentRates)
            val prevTriple = calculateBasketTriple(prevRates)
            val yearTriple = calculateBasketTriple(yearRates)

            val currBasket = currTriple.first
            val prevBasket = prevTriple.first
            val yearBasket = yearTriple.first

            CurrencyBasket(
                date = date,
                basketValue = currBasket,
                rubRate = currTriple.second[0],
                usdRate = currTriple.second[1],
                cnyRate = currTriple.second[2],
                basketPrevDayChangePercent = calcPercentChange(currBasket, prevBasket),
                rubPrevDayChangePercent = calcPercentChange(currTriple.second[0], prevTriple.second[0]),
                usdPrevDayChangePercent = calcPercentChange(currTriple.second[1], prevTriple.second[1]),
                cnyPrevDayChangePercent = calcPercentChange(currTriple.second[2], prevTriple.second[2]),
                basketYearChangePercent = calcPercentChange(currBasket, yearBasket),
                rubYearChangePercent = calcPercentChange(currTriple.second[0], yearTriple.second[0]),
                usdYearChangePercent = calcPercentChange(currTriple.second[1], yearTriple.second[1]),
                cnyYearChangePercent = calcPercentChange(currTriple.second[2], yearTriple.second[2])
            )
        }.onStart {
            try {
                refreshRatesIfNeeded(date)
                refreshRatesIfNeeded(previousDate)
                refreshRatesIfNeeded(dec31Date)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
    }

    override fun getChartDynamics(currencyId: Int, period: ChartPeriod): Flow<List<ChartPoint>> {
        return flow {
            val endDate = getCurrentDate()
            val startDate = getStartDateForPeriod(endDate, period.days)

            val linkedIds = getLinkedCurrencyIds(currencyId)

            try {
                for (currId in linkedIds) {
                    val dtos = apiService.getDynamics(
                        curId = currId,
                        startDate = startDate,
                        endDate = endDate
                    )
                    val currObj = currencyDao.getCurrencyById(currId)
                    val code = currObj?.code ?: ""
                    val abbreviation = currObj?.abbreviation ?: ""
                    val scale = currObj?.scale ?: 1
                    val name = currObj?.name ?: ""

                    val dbRates = dtos.mapNotNull { dto ->
                        val rate = dto.curOfficialRate ?: return@mapNotNull null
                        val formattedDate = formatIsoDateToShort(dto.date)
                        RateDbModel(
                            id = currId,
                            date = formattedDate,
                            code = code,
                            abbreviation = abbreviation,
                            scale = scale,
                            name = name,
                            officialRate = rate
                        )
                    }
                    rateDao.insertRates(dbRates)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }

            val cachedRates = rateDao.getRatesByIdsAndDateRangeSync(linkedIds, startDate, endDate)
            val points = cachedRates.distinctBy { formatIsoDateToShort(it.date) }.map { model ->
                val perUnit = if (model.scale > 0) model.officialRate / model.scale else model.officialRate
                ChartPoint(
                    date = formatIsoDateToShort(model.date),
                    ratePerUnit = perUnit
                )
            }
            emit(points)
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
    }

    override suspend fun getCurrentDate(): String {
        return timeManager.getCurrentDateFormatted()
    }

    private fun calculateBasketTriple(rates: List<RateDbModel>): Pair<Double, List<Double>> {
        val ratesMap = rates.associateBy { it.abbreviation }
        val rub = ratesMap[CurrencyBasket.RUB_CODE]
        val usd = ratesMap[CurrencyBasket.USD_CODE]
        val cny = ratesMap[CurrencyBasket.CNY_CODE]

        val rubUnit = rub?.let { if (it.scale > 0) it.officialRate / it.scale else it.officialRate } ?: 0.0
        val usdUnit = usd?.let { if (it.scale > 0) it.officialRate / it.scale else it.officialRate } ?: 0.0
        val cnyUnit = cny?.let { if (it.scale > 0) it.officialRate / it.scale else it.officialRate } ?: 0.0

        val basketVal = if (rubUnit > 0 && usdUnit > 0 && cnyUnit > 0) {
            rubUnit.pow(CurrencyBasket.RUB_WEIGHT) *
                    usdUnit.pow(CurrencyBasket.USD_WEIGHT) *
                    cnyUnit.pow(CurrencyBasket.CNY_WEIGHT)
        } else 0.0

        return Pair(basketVal, listOf(rubUnit, usdUnit, cnyUnit))
    }

    private fun calcPercentChange(current: Double, base: Double): Double? {
        if (current <= 0 || base <= 0) return null
        return ((base / current) - 1.0) * 100.0
    }

    private suspend fun getLinkedCurrencyIds(currencyId: Int): List<Int> {
        val selectedCurr = currencyDao.getCurrencyById(currencyId) ?: return listOf(currencyId)
        val parentId = selectedCurr.parentId
        if (parentId <= 0) return listOf(currencyId)

        val linkedCurrencies = currencyDao.getCurrenciesByParentId(parentId)
        if (linkedCurrencies.isEmpty()) return listOf(currencyId)

        return linkedCurrencies.map { it.id }
    }

    private suspend fun refreshRatesIfNeeded(date: String) {
        val existing = rateDao.getRatesByDateSync(date)
        if (existing.isEmpty()) {
            fetchAndStoreRatesForDate(date)
        }
    }

    private suspend fun fetchAndStoreCurrencies() {
        val dtos = apiService.getCurrencies()
        val dbModels = dtos.map { it.toDbModel() }
        currencyDao.insertCurrencies(dbModels)
    }

    private suspend fun fetchAndStoreRatesForDate(date: String) {
        val dtos = apiService.getRates(date = date, periodicity = 0)
        val dbModels = dtos.map { it.toDbModel(formattedDate = date) }
        rateDao.insertRates(dbModels)
    }

    private fun getPreviousDayDate(dateStr: String): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        val date = sdf.parse(dateStr) ?: Date()
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return sdf.format(cal.time)
    }

    private fun getPreviousYearDec31Date(dateStr: String): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        val date = sdf.parse(dateStr) ?: Date()
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.time = date
        val year = cal.get(Calendar.YEAR)
        return "${year - 1}-12-31"
    }

    private fun getStartDateForPeriod(endDateStr: String, daysBack: Int): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        val date = sdf.parse(endDateStr) ?: Date()
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, -daysBack)
        return sdf.format(cal.time)
    }

    private fun formatIsoDateToShort(isoDateStr: String): String {
        return if (isoDateStr.length >= 10) {
            isoDateStr.substring(0, 10)
        } else {
            isoDateStr
        }
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }
}
