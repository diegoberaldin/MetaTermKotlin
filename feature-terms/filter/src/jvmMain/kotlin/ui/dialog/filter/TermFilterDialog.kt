package ui.dialog.filter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import data.LanguageModel
import data.SearchCriterion
import data.TermbaseModel
import kotlinx.coroutines.launch
import localized
import ui.components.CustomSpinner
import ui.components.CustomTextField
import ui.components.CustomTooltipArea
import ui.components.StyledLabel
import ui.theme.Indigo800
import ui.theme.MetaTermTheme
import ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TermFilterDialog(
    termbase: TermbaseModel,
    viewModel: TermFilterViewModel,
    sourceLanguage: LanguageModel?,
    criteria: List<SearchCriterion> = emptyList(),
    onConfirm: (List<SearchCriterion>) -> Unit,
    onClose: () -> Unit,
) {
    MetaTermTheme {
        Window(
            title = "dialog_title_filter".localized(),
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            resizable = false,
            onCloseRequest = {
                onClose()
            },
        ) {
            LaunchedEffect(termbase) {
                viewModel.loadInitial(
                    termbase = termbase,
                    criteria = criteria,
                    sourceLanguage = sourceLanguage,
                )
            }

            LaunchedEffect(viewModel) {
                launch {
                    viewModel.done.collect {
                        onConfirm(it)
                    }
                }
            }

            Column(
                modifier = Modifier.size(800.dp, 600.dp)
                    .background(MaterialTheme.colors.background)
                    .padding(Spacing.s),
            ) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = "dialog_filter_intro".localized(),
                        style = MaterialTheme.typography.caption,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    CustomTooltipArea(text = "tooltip_clear_current_filter".localized()) {
                        Icon(
                            modifier = Modifier.size(20.dp).onClick {
                                viewModel.clearCurrent()
                            },
                            imageVector = Icons.Filled.RemoveCircle,
                            tint = MaterialTheme.colors.primary,
                            contentDescription = null
                        )
                    }
                    CustomTooltipArea(text = "tooltip_clear_all_filters".localized()) {
                        Icon(
                            modifier = Modifier.size(20.dp).onClick {
                                viewModel.clearAll()
                            },
                            imageVector = Icons.Filled.ClearAll,
                            tint = MaterialTheme.colors.primary,
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.s))

                val propertiesUiState by viewModel.propertiesUiState.collectAsState()
                val uiState by viewModel.uiState.collectAsState()

                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    PropertyTree(
                        modifier = Modifier.weight(0.6f).fillMaxHeight(),
                        items = propertiesUiState.items,
                        configurations = propertiesUiState.configurations,
                        selectedItem = propertiesUiState.selectedItem,
                        onSelectionChanged = {
                            viewModel.toggleSelection(it)
                        }
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(2.dp),
                            ).padding(Spacing.s),
                        verticalArrangement = Arrangement.spacedBy(Spacing.s),
                    ) {
                        if (propertiesUiState.selectedItem == null) {
                            Text(
                                text = "dialog_filter_property_placeholder".localized(),
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray,
                            )
                        } else {
                            // header with field/lemma name
                            StyledLabel(
                                color = Indigo800,
                                title = "dialog_filter_header_field_descriptor".localized(),
                                internalPadding = PaddingValues(
                                    vertical = Spacing.s,
                                    horizontal = Spacing.s,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                content = {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = uiState.currentName,
                                        style = MaterialTheme.typography.caption,
                                    )
                                }
                            )

                            if (uiState.availableMatchTypes.isEmpty()) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "dialog_filter_message_no_filter".localized(),
                                    style = MaterialTheme.typography.caption,
                                )
                            } else {
                                // match configuration
                                StyledLabel(
                                    color = Indigo800,
                                    title = "dialog_filter_header_match_config".localized(),
                                    internalPadding = PaddingValues(
                                        vertical = Spacing.s,
                                        horizontal = 0.dp,
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    content = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(Spacing.s)
                                        ) {
                                            CustomSpinner(
                                                values = uiState.availableMatchTypes.map { it.toReadableString() },
                                                current = uiState.currentMatchType?.toReadableString()
                                                    ?: "dialog_filter_header_match_picklist_placeholder".localized(),
                                                valueColor = if (uiState.currentMatchType == null) Color.Gray else Color.Black,
                                                modifier = Modifier.fillMaxWidth().height(20.dp),
                                                onValueChanged = { index ->
                                                    viewModel.setCurrentMatchType(uiState.availableMatchTypes[index])
                                                }
                                            )

                                            // picklist properties allow just values in set
                                            if (uiState.availableValues != null) {
                                                CustomSpinner(
                                                    values = uiState.availableValues ?: emptyList(),
                                                    current = uiState.currentValue.ifEmpty { "dialog_filter_header_match_picklist_placeholder".localized() },
                                                    valueColor = if (uiState.currentValue.isEmpty()) Color.Gray else Color.Black,
                                                    modifier = Modifier.fillMaxWidth().height(20.dp),
                                                    onValueChanged = { index ->
                                                        val newValue = uiState.availableValues?.getOrNull(index)
                                                        if (newValue != null) {
                                                            viewModel.setCurrentValue(newValue)
                                                        }
                                                    }
                                                )
                                            } else if (uiState.currentMatchType != MatchType.SEARCHABLE) {
                                                CustomTextField(
                                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                                    label = "dialog_filter_header_match_value_title".localized(),
                                                    labelColor = Color.Black,
                                                    labelExtraSpacing = Spacing.xs,
                                                    labelStyle = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                                    hint = "dialog_filter_header_match_value_placeholder".localized(),
                                                    value = uiState.currentValue,
                                                    onValueChange = {
                                                        viewModel.setCurrentValue(it)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.s))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        modifier = Modifier.heightIn(max = 25.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            onClose()
                        },
                    ) {
                        Text(text = "button_close".localized(), style = MaterialTheme.typography.button)
                    }
                    Button(
                        modifier = Modifier.heightIn(max = 25.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            viewModel.submit()
                        },
                    ) {
                        Text(text = "button_apply".localized(), style = MaterialTheme.typography.button)
                    }
                }
            }
        }
    }
}
