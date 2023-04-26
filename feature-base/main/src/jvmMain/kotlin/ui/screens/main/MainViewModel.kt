package ui.screens.main

import data.TermbaseModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import repository.EntryRepository
import coroutines.CoroutineDispatcherProvider

class MainViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val entryRepository: EntryRepository,
) : ViewModel() {

    private val termbase = MutableStateFlow<TermbaseModel?>(null)
    private val entryCount = MutableStateFlow(0)
    private var observeEntriesJob: Job? = null

    val uiState = combine(
        termbase,
        entryCount,
    ) { termbase, entryCount ->
        MainUiState(
            termbase = termbase,
            entryCount = entryCount,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun load(termbase: TermbaseModel) {
        this.termbase.value = termbase

        viewModelScope.launch(dispatcherProvider.io) {
            entryCount.value = entryRepository.getAll(termbase.id).count()
            observeEntriesJob?.cancel()
            observeEntriesJob = launch {
                entryRepository.observeEntries(termbase.id).collect { entries ->
                    entryCount.value = entries.count()
                }
            }
        }
    }
}
