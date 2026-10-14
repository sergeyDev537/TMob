package com.nbrb.tmob.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nbrb.tmob.domain.entity.Rate
import com.nbrb.tmob.ui.theme.TMobTheme
import java.util.Locale

@Composable
fun CurrencyTableItem(
    rate: Rate,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rate.name,
                modifier = Modifier
                    .weight(1.5f)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            Text(
                text = String.format(Locale.US, "%.4f", rate.officialRate),
                modifier = Modifier
                    .weight(0.7f)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onBackground
            )

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            val dynamics = rate.dynamicsVsPreviousDay
            val textColor = when {
                dynamics != null && dynamics > 0 -> Color(0xFF2E7D32)
                dynamics != null && dynamics < 0 -> Color(0xFFC62828)
                else -> MaterialTheme.colorScheme.onBackground
            }
            val sign = if (dynamics != null && dynamics > 0) "+" else ""
            val dynamicsText = if (dynamics != null) {
                String.format(Locale.US, "%s%.4f", sign, dynamics)
            } else "-"

            Text(
                text = dynamicsText,
                modifier = Modifier
                    .weight(0.7f)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                color = textColor,
                fontSize = 13.sp
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
            thickness = 1.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrencyTableItemPreview() {
    TMobTheme {
        CurrencyTableItem(
            rate = Rate(
                id = 431,
                code = "840",
                abbreviation = "USD",
                name = "Доллар США",
                scale = 1,
                officialRate = 3.1683,
                date = "2023-12-19",
                dynamicsVsPreviousDay = 0.0045
            )
        )
    }
}
