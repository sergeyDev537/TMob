package com.nbrb.tmob.ui.screens.courses

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.nbrb.tmob.R
import com.nbrb.tmob.ui.componentScope
import com.nbrb.tmob.ui.screens.courses.CoursesStore.Intent
import com.nbrb.tmob.ui.screens.courses.CoursesStore.Label
import com.nbrb.tmob.ui.screens.courses.CoursesStore.State
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultCoursesComponent @AssistedInject constructor(
    @Assisted("componentContext") private val componentContext: ComponentContext,
    @Assisted("appContext") private val appContext: Context,
    @Assisted("onOpenBack") private val onOpenBack: () -> Unit,
    @Assisted("onShowMessage") private val onShowMessage: (String) -> Unit,
    private val coursesStoreFactory: CoursesStoreFactory
) : CoursesComponent, ComponentContext by componentContext {

    private val store: CoursesStore = instanceKeeper.getStore { coursesStoreFactory.create() }
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
        ): DefaultCoursesComponent
    }
}
