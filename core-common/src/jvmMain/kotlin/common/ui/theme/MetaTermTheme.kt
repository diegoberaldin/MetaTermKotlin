package common.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MetaTermTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = DarkColorPalette,
        typography = Typography,
        content = content,
    )
}
