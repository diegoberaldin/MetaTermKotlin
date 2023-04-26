package ui.dialog.create.stepthree

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ui.components.TreeItem
import ui.theme.Spacing

@Composable
fun CheckableItem(
    title: String,
    checked: Boolean = false,
    indentLevel: Int = 0,
    hasNext: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    previousLevels: List<Int> = emptyList(),
) {
    TreeItem(
        modifier = modifier.padding(
            end = Spacing.xs,
            top = Spacing.xxs,
        ).height(20.dp),
        indentLevel = indentLevel,
        hasNext = hasNext,
        previousLevels = previousLevels,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.caption,
            )
            Spacer(modifier = Modifier.weight(1f))
            Checkbox(
                modifier = Modifier.padding(0.dp).size(20.dp),
                colors = CheckboxDefaults.colors(
                    uncheckedColor = Color.Gray.copy(alpha = 0.25f),
                    checkedColor = MaterialTheme.colors.primary,
                    checkmarkColor = Color.White,
                ),
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
        }
    }
}
