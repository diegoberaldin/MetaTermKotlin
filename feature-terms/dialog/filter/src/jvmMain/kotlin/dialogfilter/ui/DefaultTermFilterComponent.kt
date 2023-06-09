package dialogfilter.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import localized
import repo.FlagsRepository
import repo.LanguageNameRepository
import repo.LanguageRepository
import repo.PropertyRepository
import kotlin.coroutines.CoroutineContext

internal class DefaultTermFilterComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val propertyRepository: PropertyRepository,
    private val languageRepository: LanguageRepository,
    private val flagsRepository: FlagsRepository,
    private val languageNameRepository: LanguageNameRepository,
) : TermFilterComponent, ComponentContext by componentContext {

    private var termbaseId: Int = 0
    private var sourceLanguage: LanguageModel? = null
    private val filterableItems = MutableStateFlow<List<FilterableItem>>(emptyList())
    private val selectedItem = MutableStateFlow<FilterableItem?>(null)
    private val currentName = MutableStateFlow("")
    private val availableMatchTypes = MutableStateFlow(
        listOf(
            MatchType.FUZZY,
            MatchType.EXACT,
            MatchType.SEARCHABLE,
        )
    )
    private val currentMatchType = MutableStateFlow<MatchType?>(null)
    private val currentValue = MutableStateFlow("")
    private val availableValues = MutableStateFlow<List<String>?>(null)
    private val _done = MutableSharedFlow<List<SearchCriterion>>()
    override val done = _done.asSharedFlow()
    private val configurations = MutableStateFlow(mapOf<FilterableItem, FilterConfiguration>())
    private lateinit var viewModelScope: CoroutineScope

    override lateinit var propertiesUiState: StateFlow<TermFilterPropertiesUiState>
    override lateinit var uiState: StateFlow<TermFilterUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
                    currentName,
                    availableMatchTypes,
                    currentMatchType,
                    currentValue,
                    availableValues,
                ) { currentName, availableMatchTypes, currentMatchType, currentValue, availableValues ->
                    TermFilterUiState(
                        currentName = currentName,
                        availableMatchTypes = availableMatchTypes,
                        currentMatchType = currentMatchType,
                        currentValue = currentValue,
                        availableValues = availableValues,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = TermFilterUiState(),
                )
                propertiesUiState = combine(
                    filterableItems, selectedItem, configurations
                ) { items, selectedItem, configurations ->
                    TermFilterPropertiesUiState(
                        items = items,
                        selectedItem = selectedItem,
                        configurations = configurations
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = TermFilterPropertiesUiState()
                )
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    private suspend fun loadCriteria(criteria: List<SearchCriterion>) {
        val properties = propertyRepository.getAll(termbaseId)

        fun SearchCriterion.MatchDescriptor.toKey(): FilterableItem? {
            val lang = this.lang
            val isLemma = lemma == true
            return when {
                isLemma && lang != null -> {
                    FilterableItem.Lemma(lang = lang)
                }

                propertyId != null -> {
                    FilterableItem.Property(
                        property = properties.first { e -> e.id == propertyId }, lang = lang
                    )
                }

                else -> {
                    null
                }
            }
        }

        // load configuration
        for (criterion in criteria) {
            when (criterion) {
                is SearchCriterion.ExactMatch -> {
                    if (criterion.matching.isEmpty()) {
                        continue
                    } else if (criterion.matching.size == 1) {
                        val key = criterion.matching.firstOrNull()?.toKey()
                        if (key != null) {
                            configurations.updateAndGet {
                                it.toMutableMap().apply {
                                    this[key] = FilterConfiguration(matchType = MatchType.EXACT, value = criterion.text)
                                }
                            }
                        }
                    }
                }

                is SearchCriterion.FuzzyMatch -> {
                    if (criterion.matching.isEmpty()) {
                        continue
                    } else if (criterion.matching.size == 1) {
                        val key = criterion.matching.firstOrNull()?.toKey()
                        if (key != null) {
                            configurations.updateAndGet {
                                it.toMutableMap().apply {
                                    this[key] = FilterConfiguration(
                                        matchType = if (criterion.text.isEmpty()) MatchType.SEARCHABLE else MatchType.FUZZY,
                                        value = criterion.text
                                    )
                                }
                            }
                        }
                    } else {
                        // searchable fields
                        for (item in criterion.matching) {
                            val key = item.toKey()
                            if (key != null) {
                                configurations.updateAndGet {
                                    it.toMutableMap().apply {
                                        this[key] = FilterConfiguration(matchType = MatchType.SEARCHABLE)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadPropertyTree() {
        val properties = propertyRepository.getAll(termbaseId)
        val languages = languageRepository.getAll(termbaseId)
        filterableItems.update {
            buildList {
                this += FilterableItem.SectionHeader(level = PropertyLevel.ENTRY)
                val entryProperties = properties.filter { it.level == PropertyLevel.ENTRY }.sortedBy { it.id }
                for (property in entryProperties) {
                    this += FilterableItem.Property(property = property)
                }
                this += FilterableItem.SectionHeader(level = PropertyLevel.LANGUAGE)
                for (language in languages) {
                    val code = language.code
                    val name = "${flagsRepository.getFlag(code)} ${languageNameRepository.getName(code)}"
                    this += FilterableItem.LanguageHeader(name = name, lang = code)
                    val languageProperties = properties.filter { it.level == PropertyLevel.LANGUAGE }.sortedBy { it.id }
                    for (property in languageProperties) {
                        this += FilterableItem.Property(
                            property = property,
                            lang = code,
                        )
                    }

                    this += FilterableItem.SectionHeader(level = PropertyLevel.TERM, lang = code)
                    this += FilterableItem.Lemma(lang = code)

                    val termProperties = properties.filter { it.level == PropertyLevel.TERM }.sortedBy { it.id }
                    for (property in termProperties) {
                        this += FilterableItem.Property(
                            property = property,
                            lang = code,
                        )
                    }
                }
            }
        }
    }

    override fun loadInitial(termbase: TermbaseModel, criteria: List<SearchCriterion>, sourceLanguage: LanguageModel?) {
        termbaseId = termbase.id
        this.sourceLanguage = sourceLanguage

        viewModelScope.launch(dispatcherProvider.io) {
            loadCriteria(criteria)
            loadPropertyTree()
        }
    }

    override fun toggleSelection(item: FilterableItem) {
        selectedItem.updateAndGet { old ->
            if (old != null) {
                val configuration = FilterConfiguration(currentMatchType.value, currentValue.value)
                if (!configuration.isEmpty) {
                    configurations.updateAndGet {
                        it.toMutableMap().apply {
                            this[old] = configuration
                        }
                    }
                }
            }
            if (old == item) {
                onSelect(null)
                null
            } else {
                onSelect(item)
                item
            }
        }
    }

    private fun onSelect(item: FilterableItem?) {
        when (item) {
            is FilterableItem.Lemma -> {
                currentName.value = buildString {
                    append("term_detail_title_lemma".localized())
                    append(" (")
                    append(languageNameRepository.getName(item.lang))
                    append(")")
                }
                availableMatchTypes.value = listOf(
                    MatchType.FUZZY,
                    MatchType.EXACT,
                    MatchType.SEARCHABLE,
                )
                availableValues.value = null
                val configuration = configurations.value.getOrDefault(item, FilterConfiguration())
                currentMatchType.value = configuration.matchType
                currentValue.value = configuration.value
            }

            is FilterableItem.Property -> {
                val property = item.property
                currentName.value = buildString {
                    append(property.name)
                    if (item.lang != null) {
                        append(" (")
                        append(languageNameRepository.getName(item.lang))
                        append(")")
                    }
                }
                when (property.type) {
                    PropertyType.IMAGE -> {
                        availableMatchTypes.value = emptyList()
                        currentMatchType.value = null
                        availableValues.value = null
                    }

                    PropertyType.PICKLIST -> {
                        availableMatchTypes.value = listOf(MatchType.EXACT)
                        currentMatchType.value = MatchType.EXACT
                        availableValues.value = property.picklistValues.map { it.value }
                        val configuration = configurations.value.getOrDefault(item, FilterConfiguration())
                        currentValue.value = configuration.value
                    }

                    else -> {
                        availableMatchTypes.value = listOf(
                            MatchType.FUZZY,
                            MatchType.EXACT,
                            MatchType.SEARCHABLE,
                        )
                        availableValues.value = null
                        val configuration = configurations.value.getOrDefault(item, FilterConfiguration())
                        currentMatchType.value = configuration.matchType
                        currentValue.value = configuration.value
                    }
                }
            }

            else -> {
                currentName.value = ""
                currentValue.value = ""
                currentMatchType.value = null
                availableValues.value = null
            }
        }
    }

    override fun setCurrentMatchType(value: MatchType) {
        currentMatchType.value = value
    }

    override fun setCurrentValue(value: String) {
        currentValue.value = value
    }

    override fun clearCurrent() {
        currentValue.value = ""
        currentMatchType.value = null
        selectedItem.value?.also { item ->
            configurations.updateAndGet {
                it.toMutableMap().apply {
                    remove(item)
                }
            }
        }
        selectedItem.value?.also {
            onSelect(it)
        }
    }

    override fun clearAll() {
        currentValue.value = ""
        currentMatchType.value = null
        configurations.value = mapOf()

        viewModelScope.launch(dispatcherProvider.io) {
            val sourceLang = sourceLanguage?.code
            if (sourceLang != null) {
                val default = SearchCriterion.getDefault(sourceLang = sourceLang)
                loadCriteria(criteria = default)
            }
            selectedItem.value?.also {
                onSelect(it)
            }

            loadPropertyTree()
        }
    }

    override fun submit() {
        val old = selectedItem.value
        if (old != null) {
            val configuration = FilterConfiguration(currentMatchType.value, currentValue.value)
            if (!configuration.isEmpty) {
                configurations.updateAndGet {
                    it.toMutableMap().apply {
                        this[old] = configuration
                    }
                }
            }
        }

        val result = mutableListOf<SearchCriterion>()

        val searchableMatchDescriptors = mutableListOf<SearchCriterion.MatchDescriptor>()
        for ((key, value) in configurations.value) {
            val matchType = value.matchType
            val searchText = value.value
            val isLemma = key is FilterableItem.Lemma
            val propertyId = (key as? FilterableItem.Property)?.property?.id
            val lang = when (key) {
                is FilterableItem.Lemma -> key.lang
                is FilterableItem.Property -> key.lang
                else -> null
            }
            val criterionMatchDescriptor = SearchCriterion.MatchDescriptor(
                propertyId = propertyId,
                lemma = isLemma,
                lang = lang,
            )
            val filter = when (matchType) {
                MatchType.FUZZY -> SearchCriterion.FuzzyMatch(
                    text = searchText, listOf(element = criterionMatchDescriptor)
                )

                MatchType.EXACT -> SearchCriterion.ExactMatch(
                    text = searchText, listOf(element = criterionMatchDescriptor)
                )

                MatchType.SEARCHABLE -> {
                    searchableMatchDescriptors += criterionMatchDescriptor
                    null
                }


                else -> null
            }
            if (filter != null) {
                result += filter
            }
        }

        // adds all the searchable field in an empty fuzzy match criterion
        if (searchableMatchDescriptors.isNotEmpty()) {
            result += SearchCriterion.FuzzyMatch(text = "", matching = searchableMatchDescriptors)
        }

        viewModelScope.launch(dispatcherProvider.io) {
            _done.emit(result)
        }
    }
}
