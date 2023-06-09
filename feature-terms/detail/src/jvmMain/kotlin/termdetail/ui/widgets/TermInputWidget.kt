package termdetail.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import localized
import common.ui.components.CustomTextField
import common.ui.theme.DeepPurple300
import common.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TermInputWidget(
    modifier: Modifier = Modifier,
    color: Color = DeepPurple300,
    value: String = "",
    onValueChange: ((String) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = color, shape = RoundedCornerShape(4.dp))
            .background(color = Color.White.copy(alpha = 0.75f), shape = RoundedCornerShape(4.dp))
            .border(color = color, width = Dp.Hairline, shape = RoundedCornerShape(4.dp))
            .padding(all = Spacing.s),
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = Spacing.xxs),
        ) {
            Icon(
                modifier = Modifier.size(16.dp).onClick {
                    onDelete?.invoke()
                },
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = color,
            )
        }

        CustomTextField(
            label = "term_detail_title_lemma".localized(),
            labelColor = color,
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.xxs).height(44.dp),
            value = value,
            singleLine = true,
            onValueChange = {
                onValueChange?.invoke(it)
            },
        )
    }
}
