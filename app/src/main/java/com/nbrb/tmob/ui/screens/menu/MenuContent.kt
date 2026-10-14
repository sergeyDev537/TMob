package com.nbrb.tmob.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nbrb.tmob.R
import com.nbrb.tmob.ui.theme.TMobTheme
import com.nbrb.tmob.ui.widgets.AppButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MenuContent(
    component: MenuComponent,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.tab_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            AppButton(
                text = stringResource(R.string.tab_courses),
                onClick = component::onRatesClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppButton(
                text = stringResource(R.string.tab_basket),
                onClick = component::onBasketClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppButton(
                text = stringResource(R.string.tab_chart),
                onClick = component::onChartsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                fontSize = 24.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuContentPreview() {
    TMobTheme {
        MenuContent(
            component = object : MenuComponent {
                override val model: StateFlow<MenuStore.State>
                    get() = MutableStateFlow(MenuStore.State(Unit))

                override fun onRatesClick() {}
                override fun onBasketClick() {}
                override fun onChartsClick() {}
            }
        )
    }
}
