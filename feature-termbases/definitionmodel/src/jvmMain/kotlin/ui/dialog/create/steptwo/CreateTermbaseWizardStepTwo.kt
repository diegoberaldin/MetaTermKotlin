package ui.dialog.create.steptwo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.PropertyLevel
import data.PropertyType
import data.toReadableString
import localized
import ui.components.CustomSpinner
import ui.components.CustomTextField
import ui.components.StyledLabel
import ui.components.TreeItem
import ui.theme.Purple800
import ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CreateTermbaseWizardStepTwo(
    modifier: Modifier = Modifier,
    viewModel: CreateTermbaseWizardStepTwoViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    Box(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.s)) {
            Spacer(modifier = Modifier.height(Spacing.s))
            var newPropertyLevelChooserExpanded by remember {
                mutableStateOf(false)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "dialog_create_termbase_step_2_title".localized(),
                    style = MaterialTheme.typography.caption,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    Icon(
                        modifier = Modifier.width(20.dp).onClick {
                            newPropertyLevelChooserExpanded = true
                        },
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                    )
                    DropdownMenu(
                        modifier = Modifier.width(300.dp)
                            .background(Color.White)
                            .border(
                                width = Dp.Hairline,
                                color = MaterialTheme.colors.background,
                                shape = RoundedCornerShape(4.dp),
                            ),
                        expanded = newPropertyLevelChooserExpanded,
                        onDismissRequest = {
                            newPropertyLevelChooserExpanded = false
                        },
                    ) {
                        var hoveredValue by remember {
                            mutableStateOf<PropertyLevel?>(null)
                        }
                        val availableLevels = listOf(
                            PropertyLevel.ENTRY,
                            PropertyLevel.LANGUAGE,
                            PropertyLevel.TERM,
                        )
                        for (level in availableLevels) {
                            DropdownMenuItem(
                                modifier = Modifier
                                    .background(color = if (level == hoveredValue) Color.Blue else Color.Transparent)
                                    .fillMaxWidth().height(20.dp)
                                    .onPointerEvent(PointerEventType.Enter) { hoveredValue = level }
                                    .onPointerEvent(PointerEventType.Exit) { hoveredValue = null }
                                    .padding(horizontal = Spacing.s, vertical = Spacing.xxs),
                                onClick = {
                                    viewModel.addProperty(level)
                                    newPropertyLevelChooserExpanded = false
                                },
                            ) {
                                Text(
                                    text = level.toReadableString(),
                                    style = MaterialTheme.typography.caption,
                                    color = if (level == hoveredValue) Color.White else Color.Black,
                                )
                            }
                        }
                    }
                }
                Icon(
                    modifier = Modifier.width(20.dp).onClick(
                        enabled = uiState.selectedProperty != null,
                        onClick = {
                            viewModel.removeProperty()
                        },
                    ),
                    imageVector = Icons.Outlined.RemoveCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.s))
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(2.dp),
                        ).padding(Dp.Hairline),
                ) {
                    itemsIndexed(
                        items = uiState.items,
                        key = { _, it ->
                            when (it) {
                                is CreateTermbaseStepTwoItem.SectionHeader -> it.level
                                is CreateTermbaseStepTwoItem.Property -> it.property.id
                            }
                        },
                    ) { idx, item ->
                        val nextItem = uiState.items.getOrNull(idx + 1)
                        when (item) {
                            is CreateTermbaseStepTwoItem.SectionHeader -> {
                                TreeItem(
                                    modifier = Modifier.height(20.dp),
                                    indentLevel = item.indentLevel(),
                                ) {
                                    Text(
                                        text = item.level.toReadableString(),
                                        style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                    )
                                }
                            }

                            is CreateTermbaseStepTwoItem.Property -> {
                                PropertyCell(
                                    name = item.property.name,
                                    selected = item.property == uiState.selectedProperty,
                                    hasNext = nextItem?.indentLevel() == item.indentLevel(),
                                    onSelected = {
                                        viewModel.selectProperty(item.property)
                                    },
                                )
                            }
                        }
                    }
                }
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
                    if (uiState.selectedProperty == null) {
                        Text(
                            text = "definition_model_placeholder".localized(),
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray,
                        )
                    } else {
                        val currentState by viewModel.currentPropertyState.collectAsState()

                        StyledLabel(
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            color = Purple800,
                            title = "definition_model_property_name".localized(),
                            internalPadding = PaddingValues(
                                vertical = Spacing.s,
                                horizontal = 0.dp,
                            ),
                        ) {
                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = currentState.name,
                                onValueChange = {
                                    viewModel.setCurrentPropertyName(it)
                                },
                            )
                        }
                        StyledLabel(
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            color = Purple800,
                            title = "definition_model_property_level".localized(),
                            internalPadding = PaddingValues(0.dp),
                        ) {
                            val values = remember {
                                listOf(
                                    PropertyLevel.ENTRY,
                                    PropertyLevel.LANGUAGE,
                                    PropertyLevel.TERM,
                                )
                            }
                            CustomSpinner(
                                modifier = Modifier.fillMaxSize(),
                                values = values.map { it.toReadableString() },
                                current = currentState.level?.toReadableString(),
                                onValueChanged = {
                                    viewModel.setCurrentPropertyLevel(values[it])
                                },
                            )
                        }
                        StyledLabel(
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            color = Purple800,
                            title = "definition_model_property_type".localized(),
                            internalPadding = PaddingValues(0.dp),
                        ) {
                            val values = remember {
                                listOf(
                                    PropertyType.TEXT,
                                    PropertyType.IMAGE,
                                    PropertyType.PICKLIST,
                                )
                            }
                            CustomSpinner(
                                modifier = Modifier.fillMaxSize(),
                                values = values.map { it.toReadableString() },
                                current = currentState.type?.toReadableString(),
                                onValueChanged = {
                                    viewModel.setCurrentPropertyType(values[it])
                                },
                            )
                        }

                        if (currentState.type == PropertyType.PICKLIST) {
                            StyledLabel(
                                modifier = Modifier.fillMaxWidth(),
                                color = Purple800,
                                title = "definition_model_property_picklist_values".localized(),
                                internalPadding = PaddingValues(0.dp),
                            ) {
                                var newPicklistValue by remember {
                                    mutableStateOf("")
                                }
                                Column(
                                    modifier = Modifier.verticalScroll(rememberScrollState())
                                        .padding(Spacing.s),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                                ) {
                                    CustomTextField(
                                        modifier = Modifier.fillMaxWidth().height(24.dp).border(
                                            width = Dp.Hairline,
                                            color = Purple800,
                                            shape = RoundedCornerShape(4.dp),
                                        ).onPreviewKeyEvent {
                                            when {
                                                it.type == KeyEventType.KeyDown && it.key == Key.Enter -> {
                                                    if (newPicklistValue.isNotEmpty()) {
                                                        viewModel.addPicklistValue(newPicklistValue)
                                                        newPicklistValue = ""
                                                    }
                                                    true
                                                }

                                                else -> false
                                            }
                                        },
                                        hint = "insert_value_placeholder".localized(),
                                        value = newPicklistValue,
                                        onValueChange = {
                                            newPicklistValue = it
                                        },
                                        endButton = {
                                            Icon(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .onClick {
                                                        if (newPicklistValue.isNotEmpty()) {
                                                            viewModel.addPicklistValue(newPicklistValue)
                                                            newPicklistValue = ""
                                                        }
                                                    },
                                                imageVector = Icons.Filled.AddCircle,
                                                tint = Purple800,
                                                contentDescription = null,
                                            )
                                        },
                                    )
                                    currentState.picklistValues.forEachIndexed { idx, value ->
                                        PicklistValueCell(
                                            value = value,
                                            onRemove = {
                                                viewModel.removePicklistValue(idx)
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
    }
}

private fun CreateTermbaseStepTwoItem.indentLevel(): Int {
    return when (this) {
        is CreateTermbaseStepTwoItem.SectionHeader -> 0
        is CreateTermbaseStepTwoItem.Property -> 1
    }
}
