package common.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import common.ui.theme.SelectedBackground
import common.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomTabBar(
    modifier: Modifier = Modifier,
    tabs: List<String> = emptyList(),
    current: Int? = null,
    onTabSelected: ((Int) -> Unit)? = null,
    rightIcon: ImageVector? = null,
    onRightIconClicked: ((Int) -> Unit)? = null,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dp.Hairline),
    ) {
        itemsIndexed(tabs) { idx, tab ->
            Tab(
                label = tab,
                selected = idx == current,
                modifier = Modifier.onClick {
                    onTabSelected?.invoke(idx)
                },
                rightIcon = rightIcon,
                onRightIconClicked = {
                    onRightIconClicked?.invoke(idx)
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Tab(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    rightIcon: ImageVector?,
    onRightIconClicked: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.background(
            color = if (selected) SelectedBackground else Color.Transparent,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        ).padding(horizontal = Spacing.m, vertical = Spacing.s),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2.copy(color = Color.White),
        )
        if (rightIcon != null) {
            Spacer(Modifier.width(Spacing.m))
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(14.dp)
                    .onClick {
                        onRightIconClicked?.invoke()
                    },
                imageVector = rightIcon,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}
