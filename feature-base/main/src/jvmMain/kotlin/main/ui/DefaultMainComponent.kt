package main.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import common.utils.getByInjection
import data.TermbaseModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import repo.EntryRepository
import terms.ui.TermsComponent
import kotlin.coroutines.CoroutineContext

internal class DefaultMainComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val entryRepository: EntryRepository,
) : MainComponent, ComponentContext by componentContext {

    private val termbase = MutableStateFlow<TermbaseModel?>(null)
    private val entryCount = MutableStateFlow(0)
    private var observeEntriesJob: Job? = null
    private lateinit var viewModelScope: CoroutineScope
    private val termsNavigation = SlotNavigation<MainComponent.TermsConfig>()

    override lateinit var uiState: StateFlow<MainUiState>
    override val terms: Value<ChildSlot<MainComponent.TermsConfig, *>> = childSlot(
        source = termsNavigation,
        key = "MainTermsSlot",
        childFactory = { _, context ->
            getByInjection<TermsComponent>(context, coroutineContext)
        }
    )

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
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
                termsNavigation.activate(MainComponent.TermsConfig)
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun load(termbase: TermbaseModel) {
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
