package common.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import common.ui.theme.Spacing

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun CustomSpinner(
    values: List<String>,
    current: String?,
    modifier: Modifier = Modifier.background(color = Color.White, shape = RoundedCornerShape(4.dp)),
    size: DpSize = DpSize(width = 150.dp, height = 24.dp),
    onValueChanged: ((Int) -> Unit)? = null,
    valueColor: Color = Color.Black,
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier.size(size),
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
            modifier = Modifier.width(size.width)
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
                        .fillMaxWidth().height(24.dp)
                        .onPointerEvent(PointerEventType.Enter) { hoveredValue = value }
                        .onPointerEvent(PointerEventType.Exit) { hoveredValue = null }
                        .padding(horizontal = Spacing.xs, vertical = Spacing.xxs),
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
