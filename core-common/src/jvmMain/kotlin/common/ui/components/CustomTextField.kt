package common.ui.components

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import common.ui.theme.Spacing

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    label: String = "",
    hint: String = "",
    labelColor: Color = Color.White,
    backgroundColor: Color = Color.White,
    textColor: Color = Color.Black,
    labelExtraSpacing: Dp = 0.dp,
    labelStyle: TextStyle = MaterialTheme.typography.caption,
    value: String,
    singleLine: Boolean = false,
    onValueChange: (String) -> Unit,
    endButton: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
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
        var initial by remember {
            mutableStateOf(true)
        }
        var textFieldValue by remember {
            mutableStateOf(TextFieldValue(value))
        }
        LaunchedEffect(value) {
            if (textFieldValue.text.isEmpty() && value.isNotEmpty() && initial) {
                initial = false
                textFieldValue = TextFieldValue(value)
            } else if (value.isEmpty()) {
                textFieldValue = TextFieldValue(value)
            }
        }
        BasicTextField(
            modifier = Modifier.fillMaxWidth()
                .weight(1f)
                .background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onValueChange(it.text)
            },
            cursorBrush = SolidColor(textColor),
            textStyle = MaterialTheme.typography.caption.copy(color = textColor),
            singleLine = singleLine,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Dp.Hairline),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (hint.isNotEmpty() && textFieldValue.text.isEmpty()) {
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
