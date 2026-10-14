package com.nbrb.tmob.ui.screens.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.nbrb.tmob.ui.screens.basket.BasketContent
import com.nbrb.tmob.ui.screens.charts.ChartsContent
import com.nbrb.tmob.ui.screens.courses.CoursesContent
import com.nbrb.tmob.ui.screens.menu.MenuContent

@Composable
fun RootContent(component: RootComponent) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        component.messageFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                Children(stack = component.stack) {
                    when (val instance = it.instance) {
                        is RootComponent.Child.Menu -> {
                            MenuContent(component = instance.component)
                        }

                        is RootComponent.Child.Courses -> {
                            CoursesContent(component = instance.component)
                        }

                        is RootComponent.Child.Basket -> {
                            BasketContent(component = instance.component)
                        }

                        is RootComponent.Child.Charts -> {
                            ChartsContent(component = instance.component)
                        }
                    }
                }
            }
        }
    )
}
