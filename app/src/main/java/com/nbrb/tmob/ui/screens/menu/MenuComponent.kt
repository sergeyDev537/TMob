package com.nbrb.tmob.ui.screens.menu

import kotlinx.coroutines.flow.StateFlow

interface MenuComponent {

    val model: StateFlow<MenuStore.State>

    fun onRatesClick()

    fun onBasketClick()

    fun onChartsClick()
}
