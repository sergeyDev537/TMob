package com.nbrb.tmob.ui.screens.menu

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.nbrb.tmob.ui.componentScope
import com.nbrb.tmob.ui.screens.menu.MenuStore.Intent
import com.nbrb.tmob.ui.screens.menu.MenuStore.Label
import com.nbrb.tmob.ui.screens.menu.MenuStore.State
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultMenuComponent @AssistedInject constructor(
    @Assisted("componentContext") private val componentContext: ComponentContext,
    @Assisted("onOpenRates") private val onOpenRates: () -> Unit,
    @Assisted("onOpenBasket") private val onOpenBasket: () -> Unit,
    @Assisted("onOpenCharts") private val onOpenCharts: () -> Unit,
    private val menuStoreFactory: MenuStoreFactory,
) : MenuComponent, ComponentContext by componentContext {

    private val store: MenuStore = instanceKeeper.getStore { menuStoreFactory.create() }

    private val scope = componentScope()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<State> = store.stateFlow

    init {
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    Label.OpenRates -> onOpenRates()
                    Label.OpenBasket -> onOpenBasket()
                    Label.OpenCharts -> onOpenCharts()
                }
            }
        }
    }

    override fun onRatesClick() {
        store.accept(Intent.ClickRates)
    }

    override fun onBasketClick() {
        store.accept(Intent.ClickBasket)
    }

    override fun onChartsClick() {
        store.accept(Intent.ClickCharts)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("onOpenRates") onOpenRates: () -> Unit,
            @Assisted("onOpenBasket") onOpenBasket: () -> Unit,
            @Assisted("onOpenCharts") onOpenCharts: () -> Unit,
        ): DefaultMenuComponent
    }
}
