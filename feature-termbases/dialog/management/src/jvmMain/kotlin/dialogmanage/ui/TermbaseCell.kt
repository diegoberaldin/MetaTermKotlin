package dialogmanage.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import common.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TermbaseCell(
    name: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onSelected: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .background(
                color = if (selected) {
                    Color.Blue
                } else {
                    Color.Transparent
                },
            ).onClick {
                onSelected?.invoke()
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xxs).fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = name,
                style = MaterialTheme.typography.body2,
                color = if (selected) Color.White else Color.Black,
            )
        }
    }
}
