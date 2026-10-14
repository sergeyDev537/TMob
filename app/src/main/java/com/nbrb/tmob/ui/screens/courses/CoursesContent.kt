package com.nbrb.tmob.ui.screens.courses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nbrb.tmob.R
import com.nbrb.tmob.domain.entity.Rate
import com.nbrb.tmob.ui.theme.TMobTheme
import com.nbrb.tmob.ui.widgets.AppButton
import com.nbrb.tmob.ui.widgets.CurrencyTableItem
import com.nbrb.tmob.ui.widgets.ErrorStateWidget
import com.nbrb.tmob.ui.widgets.LoadingStateWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CoursesContent(
    component: CoursesComponent,
    modifier: Modifier = Modifier
) {
    val state by component.model.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = component::onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.courses_title, state.date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppButton(
                    text = stringResource(R.string.refresh_button),
                    onClick = component::onRefreshClick,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(44.dp),
                    fontSize = 15.sp,
                    enabled = !state.isRefreshing
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (state.isLoading) {
                LoadingStateWidget(
                    modifier = Modifier.weight(1f)
                )
            } else if (state.rates.isEmpty()) {
                ErrorStateWidget(
                    message = stringResource(R.string.offline_no_data_notice),
                    onRetryClick = component::onRefreshClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = state.rates,
                            key = { rate -> rate.id }
                        ) { rate ->
                            CurrencyTableItem(
                                rate = rate
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoursesContentPreview() {
    TMobTheme {
        CoursesContent(
            component = object : CoursesComponent {
                override val model: StateFlow<CoursesStore.State>
                    get() = MutableStateFlow(
                        CoursesStore.State(
                            date = "19.12.2023",
                            isLoading = false,
                            rates = listOf(
                                Rate(1, "036", "AUD", "Австралийский доллар", 1, 2.1327, "2023-12-19", -0.0045),
                                Rate(2, "051", "AMD", "Армянский драм", 1000, 7.8047, "2023-12-19", -0.0327),
                                Rate(3, "975", "BGN", "Болгарский лев", 1, 1.7702, "2023-12-19", null),
                                Rate(4, "986", "BRL", "Бразильский реал", 10, 6.4141, "2023-12-19", null),
                                Rate(5, "840", "USD", "Доллар США", 1, 3.1683, "2023-12-19", 0.0045)
                            )
                        )
                    )

                override fun onBackClick() {}
                override fun onRefreshClick() {}
            }
        )
    }
}
