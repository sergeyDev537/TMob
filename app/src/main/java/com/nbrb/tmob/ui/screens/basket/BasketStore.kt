package com.nbrb.tmob.ui.screens.basket

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nbrb.tmob.data.managers.NetworkManager
import com.nbrb.tmob.domain.entity.CurrencyBasket
import com.nbrb.tmob.domain.usecase.GetBasketUseCase
import com.nbrb.tmob.domain.usecase.GetCurrentDateUseCase
import com.nbrb.tmob.domain.usecase.RefreshRatesUseCase
import com.nbrb.tmob.ui.screens.basket.BasketStore.Intent
import com.nbrb.tmob.ui.screens.basket.BasketStore.Label
import com.nbrb.tmob.ui.screens.basket.BasketStore.State
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

interface BasketStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object ClickBack : Intent
        data class SelectDate(val date: String) : Intent
        data object ClickFetch : Intent
    }

    data class State(
        val date: String = "",
        val basket: CurrencyBasket? = null,
        val isLoading: Boolean = true,
        val error: String? = null
    )

    sealed interface Label {
        data object OpenBack : Label
        data class ShowOfflineCachedNotice(val date: String) : Label
        data object ShowOfflineNoDataNotice : Label
    }
}

class BasketStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getBasketUseCase: GetBasketUseCase,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
    private val refreshRatesUseCase: RefreshRatesUseCase,
    private val networkManager: NetworkManager
) {

    fun create(): BasketStore =
        object : BasketStore, Store<Intent, State, Label> by storeFactory.create(
            name = "BasketStore",
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
        data class BasketResult(
            val date: String,
            val basket: CurrencyBasket?,
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
                is Intent.SelectDate -> {
                    loadBasket(intent.date, getState)
                }
                Intent.ClickFetch -> {
                    val date = getState().date
                    if (date.isNotEmpty()) {
                        refreshAndLoad(date, getState)
                    }
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                is Action.DateLoaded -> {
                    loadBasket(action.date, getState)
                }
            }
        }

        private fun loadBasket(date: String, getState: () -> State) {
            scope.launch {
                getBasketUseCase(date)
                    .onStart {
                        dispatch(Msg.StartLoading(date))
                    }
                    .catch { e ->
                        val isConnected = networkManager.isNetworkConnected
                        val currentBasket = getState().basket
                        if (!isConnected) {
                            if (currentBasket != null && currentBasket.basketValue > 0) {
                                publish(Label.ShowOfflineCachedNotice(date))
                            } else {
                                publish(Label.ShowOfflineNoDataNotice)
                            }
                        }
                        dispatch(
                            Msg.BasketResult(
                                date = date,
                                basket = currentBasket,
                                error = e.message
                            )
                        )
                    }
                    .collect { basketObj ->
                        val isConnected = networkManager.isNetworkConnected
                        if (!isConnected) {
                            if (basketObj.basketValue > 0) {
                                publish(Label.ShowOfflineCachedNotice(date))
                            } else {
                                publish(Label.ShowOfflineNoDataNotice)
                            }
                        }
                        dispatch(
                            Msg.BasketResult(
                                date = date,
                                basket = basketObj,
                                error = null
                            )
                        )
                    }
            }
        }

        private fun refreshAndLoad(date: String, getState: () -> State) {
            scope.launch {
                dispatch(Msg.StartLoading(date))
                refreshRatesUseCase(date)
                    .onSuccess {
                        loadBasket(date, getState)
                    }
                    .onFailure { e ->
                        val isConnected = networkManager.isNetworkConnected
                        val currentBasket = getState().basket
                        if (!isConnected) {
                            if (currentBasket != null && currentBasket.basketValue > 0) {
                                publish(Label.ShowOfflineCachedNotice(date))
                            } else {
                                publish(Label.ShowOfflineNoDataNotice)
                            }
                        }
                        dispatch(
                            Msg.BasketResult(
                                date = date,
                                basket = currentBasket,
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
                is Msg.BasketResult -> copy(
                    date = msg.date,
                    basket = msg.basket,
                    isLoading = false,
                    error = if (msg.basket != null && msg.basket.basketValue > 0) null else msg.error
                )
            }
    }
}
