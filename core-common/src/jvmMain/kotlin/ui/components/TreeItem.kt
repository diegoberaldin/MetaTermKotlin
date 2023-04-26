package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import ui.theme.Spacing

@Composable
fun TreeItem(
    indentLevel: Int = 0,
    hasNext: Boolean = false,
    previousLevels: List<Int> = emptyList(),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        val dottedPathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(1.5f, 0.5f), phase = 0f)

        for (i in 0..indentLevel) {
            Spacer(Modifier.width(Spacing.xxs))
            if (i in previousLevels) {
                Spacer(Modifier.width(Spacing.xxs))
                Canvas(
                    modifier = Modifier.width(Spacing.xxs).fillMaxHeight(),
                ) {
                    drawLine(
                        color = Color.Gray,
                        start = Offset(0f, y = 0f),
                        end = Offset(x = 0f, y = size.height),
                        pathEffect = dottedPathEffect,
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(Spacing.xxs))
            }
        }

        if (indentLevel > 0) {
            Canvas(
                modifier = Modifier.width(14.dp).fillMaxHeight(),
            ) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, y = 0f),
                    end = if (hasNext) {
                        Offset(x = 0f, y = size.height)
                    } else {
                        Offset(x = 0f, y = size.height / 2)
                    },
                    pathEffect = dottedPathEffect,
                )
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, y = size.height / 2),
                    end = Offset(size.width * 0.8f, y = size.height / 2),
                    pathEffect = dottedPathEffect,
                )
            }
        }
        content()
    }
}
