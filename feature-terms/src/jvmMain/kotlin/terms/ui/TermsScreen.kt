package terms.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import common.ui.theme.Spacing
import dialogfilter.ui.TermFilterComponent
import termdetail.ui.TermDetail
import termdetail.ui.TermDetailComponent
import dialogfilter.ui.TermFilterDialog
import termlist.ui.TermsList

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TermsScreen(
    component: TermsComponent,
    modifier: Modifier = Modifier,
) {
    val uiState by component.uiState.collectAsState()
    val toolbarUiState by component.toolbarUiState.collectAsState()
    val searchUiState by component.searchUiState.collectAsState()
    val termDetail by component.termDetail.subscribeAsState()
    val dialog by component.dialog.subscribeAsState()

    val languagesState by derivedStateOf {
        buildList {
            val source = toolbarUiState.sourceLanguage
            if (source != null) {
                this += source
            }
            val target = toolbarUiState.targetLanguage
            if (target != null) {
                this += target
            }
        }
    }

    Column(modifier = modifier) {
        TermsToolBar(
            modifier = Modifier.padding(horizontal = Spacing.s),
            sourceLanguage = toolbarUiState.sourceLanguage,
            sourceLanguages = toolbarUiState.sourceLanguages,
            targetLanguage = toolbarUiState.targetLanguage,
            targetLanguages = toolbarUiState.targetLanguages,
            entryEditMode = uiState.entryEditMode,
            currentSearch = searchUiState.searchText,
            onSourceLanguageChanged = {
                component.changeSourceLanguage(it)
            },
            onTargetLanguageChanged = {
                component.changeTargetLanguage(it)
            },
            onSwitchLanguages = {
                component.switchLanguages()
            },
            onNewEntry = {
                component.sendCreateEntryEvent()
            },
            onDeleteEntry = {
                component.deleteCurrentEntry()
            },
            onToggleEditMode = {
                if (uiState.selectedEntry != null) {
                    component.setEntryEditMode(!uiState.entryEditMode)
                }
            },
            onSave = {
                (termDetail.child?.instance as? TermDetailComponent)?.save()
            },
            onSearchChanged = {
                component.setSearch(it)
            },
            onSearchFired = {
                component.searchTerms()
            },
            onOpenFilter = {
                component.openDialog(TermsComponent.DialogConfig.Filter)
            },
        )
        Row(modifier = Modifier.padding(start = Spacing.s, bottom = Spacing.xxs, top = Spacing.xxs).fillMaxSize()) {
            val termsFocusRequester = remember { FocusRequester() }
            TermsList(
                modifier = Modifier.weight(0.25f).fillMaxHeight().onPreviewKeyEvent {
                    when {
                        it.type == KeyDown && it.key == Key.DirectionUp -> {
                            val index = uiState.terms.indexOfFirst { t -> t.id == uiState.selectedTerm?.id }
                            if (index > 0) {
                                component.selectTerm(uiState.terms[index - 1])
                            }
                            true
                        }

                        it.type == KeyDown && it.key == Key.DirectionDown -> {
                            val index = uiState.terms.indexOfFirst { t -> t.id == uiState.selectedTerm?.id }
                            if (index < uiState.terms.count() - 1) {
                                component.selectTerm(uiState.terms[index + 1])
                            }
                            true
                        }

                        else -> {
                            false
                        }
                    }
                }
                    .focusRequester(termsFocusRequester)
                    .focusable(),
                terms = uiState.terms,
                current = uiState.selectedTerm,
                onSelected = {
                    if (it.id == uiState.selectedTerm?.id) {
                        component.selectTerm(null)
                        termsFocusRequester.freeFocus()
                    } else {
                        component.selectTerm(it)
                        termsFocusRequester.requestFocus()
                    }
                },
            )

            when (termDetail.child?.configuration) {
                TermsComponent.TermDetailConfig -> {
                    val childComponent = termDetail.child?.instance as TermDetailComponent
                    LaunchedEffect(childComponent, languagesState) {
                        childComponent.setLanguages(languagesState)
                    }
                    TermDetail(
                        component = childComponent,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        entry = uiState.selectedEntry,
                        editMode = uiState.entryEditMode,
                        searchCriteria = searchUiState.searchCriteria,
                    )
                }

                else -> Unit
            }
        }
    }

    when (dialog.child?.configuration) {
        TermsComponent.DialogConfig.Filter -> {
            val childComponent = dialog.child?.instance as TermFilterComponent
            val termbase = uiState.currentTermbase
            if (termbase != null) {
                TermFilterDialog(
                    component = childComponent,
                    termbase = termbase,
                    sourceLanguage = toolbarUiState.sourceLanguage,
                    criteria = searchUiState.searchCriteria,
                    onConfirm = { criteria ->
                        component.setSearchCriteria(criteria)
                        component.closeDialog()
                    },
                    onClose = {
                        component.closeDialog()
                    },
                )
            }
        }

        else -> Unit
    }
}
