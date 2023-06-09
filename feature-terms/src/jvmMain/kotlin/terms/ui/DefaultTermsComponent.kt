package terms.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import data.EntryModel
import data.LanguageModel
import data.SearchCriterion
import data.TermModel
import data.TermbaseModel
import data.includingSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import common.log.LogManager
import common.notification.NotificationCenter
import common.utils.getByInjection
import dialogfilter.ui.TermFilterComponent
import kotlinx.coroutines.flow.*
import repo.EntryRepository
import repo.LanguageNameRepository
import repo.LanguageRepository
import termdetail.ui.TermDetailComponent
import usecase.DeleteEntryUseCase
import usecase.SearchTermsUseCase
import kotlin.coroutines.CoroutineContext

internal class DefaultTermsComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val languageRepository: LanguageRepository,
    private val languageNameRepository: LanguageNameRepository,
    private val entryRepository: EntryRepository,
    private val notificationCenter: NotificationCenter,
    private val deleteEntryUseCase: DeleteEntryUseCase,
    private val searchTermsUseCase: SearchTermsUseCase,
    private val log: LogManager,
) : TermsComponent, ComponentContext by componentContext {

    private val currentTermbase = MutableStateFlow<TermbaseModel?>(null)
    private val terms = MutableStateFlow(listOf<TermModel>())
    private val selectedTerm = MutableStateFlow<TermModel?>(null)
    private val selectedEntry = MutableStateFlow<EntryModel?>(null)
    private val entryEditMode = MutableStateFlow(false)
    private val sourceLanguage = MutableStateFlow<LanguageModel?>(null)
    private val sourceLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())
    private val targetLanguage = MutableStateFlow<LanguageModel?>(null)
    private val targetLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())
    private val searchText = MutableStateFlow("")
    private var searchCriteria = MutableStateFlow<List<SearchCriterion>>(emptyList())
    private var termsJob: Job? = null
    private lateinit var viewModelScope: CoroutineScope
    private val termDetailNavigation = SlotNavigation<TermsComponent.TermDetailConfig>()
    private val dialogNavigation = SlotNavigation<TermsComponent.DialogConfig>()

    override lateinit var uiState: StateFlow<TermsUiState>
    override lateinit var searchUiState: StateFlow<TermsSearchUiState>
    override lateinit var toolbarUiState: StateFlow<TermsToolbarUiState>
    override val termDetail: Value<ChildSlot<TermsComponent.TermDetailConfig, *>> = childSlot(
        source = termDetailNavigation,
        key = "TermsTermDetailSlot",
        childFactory = { _, component ->
            getByInjection<TermDetailComponent>(component, coroutineContext)
        }
    )
    override val dialog: Value<ChildSlot<TermsComponent.DialogConfig, *>> = childSlot(
        source = dialogNavigation,
        key = "TermsDialogSlot",
        childFactory = { _, component ->
            getByInjection<TermFilterComponent>(component, coroutineContext)
        }
    )

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
                    currentTermbase,
                    terms,
                    selectedEntry,
                    entryEditMode,
                    selectedTerm,
                ) { currentTermbase, terms, selectedEntry, entryEditMode, selectedTerm ->
                    TermsUiState(
                        currentTermbase = currentTermbase,
                        terms = terms,
                        selectedEntry = selectedEntry,
                        entryEditMode = entryEditMode,
                        selectedTerm = selectedTerm,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = TermsUiState(),
                )
                searchUiState = combine(
                    searchText,
                    searchCriteria,
                ) { searchText, searchCriteria ->
                    TermsSearchUiState(
                        searchText = searchText,
                        searchCriteria = searchCriteria,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = TermsSearchUiState(),
                )
                toolbarUiState = combine(
                    sourceLanguages,
                    sourceLanguage,
                    targetLanguages,
                    targetLanguage,

                    ) { sourceLanguages, sourceLanguage, targetLanguages, targetLanguage ->
                    TermsToolbarUiState(
                        sourceLanguages = sourceLanguages,
                        sourceLanguage = sourceLanguage,
                        targetLanguages = targetLanguages,
                        targetLanguage = targetLanguage,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = TermsToolbarUiState(),
                )

                viewModelScope.launch(dispatcherProvider.io) {
                    launch {
                        handleCurrentLanguageTermsChanges()
                    }
                    launch {
                        notificationCenter.events.filter { it is NotificationCenter.Event.OpenEntryEditMode }.collect {
                            if (selectedEntry.value != null) {
                                setEntryEditMode(true)
                            }
                        }
                    }
                    launch {
                        notificationCenter.events.filter { it is NotificationCenter.Event.CloseEntryEditMode }.collect {
                            if (selectedEntry.value != null) {
                                setEntryEditMode(false)
                            }
                        }
                    }
                    launch {
                        selectedEntry.collect {
                            entryRepository.setCurrentEntry(it)
                        }
                    }
                    launch {
                        entryEditMode.collect {
                            entryRepository.setEditMode(it)
                        }
                    }
                    launch {
                        notificationCenter.events.filter { it is NotificationCenter.Event.DeleteEntry }.collect {
                            deleteCurrentEntry()
                        }
                    }
                    launch {
                        notificationCenter.events.filter { it is NotificationCenter.Event.CreateEntry }.collect {
                            val termbase = currentTermbase.value ?: return@collect
                            selectedEntry.value = null
                            setEntryEditMode(false)

                            val termbaseId = termbase.id
                            val newEntry = EntryModel(termbaseId = termbaseId)
                            val entryId = entryRepository.create(newEntry)
                            selectedEntry.value = newEntry.copy(id = entryId, new = true)
                            selectedTerm.value = TermModel(entryId = entryId)
                            // needed to wait for the reloadItems to finish in TermDetailViewModel
                            delay(100)
                            setEntryEditMode(true)
                        }
                    }
                }

                termDetailNavigation.activate(TermsComponent.TermDetailConfig)
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun load(termbase: TermbaseModel) {
        currentTermbase.value = termbase
        viewModelScope.launch(dispatcherProvider.io) {
            loadLanguages(termbaseId = termbase.id)
            loadTerms()
        }
    }

    private suspend fun handleCurrentLanguageTermsChanges() {
        notificationCenter.events.filter { it is NotificationCenter.Event.CurrentLanguageTermsEdited }.collect {
            loadTerms()
        }
    }

    override fun changeSourceLanguage(language: LanguageModel) {
        sourceLanguage.update { language }
        viewModelScope.launch(dispatcherProvider.io) {
            loadTerms()
        }
    }

    override fun changeTargetLanguage(language: LanguageModel) {
        targetLanguage.update { language }
    }

    override fun switchLanguages() {
        val oldTarget = targetLanguage.value
        targetLanguage.update { sourceLanguage.value }
        sourceLanguage.update { oldTarget }
        val oldTargets = targetLanguages.value
        targetLanguages.update { sourceLanguages.value }
        sourceLanguages.update { oldTargets }

        viewModelScope.launch(dispatcherProvider.io) {
            loadTerms()
        }
    }

    override fun selectTerm(value: TermModel?) {
        setEntryEditMode(false)
        if (value == null) {
            selectedTerm.update { null }
            selectedEntry.update { null }
            return
        }
        selectedTerm.update { value }
        viewModelScope.launch(dispatcherProvider.io) {
            val entryId = value.entryId
            val entry = entryRepository.getById(entryId)
            selectedEntry.update { entry }
        }
    }

    override fun sendCreateEntryEvent() {
        if (currentTermbase.value == null) {
            return
        }
        val event = NotificationCenter.Event.CreateEntry
        notificationCenter.send(event)
    }

    override fun deleteCurrentEntry() {
        val entry = selectedEntry.value ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            deleteEntryUseCase(entry)
            selectedEntry.update { null }
            terms.updateAndGet { oldList ->
                // selects new term at the beginning
                val newList = oldList.filter { it.entryId != entry.id }
                val idxToSelect = (oldList.indexOfFirst { it.entryId == entry.id }).coerceIn(0, newList.size - 1) - 1
                if (idxToSelect >= 0) {
                    val newTermToSelect = newList[idxToSelect]
                    val newSelectedEntryId = newTermToSelect.entryId
                    selectedEntry.value = entryRepository.getById(newSelectedEntryId)
                    selectedTerm.value = newTermToSelect
                }

                newList
            }
        }
    }

    private suspend fun loadLanguages(termbaseId: Int) {
        val localLanguages =
            languageRepository.getAll(termbaseId).map { it.copy(name = languageNameRepository.getName(it.code)) }
        val source = localLanguages.firstOrNull()
        val target = (localLanguages - source).firstOrNull()
        sourceLanguage.update { source }
        sourceLanguages.update {
            if (target != null) {
                localLanguages - target
            } else {
                localLanguages
            }
        }
        if (source != null) {
            searchCriteria.value = SearchCriterion.getDefault(sourceLang = source.code)
        }
        targetLanguage.update { target }
        targetLanguages.update {
            if (source != null) {
                localLanguages - source
            } else {
                localLanguages
            }
        }
    }

    private fun loadTerms() {
        termsJob?.cancel()
        termsJob = null
        terms.update { emptyList() }
        val lang = sourceLanguage.value?.code ?: return
        val termbase = currentTermbase.value ?: return

        termsJob = viewModelScope.launch(dispatcherProvider.io) {
            entryRepository.observeEntries(termbase.id).distinctUntilChanged(areEquivalent = { lhs, rhs ->
                lhs.all { e -> rhs.contains(e) } && rhs.all { e -> lhs.contains(e) }
            }).onEach {
                val terms = searchTermsUseCase(
                    termbaseId = termbase.id,
                    mainLang = lang,
                    criteria = searchCriteria.value.includingSearch(
                        text = searchText.value.takeIf { e -> e.isNotEmpty() },
                    ),
                )
                val newTerms = terms.sortedBy { e -> e.lemma }
                log.debug("Loaded ${newTerms.count()} terms")
                if (selectedEntry.value?.id !in newTerms.map { it.entryId }) {
                    if (selectedEntry.value?.new == false) {
                        selectedTerm.value = null
                        selectedEntry.value = null
                    }
                } else {
                    selectedTerm.value = newTerms.firstOrNull { it.entryId == selectedEntry.value?.id }
                }
                this@DefaultTermsComponent.terms.update {
                    newTerms
                }
            }.launchIn(this)
        }
    }

    override fun setEntryEditMode(value: Boolean) {
        log.debug("Entering entry edit mode: $value")
        entryEditMode.value = value
    }

    override fun setSearch(value: String) {
        searchText.value = value
    }

    override fun setSearchCriteria(value: List<SearchCriterion>) {
        searchCriteria.value = value

        // reloads after a search
        loadTerms()
    }

    override fun searchTerms() {
        loadTerms()
    }

    override fun openDialog(config: TermsComponent.DialogConfig) {
        viewModelScope.launch(dispatcherProvider.main) {
            dialogNavigation.activate(config)
        }
    }

    override fun closeDialog() {
        viewModelScope.launch(dispatcherProvider.main) {
            dialogNavigation.activate(TermsComponent.DialogConfig.None)
        }
    }
}
