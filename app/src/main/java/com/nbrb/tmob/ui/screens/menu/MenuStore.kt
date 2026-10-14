package com.nbrb.tmob.ui.screens.menu

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nbrb.tmob.ui.screens.menu.MenuStore.Intent
import com.nbrb.tmob.ui.screens.menu.MenuStore.Label
import com.nbrb.tmob.ui.screens.menu.MenuStore.State
import javax.inject.Inject

interface MenuStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object ClickRates : Intent
        data object ClickBasket : Intent
        data object ClickCharts : Intent
    }

    data class State(val todo: Unit)

    sealed interface Label {
        data object OpenRates : Label
        data object OpenBasket : Label
        data object OpenCharts : Label
    }
}

class MenuStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory
) {

    fun create(): MenuStore =
        object : MenuStore, Store<Intent, State, Label> by storeFactory.create(
            name = "MenuStore",
            initialState = State(Unit),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action

    private sealed interface Msg

    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {}
    }

    private class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.ClickRates -> publish(Label.OpenRates)
                Intent.ClickBasket -> publish(Label.OpenBasket)
                Intent.ClickCharts -> publish(Label.OpenCharts)
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = this
    }
}
