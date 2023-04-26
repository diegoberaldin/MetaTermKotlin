package ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import localized
import ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TermCell(
    lemma: String? = null,
    modifier: Modifier = Modifier,
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
        Box(modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xs).fillMaxSize()) {
            Text(
                modifier = Modifier.align(Alignment.CenterStart),
                text = lemma ?: "empty_term_placeholder".localized(),
                style = MaterialTheme.typography.body2,
                color = when {
                    lemma == null -> Color.Gray
                    selected -> Color.White
                    else -> Color.Black
                },
            )
        }
    }
}
