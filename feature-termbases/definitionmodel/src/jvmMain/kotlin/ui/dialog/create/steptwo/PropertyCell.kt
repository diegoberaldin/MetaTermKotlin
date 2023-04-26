package ui.dialog.create.steptwo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ui.components.TreeItem
import ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PropertyCell(
    name: String,
    modifier: Modifier = Modifier,
    hasNext: Boolean = false,
    selected: Boolean = false,
    onSelected: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .background(
                color = when {
                    selected -> Color.Blue
                    else -> Color.Transparent
                },
            ).onClick {
                onSelected?.invoke()
            },
    ) {
        TreeItem(
            modifier = Modifier.height(20.dp).fillMaxSize(),
            indentLevel = 1,
            hasNext = hasNext,
        ) {
            val isEmpty = name.isEmpty()
            Text(
                modifier = Modifier.align(Alignment.CenterStart).padding(vertical = Spacing.xxs),
                text = if (isEmpty) "—" else name,
                style = MaterialTheme.typography.caption,
                color = when {
                    selected -> Color.White
                    isEmpty -> Color.Gray
                    else -> Color.Black
                },
            )
        }
    }
}
