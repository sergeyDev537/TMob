package com.nbrb.tmob.ui.screens.charts

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nbrb.tmob.data.managers.NetworkManager
import com.nbrb.tmob.domain.entity.ChartPeriod
import com.nbrb.tmob.domain.entity.ChartPoint
import com.nbrb.tmob.domain.entity.Currency
import com.nbrb.tmob.domain.usecase.GetChartDynamicsUseCase
import com.nbrb.tmob.domain.usecase.GetCurrenciesUseCase
import com.nbrb.tmob.domain.usecase.GetCurrentDateUseCase
import com.nbrb.tmob.domain.usecase.GetRatesUseCase
import com.nbrb.tmob.ui.screens.charts.ChartsStore.Intent
import com.nbrb.tmob.ui.screens.charts.ChartsStore.Label
import com.nbrb.tmob.ui.screens.charts.ChartsStore.State
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ChartsStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object ClickBack : Intent
        data class SelectCurrency(val currency: Currency) : Intent
        data class SelectPeriod(val period: ChartPeriod) : Intent
        data object ClickRefresh : Intent
    }

    data class State(
        val date: String = "",
        val currencies: List<Currency> = emptyList(),
        val selectedCurrency: Currency? = null,
        val selectedPeriod: ChartPeriod = ChartPeriod.WEEK,
        val points: List<ChartPoint> = emptyList(),
        val currentRate: Double? = null,
        val isLoading: Boolean = true,
        val error: String? = null
    )

    sealed interface Label {
        data object OpenBack : Label
        data class ShowOfflineCachedNotice(val date: String) : Label
        data object ShowOfflineNoDataNotice : Label
    }
}

class ChartsStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getCurrenciesUseCase: GetCurrenciesUseCase,
    private val getChartDynamicsUseCase: GetChartDynamicsUseCase,
    private val getRatesUseCase: GetRatesUseCase,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
    private val networkManager: NetworkManager
) {

    fun create(): ChartsStore =
        object : ChartsStore, Store<Intent, State, Label> by storeFactory.create(
            name = "ChartsStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data class DateLoaded(val date: String) : Action
        data class CurrenciesLoaded(val currencies: List<Currency>) : Action
    }

    private sealed interface Msg {
        data class DateLoaded(val date: String) : Msg
        data class StartLoading(val selectedCurrency: Currency?, val period: ChartPeriod) : Msg
        data class ChartsDataResult(
            val currencies: List<Currency>? = null,
            val selectedCurrency: Currency? = null,
            val points: List<ChartPoint>? = null,
            val currentRate: Double? = null,
            val isLoading: Boolean = false,
            val error: String? = null
        ) : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                val currentDate = getCurrentDateUseCase()
                dispatch(Action.DateLoaded(currentDate))
            }
            scope.launch {
                getCurrenciesUseCase().collect { currencyList ->
                    dispatch(Action.CurrenciesLoaded(currencyList))
                }
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.ClickBack -> publish(Label.OpenBack)
                is Intent.SelectCurrency -> {
                    loadDynamicsAndRate(intent.currency, getState().selectedPeriod, getState)
                }
                is Intent.SelectPeriod -> {
                    val selected = getState().selectedCurrency
                    if (selected != null) {
                        loadDynamicsAndRate(selected, intent.period, getState)
                    }
                }
                Intent.ClickRefresh -> {
                    val selected = getState().selectedCurrency
                    if (selected != null) {
                        loadDynamicsAndRate(selected, getState().selectedPeriod, getState)
                    } else {
                        refreshAll(getState)
                    }
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                is Action.DateLoaded -> {
                    dispatch(Msg.DateLoaded(action.date))
                }
                is Action.CurrenciesLoaded -> {
                    if (action.currencies.isEmpty()) {
                        val isConnected = networkManager.isNetworkConnected
                        if (!isConnected) {
                            publish(Label.ShowOfflineNoDataNotice)
                        }
                        dispatch(
                            Msg.ChartsDataResult(
                                currencies = emptyList(),
                                selectedCurrency = null,
                                points = emptyList(),
                                isLoading = false,
                                error = "No currencies"
                            )
                        )
                    } else {
                        val selected = getState().selectedCurrency
                            ?: action.currencies.find { it.abbreviation.equals("USD", ignoreCase = true) }
                            ?: action.currencies.first()
                        loadDynamicsAndRate(selected, getState().selectedPeriod, getState, action.currencies)
                    }
                }
            }
        }

        private fun refreshAll(getState: () -> State) {
            scope.launch {
                dispatch(Msg.StartLoading(null, getState().selectedPeriod))
                val currencyList = getCurrenciesUseCase().firstOrNull() ?: emptyList()
                if (currencyList.isEmpty()) {
                    val isConnected = networkManager.isNetworkConnected
                    if (!isConnected) {
                        publish(Label.ShowOfflineNoDataNotice)
                    }
                    dispatch(
                        Msg.ChartsDataResult(
                            currencies = emptyList(),
                            selectedCurrency = null,
                            points = emptyList(),
                            isLoading = false,
                            error = "No currencies"
                        )
                    )
                } else {
                    val selected = currencyList.find { it.abbreviation.equals("USD", ignoreCase = true) }
                        ?: currencyList.first()
                    loadDynamicsAndRate(selected, getState().selectedPeriod, getState, currencyList)
                }
            }
        }

        private fun loadDynamicsAndRate(
            currency: Currency,
            period: ChartPeriod,
            getState: () -> State,
            currencies: List<Currency>? = null
        ) {
            scope.launch {
                dispatch(Msg.StartLoading(currency, period))
                getChartDynamicsUseCase(currency.id, period)
                    .catch { e ->
                        val isConnected = networkManager.isNetworkConnected
                        val currentPoints = getState().points
                        if (!isConnected) {
                            if (currentPoints.size >= 5) {
                                val displayDate = if (getState().date.isNotEmpty()) getState().date else (currentPoints.lastOrNull()?.date ?: "")
                                publish(Label.ShowOfflineCachedNotice(displayDate))
                            } else {
                                publish(Label.ShowOfflineNoDataNotice)
                            }
                        }
                        dispatch(
                            Msg.ChartsDataResult(
                                currencies = currencies,
                                selectedCurrency = currency,
                                points = if (currentPoints.size >= 5) currentPoints else emptyList(),
                                currentRate = getState().currentRate,
                                isLoading = false,
                                error = e.message
                            )
                        )
                    }
                    .collect { pointsList ->
                        val isConnected = networkManager.isNetworkConnected
                        val displayDate = if (getState().date.isNotEmpty()) getState().date else (pointsList.lastOrNull()?.date ?: "")

                        if (pointsList.size < 3) {
                            if (!isConnected) {
                                publish(Label.ShowOfflineNoDataNotice)
                            }
                            dispatch(
                                Msg.ChartsDataResult(
                                    currencies = currencies,
                                    selectedCurrency = currency,
                                    points = emptyList(),
                                    currentRate = getState().currentRate,
                                    isLoading = false,
                                    error = "Insufficient data"
                                )
                            )
                        } else {
                            if (!isConnected) {
                                publish(Label.ShowOfflineCachedNotice(displayDate))
                            }
                            loadCurrentRate(currency, getState().date, pointsList, currencies)
                        }
                    }
            }
        }

        private fun loadCurrentRate(
            currency: Currency,
            date: String,
            pointsList: List<ChartPoint>,
            currencies: List<Currency>? = null
        ) {
            if (date.isEmpty()) {
                dispatch(
                    Msg.ChartsDataResult(
                        currencies = currencies,
                        selectedCurrency = currency,
                        points = pointsList,
                        currentRate = null,
                        isLoading = false,
                        error = null
                    )
                )
                return
            }
            scope.launch {
                getRatesUseCase(date)
                    .catch {
                        dispatch(
                            Msg.ChartsDataResult(
                                currencies = currencies,
                                selectedCurrency = currency,
                                points = pointsList,
                                currentRate = null,
                                isLoading = false,
                                error = null
                            )
                        )
                    }
                    .collect { rates ->
                        val found = rates.find { it.abbreviation.equals(currency.abbreviation, ignoreCase = true) }
                        dispatch(
                            Msg.ChartsDataResult(
                                currencies = currencies,
                                selectedCurrency = currency,
                                points = pointsList,
                                currentRate = found?.officialRate,
                                isLoading = false,
                                error = null
                            )
                        )
                    }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.DateLoaded -> copy(date = msg.date)
                is Msg.StartLoading -> copy(
                    selectedCurrency = msg.selectedCurrency ?: selectedCurrency,
                    selectedPeriod = msg.period,
                    isLoading = true
                )
                is Msg.ChartsDataResult -> copy(
                    currencies = msg.currencies ?: currencies,
                    selectedCurrency = msg.selectedCurrency ?: selectedCurrency,
                    points = msg.points ?: points,
                    currentRate = msg.currentRate ?: currentRate,
                    isLoading = msg.isLoading,
                    error = if ((msg.points ?: points).isNotEmpty()) null else msg.error
                )
            }
    }
}
