package com.nbrb.tmob.ui.screens.basket

import kotlinx.coroutines.flow.StateFlow

interface BasketComponent {

    val model: StateFlow<BasketStore.State>

    fun onBackClick()

    fun onDateSelected(date: String)

    fun onFetchClick()
}
