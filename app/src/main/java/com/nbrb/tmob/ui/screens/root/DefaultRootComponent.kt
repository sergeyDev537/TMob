package com.nbrb.tmob.ui.screens.root

import android.content.Context
import android.os.Parcelable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.nbrb.tmob.data.managers.NetworkManager
import com.nbrb.tmob.ui.componentScope
import com.nbrb.tmob.ui.screens.basket.DefaultBasketComponent
import com.nbrb.tmob.ui.screens.charts.DefaultChartsComponent
import com.nbrb.tmob.ui.screens.courses.DefaultCoursesComponent
import com.nbrb.tmob.ui.screens.menu.DefaultMenuComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class DefaultRootComponent @AssistedInject constructor(
    @Assisted("componentContext") private val componentContext: ComponentContext,
    @Assisted("appContext") private val appContext: Context,
    private val menuComponentFactory: DefaultMenuComponent.Factory,
    private val coursesComponentFactory: DefaultCoursesComponent.Factory,
    private val basketComponentFactory: DefaultBasketComponent.Factory,
    private val chartsComponentFactory: DefaultChartsComponent.Factory,
    private val networkManager: NetworkManager,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()
    private val scope = componentScope()

    private val _messageFlow = MutableSharedFlow<String>()
    override val messageFlow: SharedFlow<String> = _messageFlow.asSharedFlow()

    override val isNetworkConnected: StateFlow<Boolean> = networkManager.isNetworkConnectedFlow

    init {
        networkManager.startListenNetworkState()
    }

    override fun onShowMessage(message: String) {
        scope.launch {
            _messageFlow.emit(message)
        }
    }

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        initialConfiguration = Config.Menu,
        handleBackButton = true,
        childFactory = ::child
    )

    fun child(
        config: Config,
        componentContext: ComponentContext,
    ): RootComponent.Child {
        return when (config) {
            is Config.Menu -> {
                val component = menuComponentFactory.create(
                    componentContext = componentContext,
                    onOpenRates = {
                        navigation.bringToFront(Config.Courses)
                    },
                    onOpenBasket = {
                        navigation.bringToFront(Config.Basket)
                    },
                    onOpenCharts = {
                        navigation.bringToFront(Config.Charts)
                    },
                )
                RootComponent.Child.Menu(component)
            }

            is Config.Courses -> {
                val component = coursesComponentFactory.create(
                    componentContext = componentContext,
                    appContext = appContext,
                    onOpenBack = {
                        navigation.pop()
                    },
                    onShowMessage = ::onShowMessage
                )
                RootComponent.Child.Courses(component)
            }

            is Config.Basket -> {
                val component = basketComponentFactory.create(
                    componentContext = componentContext,
                    appContext = appContext,
                    onOpenBack = {
                        navigation.pop()
                    },
                    onShowMessage = ::onShowMessage
                )
                RootComponent.Child.Basket(component)
            }

            is Config.Charts -> {
                val component = chartsComponentFactory.create(
                    componentContext = componentContext,
                    appContext = appContext,
                    onOpenBack = {
                        navigation.pop()
                    },
                    onShowMessage = ::onShowMessage
                )
                RootComponent.Child.Charts(component)
            }
        }
    }

    sealed interface Config : Parcelable {

        @Parcelize
        data object Menu : Config

        @Parcelize
        data object Courses : Config

        @Parcelize
        data object Basket : Config

        @Parcelize
        data object Charts : Config
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("appContext") appContext: Context,
        ): DefaultRootComponent
    }
}
