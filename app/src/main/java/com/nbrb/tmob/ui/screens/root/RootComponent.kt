package com.nbrb.tmob.ui.screens.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.nbrb.tmob.ui.screens.basket.BasketComponent
import com.nbrb.tmob.ui.screens.charts.ChartsComponent
import com.nbrb.tmob.ui.screens.courses.CoursesComponent
import com.nbrb.tmob.ui.screens.menu.MenuComponent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>
    val messageFlow: SharedFlow<String>
    val isNetworkConnected: StateFlow<Boolean>

    fun onShowMessage(message: String)

    sealed interface Child {
        data class Menu(val component: MenuComponent) : Child
        data class Courses(val component: CoursesComponent) : Child
        data class Basket(val component: BasketComponent) : Child
        data class Charts(val component: ChartsComponent) : Child
    }
}
