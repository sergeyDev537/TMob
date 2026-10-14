package com.nbrb.tmob.ui.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OfflineSnackbarWidget(
    message: String,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier.padding(12.dp),
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
