package ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.arkivanov.essenty.instancekeeper.getOrCreate
import org.koin.java.KoinJavaComponent
import ui.detail.TermDetail
import ui.detail.TermDetailViewModel
import ui.dialog.filter.TermFilterDialog
import ui.dialog.filter.TermFilterViewModel
import ui.list.TermsList
import ui.theme.Spacing
import utils.AppBusiness

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TermsScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: TermsViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: TermsViewModel by KoinJavaComponent.inject(TermsViewModel::class.java)
        res
    }
    val termDetailViewModel: TermDetailViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: TermDetailViewModel by KoinJavaComponent.inject(TermDetailViewModel::class.java)
        res
    }
    val uiState by viewModel.uiState.collectAsState()
    val toolbarUiState by viewModel.toolbarUiState.collectAsState()
    val searchUiState by viewModel.searchUiState.collectAsState()

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
    LaunchedEffect(languagesState) {
        termDetailViewModel.setLanguages(languagesState)
    }

    var filterDialogOpen by remember {
        mutableStateOf(false)
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
                viewModel.changeSourceLanguage(it)
            },
            onTargetLanguageChanged = {
                viewModel.changeTargetLanguage(it)
            },
            onSwitchLanguages = {
                viewModel.switchLanguages()
            },
            onNewEntry = {
                viewModel.sendCreateEntryEvent()
            },
            onDeleteEntry = {
                viewModel.deleteCurrentEntry()
            },
            onToggleEditMode = {
                if (uiState.selectedEntry != null) {
                    viewModel.setEntryEditMode(!uiState.entryEditMode)
                }
            },
            onSave = {
                termDetailViewModel.save()
            },
            onSearchChanged = {
                viewModel.setSearch(it)
            },
            onSearchFired = {
                viewModel.searchTerms()
            },
            onOpenFilter = {
                filterDialogOpen = true
            },
        )
        Row(modifier = Modifier.padding(start = Spacing.s, bottom = Spacing.xxs, top = Spacing.xxs)) {
            val termsFocusRequester = remember { FocusRequester() }
            TermsList(
                modifier = Modifier.weight(0.25f).onPreviewKeyEvent {
                    when {
                        it.type == KeyDown && it.key == Key.DirectionUp -> {
                            val index = uiState.terms.indexOfFirst { t -> t.id == uiState.selectedTerm?.id }
                            if (index > 0) {
                                viewModel.selectTerm(uiState.terms[index - 1])
                            }
                            true
                        }

                        it.type == KeyDown && it.key == Key.DirectionDown -> {
                            val index = uiState.terms.indexOfFirst { t -> t.id == uiState.selectedTerm?.id }
                            if (index < uiState.terms.count() - 1) {
                                viewModel.selectTerm(uiState.terms[index + 1])
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
                        viewModel.selectTerm(null)
                        termsFocusRequester.freeFocus()
                    } else {
                        viewModel.selectTerm(it)
                        termsFocusRequester.requestFocus()
                    }
                },
            )

            TermDetail(
                modifier = Modifier.weight(1f),
                entry = uiState.selectedEntry,
                editMode = uiState.entryEditMode,
                searchCriteria = searchUiState.searchCriteria,
            )
        }
    }

    if (filterDialogOpen) {
        val termbase = uiState.currentTermbase
        if (termbase != null) {
            TermFilterDialog(
                termbase = termbase,
                sourceLanguage = toolbarUiState.sourceLanguage,
                criteria = searchUiState.searchCriteria,
                onConfirm = { criteria ->
                    viewModel.setSearchCriteria(criteria)
                    filterDialogOpen = false
                },
                onClose = {
                    filterDialogOpen = false
                },
            )
        }
    }
}
