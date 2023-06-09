package dialogcreate.steptwo.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import common.ui.theme.Purple800
import common.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PicklistValueCell(
    value: String,
    onRemove: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(
            color = Purple800.copy(alpha = 0.5f),
            shape = RoundedCornerShape(4.dp),
        ).padding(vertical = Spacing.xxs, horizontal = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.caption,
            color = Purple800,
        )
        Icon(
            modifier = Modifier
                .size(18.dp)
                .onClick {
                    onRemove?.invoke()
                },
            imageVector = Icons.Filled.RemoveCircle,
            tint = Purple800,
            contentDescription = null,
        )
    }
}
