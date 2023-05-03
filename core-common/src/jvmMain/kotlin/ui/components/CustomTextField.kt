package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ui.theme.Spacing

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    label: String = "",
    hint: String = "",
    labelColor: Color = Color.White,
    labelExtraSpacing: Dp = 0.dp,
    labelStyle: TextStyle = MaterialTheme.typography.caption,
    value: String,
    singleLine: Boolean = false,
    onValueChange: (String) -> Unit,
    endButton: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (label.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = Spacing.xs + labelExtraSpacing),
                text = label,
                style = labelStyle,
                color = labelColor,
            )
            Spacer(modifier = Modifier.height(Spacing.s))
        }
        var value by remember {
            mutableStateOf(TextFieldValue(value))
        }
        BasicTextField(
            modifier = Modifier.fillMaxWidth()
                .weight(1f)
                .background(color = Color.White, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
            value = value,
            onValueChange = {
                value = it.copy(selection = TextRange(it.text.length))
                onValueChange(it.text)
            },
            textStyle = MaterialTheme.typography.caption,
            singleLine = singleLine,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Dp.Hairline),
                    contentAlignment = Alignment.TopStart,
                ) {
                    if (hint.isNotEmpty() && value.text.isEmpty()) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray,
                        )
                    }
                    if (endButton != null) {
                        Box(
                            modifier = Modifier.align(Alignment.CenterEnd),
                        ) {
                            endButton()
                        }
                    }
                    innerTextField()
                }
            },
        )
    }
}
