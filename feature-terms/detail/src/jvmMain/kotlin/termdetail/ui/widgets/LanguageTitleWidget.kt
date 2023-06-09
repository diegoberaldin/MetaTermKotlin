package termdetail.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import common.ui.theme.Spacing

@Composable
fun LanguageTitleWidget(
    modifier: Modifier = Modifier,
    flag: String,
    name: String,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = flag,
            style = MaterialTheme.typography.body2,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.body2.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = Color.Blue,
        )
    }
}
