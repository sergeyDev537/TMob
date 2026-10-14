package com.nbrb.tmob.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nbrb.tmob.R
import com.nbrb.tmob.domain.entity.ChartPeriod
import com.nbrb.tmob.ui.theme.TMobTheme

@Composable
fun PeriodSelectorWidget(
    selectedPeriod: ChartPeriod,
    modifier: Modifier = Modifier,
    onPeriodSelected: (ChartPeriod) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ChartPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            val labelRes = when (period) {
                ChartPeriod.WEEK -> R.string.period_week
                ChartPeriod.MONTH -> R.string.period_month
                ChartPeriod.QUARTER -> R.string.period_quarter
            }

            FilterChip(
                selected = isSelected,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        text = stringResource(labelRes),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PeriodSelectorWidgetPreview() {
    TMobTheme {
        PeriodSelectorWidget(
            selectedPeriod = ChartPeriod.WEEK
        )
    }
}
