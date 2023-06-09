package dialogfilter.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.PropertyLevel
import data.toReadableString
import localized
import common.ui.components.TreeItem
import common.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PropertyTree(
    items: List<FilterableItem>,
    configurations: Map<FilterableItem, FilterConfiguration> = mapOf(),
    modifier: Modifier = Modifier,
    selectedItem: FilterableItem? = null,
    onSelectionChanged: ((FilterableItem) -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(2.dp),
            ).padding(Dp.Hairline),
    ) {
        itemsIndexed(
            items = items,
            key = { _, it ->
                when (it) {
                    is FilterableItem.SectionHeader -> it.level.toString() + it.lang
                    is FilterableItem.LanguageHeader -> it.lang
                    is FilterableItem.Lemma -> "lemma" + it.lang
                    is FilterableItem.Property -> it.property.id.toString() + it.lang
                }
            },
        ) { idx, item ->
            val nextItem = items.getOrNull(idx + 1)
            val currentIndentLevel = item.indentLevel()
            val nextIndentLevel = nextItem?.indentLevel()
            val previousLevels = calculatePreviousLevels(idx, items)
            val nextPreviousLevels = calculatePreviousLevels(idx + 1, items)
            val hasNext =
                nextIndentLevel == currentIndentLevel || nextPreviousLevels.contains(currentIndentLevel)
            val selected = item == selectedItem
            when (item) {
                is FilterableItem.SectionHeader -> {
                    TreeItem(
                        modifier = Modifier.height(20.dp),
                        indentLevel = currentIndentLevel,
                        hasNext = hasNext,
                        previousLevels = previousLevels,
                    ) {
                        Text(
                            text = item.level.toReadableString(),
                            style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }

                is FilterableItem.LanguageHeader -> {
                    TreeItem(
                        modifier = Modifier.height(20.dp),
                        hasNext = hasNext,
                        indentLevel = currentIndentLevel,
                        previousLevels = previousLevels,
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = Spacing.xxs),
                            text = item.name,
                            style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }

                is FilterableItem.Lemma -> {
                    TreeItem(
                        modifier = Modifier.height(20.dp)
                            .fillMaxWidth()
                            .onClick {
                                onSelectionChanged?.invoke(item)
                            }.run {
                                if (selected) {
                                    background(color = Color.Blue)
                                } else {
                                    this
                                }
                            },
                        indentLevel = currentIndentLevel,
                        hasNext = hasNext,
                        previousLevels = previousLevels,
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = Spacing.xxs),
                            text = buildString {
                                append("term_detail_title_lemma".localized())
                                if (configurations[item]?.isEmpty == false) {
                                    append(" ✪")
                                }
                            }.localized(),
                            color = if (selected) Color.White else Color.Black,
                            style = MaterialTheme.typography.caption,
                        )
                    }
                }

                is FilterableItem.Property -> {
                    TreeItem(
                        modifier = Modifier.height(20.dp)
                            .fillMaxWidth()
                            .onClick {
                                onSelectionChanged?.invoke(item)
                            }.run {
                                if (selected) {
                                    background(color = Color.Blue)
                                } else {
                                    this
                                }
                            },
                        indentLevel = currentIndentLevel,
                        hasNext = hasNext,
                        previousLevels = previousLevels,
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = Spacing.xxs),
                            text = buildString {
                                append(item.property.name)
                                if (configurations[item]?.isEmpty == false) {
                                    append(" ✪")
                                }
                            },
                            color = if (selected) Color.White else Color.Black,
                            style = MaterialTheme.typography.caption,
                        )
                    }
                }
            }
        }
    }
}

private fun FilterableItem.indentLevel(): Int {
    return when (this) {
        is FilterableItem.Property -> when (property.level) {
            PropertyLevel.TERM -> 2
            else -> 1
        }

        is FilterableItem.Lemma -> 2
        is FilterableItem.LanguageHeader -> 1
        is FilterableItem.SectionHeader -> when (level) {
            PropertyLevel.TERM -> 1
            else -> 0
        }
    }
}

private fun calculatePreviousLevels(idx: Int, items: List<FilterableItem>): List<Int> {
    if (idx !in items.indices) {
        return emptyList()
    }
    val currentIndentLevel = items[idx].indentLevel()
    val previousLevels = mutableSetOf<Int>()
    for (i in idx until items.size) {
        val lvl = items[i].indentLevel()
        if (lvl in 1 until currentIndentLevel) {
            previousLevels.add(lvl)
        }
    }
    return previousLevels.toList()
}
