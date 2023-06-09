package main.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import localized
import common.ui.theme.Spacing

@Composable
internal fun StatusBar(
    termbaseName: String,
    entryCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(bottom = Spacing.xs),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${"status_bar_termbase".localized()}: ${termbaseName.takeIf { it.isNotEmpty() } ?: "N/A"}",
            style = MaterialTheme.typography.caption,
            color = Color.White,
        )
        Text(
            text = ", ",
            style = MaterialTheme.typography.caption,
            color = Color.White,
        )
        Text(
            text = if (entryCount == 1) "$entryCount ${"status_bar_entry".localized()}" else "$entryCount ${"status_bar_entries".localized()}",
            style = MaterialTheme.typography.caption,
            color = Color.White,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}