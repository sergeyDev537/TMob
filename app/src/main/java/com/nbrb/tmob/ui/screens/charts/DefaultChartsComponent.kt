package com.nbrb.tmob.ui.screens.charts

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.nbrb.tmob.R
import com.nbrb.tmob.domain.entity.ChartPeriod
import com.nbrb.tmob.domain.entity.Currency
import com.nbrb.tmob.ui.componentScope
import com.nbrb.tmob.ui.screens.charts.ChartsStore.Intent
import com.nbrb.tmob.ui.screens.charts.ChartsStore.Label
import com.nbrb.tmob.ui.screens.charts.ChartsStore.State
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultChartsComponent @AssistedInject constructor(
    @Assisted("componentContext") private val componentContext: ComponentContext,
    @Assisted("appContext") private val appContext: Context,
    @Assisted("onOpenBack") private val onOpenBack: () -> Unit,
    @Assisted("onShowMessage") private val onShowMessage: (String) -> Unit,
    private val chartsStoreFactory: ChartsStoreFactory
) : ChartsComponent, ComponentContext by componentContext {

    private val store: ChartsStore = instanceKeeper.getStore { chartsStoreFactory.create() }
    private val scope = componentScope()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<State> = store.stateFlow

    init {
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    Label.OpenBack -> onOpenBack()
                    is Label.ShowOfflineCachedNotice -> onShowMessage(appContext.getString(R.string.offline_cached_notice, label.date))
                    Label.ShowOfflineNoDataNotice -> onShowMessage(appContext.getString(R.string.offline_no_data_notice))
                }
            }
        }
    }

    override fun onBackClick() {
        store.accept(Intent.ClickBack)
    }

    override fun onCurrencySelected(currency: Currency) {
        store.accept(Intent.SelectCurrency(currency))
    }

    override fun onPeriodSelected(period: ChartPeriod) {
        store.accept(Intent.SelectPeriod(period))
    }

    override fun onRefreshClick() {
        store.accept(Intent.ClickRefresh)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("appContext") appContext: Context,
            @Assisted("onOpenBack") onOpenBack: () -> Unit,
            @Assisted("onShowMessage") onShowMessage: (String) -> Unit
        ): DefaultChartsComponent
    }
}
