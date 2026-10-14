package com.nbrb.tmob.ui.screens.courses

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nbrb.tmob.data.managers.NetworkManager
import com.nbrb.tmob.domain.entity.Rate
import com.nbrb.tmob.domain.usecase.GetCurrentDateUseCase
import com.nbrb.tmob.domain.usecase.GetRatesUseCase
import com.nbrb.tmob.domain.usecase.RefreshRatesUseCase
import com.nbrb.tmob.ui.screens.courses.CoursesStore.Intent
import com.nbrb.tmob.ui.screens.courses.CoursesStore.Label
import com.nbrb.tmob.ui.screens.courses.CoursesStore.State
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

interface CoursesStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object ClickBack : Intent
        data object ClickRefresh : Intent
    }

    data class State(
        val date: String = "",
        val rates: List<Rate> = emptyList(),
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val error: String? = null
    )

    sealed interface Label {
        data object OpenBack : Label
        data class ShowOfflineCachedNotice(val date: String) : Label
        data object ShowOfflineNoDataNotice : Label
    }
}

class CoursesStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getRatesUseCase: GetRatesUseCase,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
    private val refreshRatesUseCase: RefreshRatesUseCase,
    private val networkManager: NetworkManager
) {

    fun create(): CoursesStore =
        object : CoursesStore, Store<Intent, State, Label> by storeFactory.create(
            name = "CoursesStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data class DateLoaded(val date: String) : Action
    }

    private sealed interface Msg {
        data class StartLoading(val date: String) : Msg
        data class StartRefreshing(val isRefreshing: Boolean) : Msg
        data class RatesResult(
            val date: String,
            val rates: List<Rate>,
            val isRefreshing: Boolean = false,
            val error: String? = null
        ) : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                val currentDate = getCurrentDateUseCase()
                dispatch(Action.DateLoaded(currentDate))
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.ClickBack -> publish(Label.OpenBack)
                Intent.ClickRefresh -> {
                    val date = getState().date
                    if (date.isNotEmpty()) {
                        refresh(date, getState)
                    }
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                is Action.DateLoaded -> {
                    loadRates(action.date, getState)
                }
            }
        }

        private fun loadRates(date: String, getState: () -> State) {
            scope.launch {
                getRatesUseCase(date)
                    .onStart {
                        dispatch(Msg.StartLoading(date))
                    }
                    .catch { e ->
                        val isConnected = networkManager.isNetworkConnected
                        if (!isConnected) {
                            if (getState().rates.isNotEmpty()) {
                                publish(Label.ShowOfflineCachedNotice(date))
                            } else {
                                publish(Label.ShowOfflineNoDataNotice)
                            }
                        }
                        dispatch(
                            Msg.RatesResult(
                                date = date,
                                rates = getState().rates,
                                error = e.message
                            )
                        )
                    }
                    .collect { ratesList ->
                        val isConnected = networkManager.isNetworkConnected
                        if (!isConnected) {
                            if (ratesList.isNotEmpty()) {
                                publish(Label.ShowOfflineCachedNotice(date))
                            } else {
                                publish(Label.ShowOfflineNoDataNotice)
                            }
                        }
                        dispatch(
                            Msg.RatesResult(
                                date = date,
                                rates = ratesList,
                                error = null
                            )
                        )
                    }
            }
        }

        private fun refresh(date: String, getState: () -> State) {
            scope.launch {
                dispatch(Msg.StartLoading(date))
                refreshRatesUseCase(date)
                    .onSuccess {
                        dispatch(
                            Msg.RatesResult(
                                date = date,
                                rates = getState().rates,
                                isRefreshing = false,
                                error = null
                            )
                        )
                    }
                    .onFailure { e ->
                        val isConnected = networkManager.isNetworkConnected
                        if (!isConnected) {
                            if (getState().rates.isNotEmpty()) {
                                publish(Label.ShowOfflineCachedNotice(date))
                            } else {
                                publish(Label.ShowOfflineNoDataNotice)
                            }
                        }
                        dispatch(
                            Msg.RatesResult(
                                date = date,
                                rates = getState().rates,
                                isRefreshing = false,
                                error = e.message
                            )
                        )
                    }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.StartLoading -> copy(date = msg.date, isLoading = true)
                is Msg.StartRefreshing -> copy(isRefreshing = msg.isRefreshing)
                is Msg.RatesResult -> copy(
                    date = msg.date,
                    rates = msg.rates,
                    isLoading = false,
                    isRefreshing = msg.isRefreshing,
                    error = if (msg.rates.isNotEmpty()) null else msg.error
                )
            }
    }
}
