package com.nbrb.tmob.ui.screens.basket

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nbrb.tmob.R
import com.nbrb.tmob.domain.entity.CurrencyBasket
import com.nbrb.tmob.ui.theme.TMobTheme
import com.nbrb.tmob.ui.widgets.DatePickerBlock
import com.nbrb.tmob.ui.widgets.ErrorStateWidget
import com.nbrb.tmob.ui.widgets.LoadingStateWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

@Composable
fun BasketContent(
    component: BasketComponent,
    modifier: Modifier = Modifier
) {
    val state by component.model.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    text = stringResource(R.string.basket_title, state.date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                DatePickerBlock(
                    selectedDate = state.date,
                    onDateSelected = component::onDateSelected,
                    onSearchClick = component::onFetchClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.basket_formula_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    lineHeight = 16.sp,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    LoadingStateWidget(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else if (state.basket == null || state.basket?.basketValue == 0.0) {
                    ErrorStateWidget(
                        message = stringResource(R.string.offline_no_data_notice),
                        onRetryClick = component::onFetchClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else {
                    val basket = state.basket
                    if (basket != null) {
                        BasketTableWidget(basket = basket)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.basket_table_description),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class BasketRowItem(
    val label: String,
    val value: Double,
    val yearChangePercent: Double?,
    val prevDayChangePercent: Double?
)

@Composable
private fun BasketTableWidget(
    basket: CurrencyBasket,
    modifier: Modifier = Modifier
) {
    val year = if (basket.date.length >= 4) basket.date.substring(0, 4).toIntOrNull() ?: 2023 else 2023
    val prevYear = year - 1

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(MaterialTheme.colorScheme.primary),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.basket_currency_column),
                    modifier = Modifier
                        .weight(1.1f)
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 10.sp
                )
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                Text(
                    text = stringResource(R.string.official_rate_column),
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 10.sp
                )
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                Text(
                    text = stringResource(R.string.year_change_column, prevYear),
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                Text(
                    text = stringResource(R.string.prev_day_change_column),
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.primary)

            val rows = listOf(
                BasketRowItem(
                    stringResource(R.string.basket_row_label),
                    basket.basketValue,
                    basket.basketYearChangePercent,
                    basket.basketPrevDayChangePercent
                ),
                BasketRowItem(
                    stringResource(R.string.rub_100_row_label),
                    basket.rubRate * 100,
                    basket.rubYearChangePercent,
                    basket.rubPrevDayChangePercent
                ),
                BasketRowItem(
                    stringResource(R.string.usd_1_row_label),
                    basket.usdRate,
                    basket.usdYearChangePercent,
                    basket.usdPrevDayChangePercent
                ),
                BasketRowItem(
                    stringResource(R.string.cny_10_row_label),
                    basket.cnyRate * 10,
                    basket.cnyYearChangePercent,
                    basket.cnyPrevDayChangePercent
                )
            )

            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.label,
                        modifier = Modifier
                            .weight(1.1f)
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    )
                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        thickness = 1.dp
                    )
                    Text(
                        text = String.format(Locale.US, "%.4f", row.value),
                        modifier = Modifier
                            .weight(0.8f)
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    )
                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        thickness = 1.dp
                    )

                    ChangeCell(
                        changeVal = row.yearChangePercent,
                        modifier = Modifier.weight(1.0f)
                    )

                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        thickness = 1.dp
                    )

                    ChangeCell(
                        changeVal = row.prevDayChangePercent,
                        modifier = Modifier.weight(1.0f)
                    )
                }

                if (index < rows.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                }
            }
        }
    }
}

@Composable
private fun ChangeCell(
    changeVal: Double?,
    modifier: Modifier = Modifier
) {
    if (changeVal == null) {
        Text(
            text = "-",
            modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp
        )
    } else {
        val isPositive = changeVal > 0
        val isNegative = changeVal < 0
        val color = when {
            isPositive -> Color(0xFF2E7D32)
            isNegative -> Color(0xFFC62828)
            else -> MaterialTheme.colorScheme.onSurface
        }
        val arrow = when {
            isPositive -> " ⬆"
            isNegative -> " ⬇"
            else -> ""
        }
        val text = String.format(Locale.US, "%.2f%s", changeVal, arrow)

        Text(
            text = text,
            modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = color,
            fontSize = 11.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BasketContentPreview() {
    TMobTheme {
        BasketContent(
            component = object : BasketComponent {
                override val model: StateFlow<BasketStore.State>
                    get() = MutableStateFlow(
                        BasketStore.State(
                            date = "21.12.2023",
                            isLoading = false,
                            basket = CurrencyBasket(
                                date = "21.12.2023",
                                basketValue = 0.1749,
                                rubRate = 0.036006,
                                usdRate = 3.0098,
                                cnyRate = 0.45057,
                                basketPrevDayChangePercent = -0.06,
                                rubPrevDayChangePercent = -0.12,
                                usdPrevDayChangePercent = 0.03,
                                cnyPrevDayChangePercent = 0.13,
                                basketYearChangePercent = -0.23,
                                rubYearChangePercent = 2.84,
                                usdYearChangePercent = -3.69,
                                cnyYearChangePercent = -8.79
                            )
                        )
                    )

                override fun onBackClick() {}
                override fun onDateSelected(date: String) {}
                override fun onFetchClick() {}
            }
        )
    }
}
