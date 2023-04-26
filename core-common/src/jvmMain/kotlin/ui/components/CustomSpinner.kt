package ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ui.theme.Spacing

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun CustomSpinner(
    values: List<String>,
    current: String?,
    modifier: Modifier = Modifier.width(150.dp).height(20.dp),
    onValueChanged: ((Int) -> Unit)? = null,
    valueColor: Color = Color.Black,
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier.background(Color.White, shape = RoundedCornerShape(4.dp)),
    ) {
        BasicTextField(
            modifier = Modifier.matchParentSize(),
            value = current ?: "",
            textStyle = MaterialTheme.typography.caption.copy(color = valueColor),
            readOnly = true,
            onValueChange = {},
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(start = Spacing.s),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Icon(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = null,
                    )
                    innerTextField()
                }
            },
        )
        DropdownMenu(
            modifier = Modifier.width(150.dp)
                .background(Color.White)
                .border(
                    width = Dp.Hairline,
                    color = MaterialTheme.colors.background,
                    shape = RoundedCornerShape(4.dp),
                ),
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            var hoveredValue by remember {
                mutableStateOf<String?>(null)
            }
            values.forEachIndexed { idx, value ->
                DropdownMenuItem(
                    modifier = Modifier
                        .background(color = if (value == hoveredValue) Color.Blue else Color.Transparent)
                        .fillMaxWidth().height(20.dp)
                        .onPointerEvent(PointerEventType.Enter) { hoveredValue = value }
                        .onPointerEvent(PointerEventType.Exit) { hoveredValue = null }
                        .padding(horizontal = Spacing.s, vertical = Spacing.xxs),
                    onClick = {
                        onValueChanged?.invoke(idx)
                        expanded = false
                    },
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.caption,
                        color = if (value == hoveredValue) Color.White else Color.Black,
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier.matchParentSize().onClick {
                if (values.isEmpty()) {
                    return@onClick
                }
                expanded = !expanded
            },
        )
    }
}
