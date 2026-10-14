package com.nbrb.tmob.ui.screens.courses

import kotlinx.coroutines.flow.StateFlow

interface CoursesComponent {

    val model: StateFlow<CoursesStore.State>

    fun onBackClick()

    fun onRefreshClick()
}
