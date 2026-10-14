package com.nbrb.tmob.ui.screens.basket

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.nbrb.tmob.R
import com.nbrb.tmob.ui.componentScope
import com.nbrb.tmob.ui.screens.basket.BasketStore.Intent
import com.nbrb.tmob.ui.screens.basket.BasketStore.Label
import com.nbrb.tmob.ui.screens.basket.BasketStore.State
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultBasketComponent @AssistedInject constructor(
    @Assisted("componentContext") private val componentContext: ComponentContext,
    @Assisted("appContext") private val appContext: Context,
    @Assisted("onOpenBack") private val onOpenBack: () -> Unit,
    @Assisted("onShowMessage") private val onShowMessage: (String) -> Unit,
    private val basketStoreFactory: BasketStoreFactory
) : BasketComponent, ComponentContext by componentContext {

    private val store: BasketStore = instanceKeeper.getStore { basketStoreFactory.create() }
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

    override fun onDateSelected(date: String) {
        store.accept(Intent.SelectDate(date))
    }

    override fun onFetchClick() {
        store.accept(Intent.ClickFetch)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("appContext") appContext: Context,
            @Assisted("onOpenBack") onOpenBack: () -> Unit,
            @Assisted("onShowMessage") onShowMessage: (String) -> Unit
        ): DefaultBasketComponent
    }
}
