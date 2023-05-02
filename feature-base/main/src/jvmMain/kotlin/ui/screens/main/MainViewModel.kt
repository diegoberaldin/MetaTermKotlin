package ui.screens.main

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import coroutines.CoroutineDispatcherProvider
import data.TermbaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import repository.EntryRepository

class MainViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val entryRepository: EntryRepository,
) : InstanceKeeper.Instance {

    private val termbase = MutableStateFlow<TermbaseModel?>(null)
    private val entryCount = MutableStateFlow(0)
    private var observeEntriesJob: Job? = null
    private val viewModelScope = CoroutineScope(SupervisorJob())

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

    override fun onDestroy() {
        viewModelScope.cancel()
    }

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
