package com.nbrb.tmob.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.nbrb.tmob.ui.screens.root.DefaultRootComponent
import com.nbrb.tmob.ui.screens.root.RootContent
import com.nbrb.tmob.ui.theme.TMobTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootComponentFactory: DefaultRootComponent.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val root = rootComponentFactory.create(componentContext = defaultComponentContext(), this)
        setContent {
            TMobTheme {
                RootContent(component = root)
            }
        }
    }
}