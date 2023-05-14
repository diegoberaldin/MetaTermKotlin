package ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.essenty.instancekeeper.getOrCreate
import data.EntryModel
import data.PropertyModel
import data.SearchCriterion
import localized
import org.koin.java.KoinJavaComponent.inject
import ui.detail.widgets.CreateButton
import ui.detail.widgets.EntryIdWidget
import ui.detail.widgets.LanguageTitleWidget
import ui.detail.widgets.PropertyInputWidget
import ui.detail.widgets.PropertyWidget
import ui.detail.widgets.TermInputWidget
import ui.detail.widgets.TermWidget
import ui.theme.DeepPurple300
import ui.theme.DeepPurple800
import ui.theme.Indigo800
import ui.theme.Purple800
import ui.theme.Spacing
import utils.AppBusiness

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TermDetail(
    entry: EntryModel?,
    modifier: Modifier = Modifier,
    editMode: Boolean = false,
    searchCriteria: List<SearchCriterion> = emptyList(),
) {
    val viewModel: TermDetailViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: TermDetailViewModel by inject(TermDetailViewModel::class.java)
        res
    }
    LaunchedEffect(entry, searchCriteria) {
        viewModel.load(
            entryId = entry?.id,
            termbaseId = entry?.termbaseId,
            newEntry = entry?.new ?: false,
            searchCriteria = searchCriteria,
        )
    }
    LaunchedEffect(editMode) {
        viewModel.setEditMode(editMode)
    }
    val uiState by viewModel.uiState.collectAsState()
    val availablePropertiesUiState by viewModel.availablePropertiesUiState.collectAsState()

    Box(
        modifier = modifier.padding(horizontal = Spacing.s).fillMaxSize()
            .background(Color.White, shape = RoundedCornerShape(4.dp)),
    ) {
        if (uiState.items.isEmpty()) {
            if (!uiState.loading) {
                Text(
                    modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s),
                    text = "term_detail_placeholder".localized(),
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = Spacing.s, vertical = Spacing.xxs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                itemsIndexed(uiState.items) { idx, termDetailItem ->
                    when (termDetailItem) {
                        is TermDetailItem.EntryId -> {
                            EntryIdWidget(
                                modifier = Modifier.padding(vertical = Spacing.xxs),
                                entryId = termDetailItem.id,
                            )
                        }

                        is TermDetailItem.LanguageTitle -> {
                            LanguageTitleWidget(
                                modifier = Modifier.fillMaxWidth().padding(top = Spacing.s, start = Spacing.xs),
                                flag = termDetailItem.flag,
                                name = termDetailItem.language.name,
                            )
                        }

                        is TermDetailItem.Term -> {
                            TermWidget(
                                lemma = termDetailItem.lemma,
                            )
                        }

                        is TermDetailItem.Property -> {
                            val color = getPropertyColor(
                                entryId = termDetailItem.entryId,
                                languageId = termDetailItem.languageId,
                                termId = termDetailItem.termId,
                            )
                            PropertyWidget(
                                color = color,
                                name = termDetailItem.name,
                                value = termDetailItem.value,
                                type = termDetailItem.type,
                            )
                        }

                        is TermDetailItem.CreateTermButton -> {
                            CreateButton(
                                modifier = Modifier.fillMaxWidth(),
                                color = DeepPurple300,
                                title = "term_detail_add_term".localized(),
                                onClick = {
                                    viewModel.startInsertTermAt(index = idx, langCode = termDetailItem.langCode)
                                },
                            )
                        }

                        is TermDetailItem.EditTermField -> {
                            TermInputWidget(
                                value = termDetailItem.lemma,
                                onValueChange = {
                                    viewModel.setTermLemma(index = idx, lemma = it)
                                },
                                onDelete = {
                                    viewModel.markTermFormDeletion(idx)
                                },
                            )
                        }

                        is TermDetailItem.CreatePropertyButton -> {
                            val color = getPropertyColor(
                                entryId = termDetailItem.entryId,
                                languageId = termDetailItem.languageId,
                                termId = termDetailItem.termId,
                            )
                            val availableProperties = when {
                                termDetailItem.entryId != null -> availablePropertiesUiState.entryLevelProperties
                                termDetailItem.languageId != null -> availablePropertiesUiState.languageLevelProperties
                                termDetailItem.termId != null -> availablePropertiesUiState.termLevelProperties
                                else -> emptyList()
                            }
                            var propertyChooserExpanded by remember {
                                mutableStateOf(false)
                            }
                            Box {
                                CreateButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = color,
                                    title = "term_detail_add_property".localized(),
                                    onClick = {
                                        when {
                                            termDetailItem.entryId != null -> {
                                                viewModel.calculateAvailableEntryProperties()
                                            }

                                            termDetailItem.languageId != null -> {
                                                viewModel.calculateAvailableLanguageProperties(index = idx)
                                            }

                                            termDetailItem.termId != null -> {
                                                viewModel.calculateAvailableTermProperties(index = idx)
                                            }

                                            else -> Unit
                                        }
                                        propertyChooserExpanded = true
                                    },
                                )

                                DropdownMenu(
                                    modifier = Modifier.width(300.dp)
                                        .background(Color.White)
                                        .border(
                                            width = Dp.Hairline,
                                            color = MaterialTheme.colors.background,
                                            shape = RoundedCornerShape(4.dp),
                                        ),
                                    expanded = propertyChooserExpanded,
                                    onDismissRequest = {
                                        propertyChooserExpanded = false
                                    },
                                ) {
                                    var hoveredValue by remember {
                                        mutableStateOf<PropertyModel?>(null)
                                    }
                                    for (property in availableProperties) {
                                        DropdownMenuItem(
                                            modifier = Modifier
                                                .background(color = if (property == hoveredValue) Color.Blue else Color.Transparent)
                                                .fillMaxWidth().height(20.dp)
                                                .onPointerEvent(PointerEventType.Enter) { hoveredValue = property }
                                                .onPointerEvent(PointerEventType.Exit) { hoveredValue = null }
                                                .padding(horizontal = Spacing.s, vertical = Spacing.xxs),
                                            onClick = {
                                                viewModel.startInsertPropertyAt(
                                                    propertyId = property.id,
                                                    entryId = termDetailItem.entryId,
                                                    languageId = termDetailItem.languageId,
                                                    termId = termDetailItem.termId,
                                                    valueId = 0,
                                                    index = idx,
                                                    new = true,
                                                )
                                            },
                                        ) {
                                            Text(
                                                text = property.name,
                                                style = MaterialTheme.typography.caption,
                                                color = if (property == hoveredValue) Color.White else Color.Black,
                                            )
                                        }
                                    }
                                    if (availableProperties.isEmpty()) {
                                        DropdownMenuItem(
                                            modifier = Modifier
                                                .background(color = Color.Transparent)
                                                .fillMaxWidth().height(20.dp)
                                                .padding(horizontal = Spacing.s, vertical = Spacing.xxs),
                                            onClick = {},
                                        ) {
                                            Text(
                                                text = "empty_properties_placeholder".localized(),
                                                style = MaterialTheme.typography.caption,
                                                color = Color.Gray,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        is TermDetailItem.EditPropertyField -> {
                            val color = getPropertyColor(
                                entryId = termDetailItem.entryId,
                                languageId = termDetailItem.languageId,
                                termId = termDetailItem.termId,
                            )
                            PropertyInputWidget(
                                type = termDetailItem.type,
                                name = termDetailItem.name,
                                value = termDetailItem.value,
                                picklistValues = termDetailItem.picklistValues,
                                color = color,
                                onValueChange = {
                                    viewModel.setPropertyValue(value = it, index = idx)
                                },
                                onDelete = {
                                    viewModel.markPropertyForDeletion(index = idx)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getPropertyColor(
    entryId: Int? = null,
    languageId: Int? = null,
    termId: Int? = null,
) = when {
    entryId != null -> Purple800
    languageId != null -> DeepPurple800
    termId != null -> Indigo800
    else -> Color.Transparent
}
