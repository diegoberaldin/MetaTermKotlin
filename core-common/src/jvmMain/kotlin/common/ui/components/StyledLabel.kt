package common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import common.ui.theme.Spacing

@Composable
fun StyledLabel(
    color: Color,
    title: String,
    filled: Boolean = false,
    internalPadding: PaddingValues = PaddingValues(Spacing.s),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.background(
                color = color,
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
            ).padding(horizontal = Spacing.s, vertical = Spacing.xxs),
            text = title,
            style = MaterialTheme.typography.caption,
            color = Color.White,
        )
        Box(
            modifier = Modifier.border(
                width = Dp.Hairline,
                color = color,
                shape = RoundedCornerShape(topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
            )
                .background(
                    color = if (filled) color else Color.Transparent,
                    shape = RoundedCornerShape(topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                )
                .fillMaxWidth()
                .padding(internalPadding),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
