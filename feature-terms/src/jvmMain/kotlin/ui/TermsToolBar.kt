package ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import data.LanguageModel
import localized
import ui.components.CustomSpinner
import ui.components.CustomTextField
import ui.components.CustomTooltipArea
import ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun TermsToolBar(
    sourceLanguages: List<LanguageModel>,
    sourceLanguage: LanguageModel?,
    targetLanguages: List<LanguageModel>,
    targetLanguage: LanguageModel?,
    modifier: Modifier = Modifier,
    entryEditMode: Boolean = false,
    onNewEntry: (() -> Unit)? = null,
    onDeleteEntry: (() -> Unit)? = null,
    onSourceLanguageChanged: ((LanguageModel) -> Unit)? = null,
    onTargetLanguageChanged: ((LanguageModel) -> Unit)? = null,
    onSwitchLanguages: (() -> Unit)? = null,
    onToggleEditMode: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    currentSearch: String = "",
    onSearchChanged: ((String) -> Unit)? = null,
    onSearchFired: (() -> Unit)? = null,
    onOpenFilter: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.padding(vertical = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            LanguageChooser(
                languages = sourceLanguages,
                currentLanguage = sourceLanguage,
                onLanguageChanged = onSourceLanguageChanged,
            )
            CustomTooltipArea(text = "toolbar_switch_languages_tooltip".localized()) {
                Icon(
                    modifier = Modifier.size(24.dp).onClick {
                        onSwitchLanguages?.invoke()
                    },
                    imageVector = Icons.Filled.CompareArrows,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
            LanguageChooser(
                languages = targetLanguages,
                currentLanguage = targetLanguage,
                onLanguageChanged = onTargetLanguageChanged,
            )
        }

        Spacer(modifier = Modifier.width(Spacing.s))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomTooltipArea(text = "toolbar_create_entry_tooltip".localized()) {
                Icon(
                    modifier = Modifier.width(20.dp).onClick { onNewEntry?.invoke() },
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
            }
            Spacer(modifier = Modifier.width(Spacing.xs))
            CustomTooltipArea(text = "toolbar_delete_entry_tooltip".localized()) {
                Icon(
                    modifier = Modifier.width(20.dp).onClick { onDeleteEntry?.invoke() },
                    imageVector = Icons.Outlined.RemoveCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
            }
            Spacer(modifier = Modifier.width(Spacing.xs))
            CustomTooltipArea(text = if (entryEditMode) "toolbar_exit_edit_mode".localized() else "toolbar_enter_edit_mode".localized()) {
                Icon(
                    modifier = Modifier.width(20.dp).onClick {
                        onToggleEditMode?.invoke()
                    },
                    imageVector = if (entryEditMode) Icons.Outlined.EditOff else Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
            }
            Spacer(modifier = Modifier.width(Spacing.xs))
            CustomTooltipArea(text = "toolbar_entry_save".localized()) {
                Icon(
                    modifier = Modifier.width(20.dp).onClick {
                        onSave?.invoke()
                    },
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
            }
        }

        Spacer(modifier = Modifier.width(Spacing.s))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${"toolbar_search_title".localized()}:",
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
            Spacer(modifier = Modifier.width(Spacing.s))
            CustomTextField(
                modifier = Modifier.weight(1f).height(24.dp).onPreviewKeyEvent {
                    when {
                        it.type == KeyEventType.KeyDown && it.key == Key.Enter -> {
                            onSearchFired?.invoke()
                            true
                        }

                        else -> false
                    }
                },
                hint = "toolbar_search_placeholder".localized(),
                singleLine = true,
                value = currentSearch,
                onValueChange = {
                    onSearchChanged?.invoke(it)
                },
                endButton = {
                    if (currentSearch.isEmpty()) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    } else {
                        Icon(
                            modifier = Modifier.onClick {
                                onSearchChanged?.invoke("")
                                onSearchFired?.invoke()
                            },
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.width(Spacing.s))
            CustomTooltipArea(text = "toolbar_open_filter".localized()) {
                Icon(
                    modifier = Modifier.onClick {
                        onOpenFilter?.invoke()
                    },
                    imageVector = Icons.Filled.FilterList,
                    tint = MaterialTheme.colors.primary,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun LanguageChooser(
    languages: List<LanguageModel>,
    currentLanguage: LanguageModel?,
    onLanguageChanged: ((LanguageModel) -> Unit)? = null,
) {
    CustomSpinner(
        values = languages.map { it.name },
        current = currentLanguage?.name,
        onValueChanged = {
            onLanguageChanged?.invoke(languages[it])
        },
    )
}
