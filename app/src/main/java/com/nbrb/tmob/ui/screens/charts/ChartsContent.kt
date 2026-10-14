package com.nbrb.tmob.ui.screens.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nbrb.tmob.R
import com.nbrb.tmob.domain.entity.ChartPeriod
import com.nbrb.tmob.domain.entity.ChartPoint
import com.nbrb.tmob.domain.entity.Currency
import com.nbrb.tmob.ui.theme.TMobTheme
import com.nbrb.tmob.ui.widgets.ErrorStateWidget
import com.nbrb.tmob.ui.widgets.LoadingStateWidget
import com.nbrb.tmob.ui.widgets.PeriodSelectorWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

@Composable
fun ChartsContent(
    component: ChartsComponent,
    modifier: Modifier = Modifier
) {
    val state by component.model.collectAsState()
    var isCurrencyMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = component::onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                val currAbbr = state.selectedCurrency?.abbreviation ?: "USD"
                Text(
                    text = stringResource(R.string.rate_nbrb_title, currAbbr),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box {
                    OutlinedCard(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { isCurrencyMenuExpanded = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currAbbr,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    val displayCurrencies = remember(state.currencies) {
                        state.currencies
                            .filter { it.periodicity == 0 }
                            .distinctBy { it.abbreviation }
                            .sortedBy { it.abbreviation }
                    }

                    DropdownMenu(
                        expanded = isCurrencyMenuExpanded,
                        onDismissRequest = { isCurrencyMenuExpanded = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        displayCurrencies.forEach { currency ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${currency.abbreviation} - ${currency.name}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    component.onCurrencySelected(currency)
                                    isCurrencyMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.on_today),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (state.currentRate != null) {
                        Text(
                            text = String.format(Locale.US, "%.4f", state.currentRate),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val selectedName = state.selectedCurrency?.abbreviation ?: "USD"
                Text(
                    text = stringResource(R.string.chart_title, selectedName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                LoadingStateWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else if (state.points.isEmpty()) {
                ErrorStateWidget(
                    message = stringResource(R.string.offline_no_data_notice),
                    onRetryClick = component::onRefreshClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    LineChartCanvas(
                        points = state.points,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            PeriodSelectorWidget(
                selectedPeriod = state.selectedPeriod,
                onPeriodSelected = component::onPeriodSelected,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun LineChartCanvas(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        return
    }

    val chartColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
    val textStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.padding(vertical = 12.dp)) {
        val width = size.width
        val height = size.height

        val leftPadding = 20f
        val chartLeftPadding = 100f
        val bottomPadding = 50f
        val topPadding = 20f
        val rightPadding = 20f

        val rawMin = points.minOf { it.ratePerUnit }
        val rawMax = points.maxOf { it.ratePerUnit }
        val rawRange = rawMax - rawMin

        val paddingAmount = if (rawRange == 0.0) (if (rawMin == 0.0) 1.0 else rawMin * 0.1) else rawRange * 0.15
        val minRate = (rawMin - paddingAmount).coerceAtLeast(0.0)
        val maxRate = rawMax + paddingAmount
        val range = maxRate - minRate

        val gridCount = 5
        for (i in 0..gridCount) {
            val ratio = i.toFloat() / gridCount
            val y = height - bottomPadding - ratio * (height - topPadding - bottomPadding)
            val valAtGrid = minRate + ratio * range

            drawLine(
                color = gridColor,
                start = Offset(leftPadding, y),
                end = Offset(width - rightPadding, y),
                strokeWidth = 1f
            )

            val priceText = String.format(Locale.US, "%.2f", valAtGrid)
            val textLayoutResult = textMeasurer.measure(priceText, textStyle)
            val textY = if (i == 0) y - textLayoutResult.size.height else y - textLayoutResult.size.height / 2f
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(leftPadding, textY)
            )
        }

        val stepX = (width - chartLeftPadding - rightPadding) / (points.size - 1).coerceAtLeast(1)
        val path = Path()

        val maxVisibleDateLabels = 6
        val dateStep = (points.size / maxVisibleDateLabels).coerceAtLeast(1)

        points.forEachIndexed { index, point ->
            val x = chartLeftPadding + index * stepX
            val ratio = ((point.ratePerUnit - minRate) / range).toFloat()
            val y = height - bottomPadding - ratio * (height - topPadding - bottomPadding)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            drawCircle(
                color = Color.White,
                radius = 7f,
                center = Offset(x, y)
            )
            drawCircle(
                color = chartColor,
                radius = 7f,
                center = Offset(x, y),
                style = Stroke(width = 2.5f)
            )

            if (index % dateStep == 0 || index == points.lastIndex) {
                val dateText = point.date.takeLast(5)
                val textLayout = textMeasurer.measure(dateText, textStyle)
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(x - textLayout.size.width / 2f, height - bottomPadding + 10f)
                )
            }
        }

        drawPath(
            path = path,
            color = chartColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChartsContentPreview() {
    TMobTheme {
        ChartsContent(
            component = object : ChartsComponent {
                override val model: StateFlow<ChartsStore.State>
                    get() = MutableStateFlow(
                        ChartsStore.State(
                            date = "27.01.2026",
                            selectedCurrency = Currency(431, 190, "840", "USD", "Доллар США", "1 Доллар США", 1, 0, "2021-07-09", "2050-01-01"),
                            currentRate = 2.5782,
                            isLoading = false,
                            points = listOf(
                                ChartPoint("21.01", 2.5390),
                                ChartPoint("22.01", 2.5370),
                                ChartPoint("23.01", 2.5475),
                                ChartPoint("24.01", 2.5475),
                                ChartPoint("25.01", 2.5475),
                                ChartPoint("26.01", 2.5565),
                                ChartPoint("27.01", 2.5782)
                            )
                        )
                    )

                override fun onBackClick() {}
                override fun onCurrencySelected(currency: Currency) {}
                override fun onPeriodSelected(period: ChartPeriod) {}
                override fun onRefreshClick() {}
            }
        )
    }
}
