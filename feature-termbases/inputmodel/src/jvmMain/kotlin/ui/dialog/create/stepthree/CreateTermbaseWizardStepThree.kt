package ui.dialog.create.stepthree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.PropertyLevel
import data.toReadableString
import localized
import ui.components.TreeItem
import ui.theme.Spacing

@Composable
fun CreateTermbaseWizardStepThree(
    modifier: Modifier = Modifier,
    viewModel: CreateTermbaseWizardStepThreeViewModel,
) {
    Box(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.s)) {
            Spacer(modifier = Modifier.height(Spacing.s))
            Row(
                modifier = Modifier.height(20.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "dialog_create_termbase_step_3_title".localized(),
                    style = MaterialTheme.typography.caption,
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.s))
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                val uiState by viewModel.uiState.collectAsState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(2.dp),
                        )
                        .padding(Dp.Hairline),
                ) {
                    itemsIndexed(
                        items = uiState.items,
                        key = { _, it ->
                            when (it) {
                                is CreateTermbaseWizardStepThreeItem.SectionHeader -> it.level.toString() + it.lang
                                is CreateTermbaseWizardStepThreeItem.LanguageHeader -> it.lang
                                is CreateTermbaseWizardStepThreeItem.Lemma -> "lemma" + it.lang
                                is CreateTermbaseWizardStepThreeItem.Property -> it.property.id.toString() + it.lang
                            }
                        },
                    ) { idx, item ->
                        val nextItem = uiState.items.getOrNull(idx + 1)
                        val currentIndentLevel = item.indentLevel()
                        val nextIndentLevel = nextItem?.indentLevel()
                        val previousLevels = calculatePreviousLevels(idx, uiState.items)
                        val nextPreviousLevels = calculatePreviousLevels(idx + 1, uiState.items)
                        val hasNext =
                            nextIndentLevel == currentIndentLevel || nextPreviousLevels.contains(currentIndentLevel)
                        when (item) {
                            is CreateTermbaseWizardStepThreeItem.SectionHeader -> {
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

                            is CreateTermbaseWizardStepThreeItem.LanguageHeader -> {
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

                            is CreateTermbaseWizardStepThreeItem.Lemma -> {
                                CheckableItem(
                                    title = "term_detail_title_lemma".localized(),
                                    checked = item.selected,
                                    indentLevel = currentIndentLevel,
                                    hasNext = hasNext,
                                    previousLevels = previousLevels,
                                    onCheckedChange = {
                                        viewModel.toggleSelection(
                                            item = item,
                                            selected = it,
                                        )
                                    },
                                )
                            }

                            is CreateTermbaseWizardStepThreeItem.Property -> {
                                CheckableItem(
                                    title = item.property.name,
                                    checked = item.selected,
                                    indentLevel = currentIndentLevel,
                                    hasNext = hasNext,
                                    previousLevels = previousLevels,
                                    onCheckedChange = {
                                        viewModel.toggleSelection(
                                            item = item,
                                            selected = it,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun CreateTermbaseWizardStepThreeItem.indentLevel(): Int {
    return when (this) {
        is CreateTermbaseWizardStepThreeItem.Property -> when (property.level) {
            PropertyLevel.TERM -> 2
            else -> 1
        }

        is CreateTermbaseWizardStepThreeItem.Lemma -> 2
        is CreateTermbaseWizardStepThreeItem.LanguageHeader -> 1
        is CreateTermbaseWizardStepThreeItem.SectionHeader -> when (level) {
            PropertyLevel.TERM -> 1
            else -> 0
        }
    }
}

private fun calculatePreviousLevels(idx: Int, items: List<CreateTermbaseWizardStepThreeItem>): List<Int> {
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
