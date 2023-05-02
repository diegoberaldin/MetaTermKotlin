package ui.detail

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import coroutines.CoroutineDispatcherProvider
import data.InputDescriptorModel
import data.LanguageModel
import data.PropertyLevel
import data.PropertyModel
import data.PropertyType
import data.PropertyValueModel
import data.SearchCriterion
import data.TermModel
import files.FileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import log.LogManager
import notification.NotificationCenter
import repository.EntryPropertyValueRepository
import repository.FlagsRepository
import repository.InputDescriptorRepository
import repository.LanguageNameRepository
import repository.LanguagePropertyValueRepository
import repository.PropertyRepository
import repository.TermPropertyValueRepository
import repository.TermRepository
import usecase.DeleteTermUseCase
import java.io.File
import java.util.*

class TermDetailViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val fileManager: FileManager,
    private val termRepository: TermRepository,
    private val flagsRepository: FlagsRepository,
    private val languageNameRepository: LanguageNameRepository,
    private val entryPropertyValueRepository: EntryPropertyValueRepository,
    private val languagePropertyValueRepository: LanguagePropertyValueRepository,
    private val termPropertyValueRepository: TermPropertyValueRepository,
    private val propertyRepository: PropertyRepository,
    private val inputDescriptorRepository: InputDescriptorRepository,
    private val deleteTermUseCase: DeleteTermUseCase,
    private val notificationCenter: NotificationCenter,
    private val log: LogManager,
) : InstanceKeeper.Instance {

    private val items = MutableStateFlow<List<TermDetailItem>>(emptyList())
    private val loading = MutableStateFlow(false)
    private var editMode = false

    private val entryLevelProperties = MutableStateFlow<List<PropertyModel>>(emptyList())
    private val languageLevelProperties = MutableStateFlow<List<PropertyModel>>(emptyList())
    private val termLevelProperties = MutableStateFlow<List<PropertyModel>>(emptyList())

    private var entryId: Int = 0
    private var termbaseId: Int = 0
    private var languages = emptyList<LanguageModel>()
    private var searchCriteria = emptyList<SearchCriterion>()

    private val termsToDelete = mutableListOf<Int>()
    private val propertiesToDelete = mutableListOf<Pair<Int, Int>>()
    private val mutex = Mutex()
    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = combine(items, loading) { items, loading ->
        TermDetailUiState(
            items = items,
            loading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TermDetailUiState(),
    )

    val availablePropertiesUiState = combine(
        entryLevelProperties,
        languageLevelProperties,
        termLevelProperties,
    ) { entryLevelProperties, languageLevelProperties, termLevelProperties ->
        TermDetailAvailablePropertiesUiState(
            entryLevelProperties = entryLevelProperties,
            languageLevelProperties = languageLevelProperties,
            termLevelProperties = termLevelProperties,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TermDetailAvailablePropertiesUiState(),
    )

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            launch {
                notificationCenter.events.filter { it is NotificationCenter.Event.CurrentLanguagesEdited }.collect {
                    mutex.withLock {
                        reloadItems()
                    }
                }
            }
            launch {
                notificationCenter.events.filter { it is NotificationCenter.Event.SaveEntry }.collect {
                    save()
                }
            }
        }
    }

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun setEditMode(value: Boolean) {
        val oldValue = editMode
        if (oldValue != value) {
            viewModelScope.launch(dispatcherProvider.io) {
                mutex.withLock {
                    if (isEmpty()) {
                        editMode = value
                        val inputModel = inputDescriptorRepository.getAll(termbaseId)
                        reloadItems(firstInputDescriptors = inputModel)
                    } else {
                        editMode = value
                        reloadItems()
                    }
                }
            }
        }
    }

    private suspend fun isEmpty(): Boolean {
        val entryProperties = entryPropertyValueRepository.getAll(entryId)
        val languageProperties = languages.flatMap {
            languagePropertyValueRepository.getAll(it.id, entryId)
        }
        val terms = termRepository.getAll(entryId = entryId)
        val termProperties = terms.flatMap {
            termPropertyValueRepository.getAll(it.id)
        }

        val conditions = listOf(
            entryProperties.isEmpty(),
            languageProperties.isEmpty(),
            terms.isEmpty(),
            termProperties.isEmpty(),
        )
        return conditions.all { it }
    }

    fun load(
        entryId: Int?,
        termbaseId: Int?,
        newEntry: Boolean = false,
        searchCriteria: List<SearchCriterion> = emptyList(),
    ) {
        viewModelScope.launch(dispatcherProvider.io) {
            mutex.withLock {
                this@TermDetailViewModel.entryId = entryId ?: 0
                this@TermDetailViewModel.termbaseId = termbaseId ?: 0
                this@TermDetailViewModel.searchCriteria = searchCriteria
                // if the entry is new, wait for the setEditMode(true) afterwards
                if (!newEntry) {
                    reloadItems()
                }
            }
        }
    }

    fun setLanguages(languages: List<LanguageModel>) {
        this.languages = languages
        viewModelScope.launch(dispatcherProvider.io) {
            mutex.withLock {
                reloadItems()
            }
        }
    }

    private suspend fun reloadItems(firstInputDescriptors: List<InputDescriptorModel> = emptyList()) {
        if (loading.value) {
            return
        }

        loading.value = true
        items.update { emptyList() }

        if (entryId == 0 || termbaseId == 0) {
            loading.value = false
            return
        }

        val allDefinitionProperties = propertyRepository.getAll(termbaseId)
        val reloadDeferred = viewModelScope.async {
            val list = mutableListOf<TermDetailItem>()
            // entry title
            list += TermDetailItem.EntryId(entryId)

            // entry level properties
            val values = entryPropertyValueRepository.getAll(entryId)
            for (value in values) {
                val propertyId = value.propertyId
                val property = allDefinitionProperties.firstOrNull { e -> e.id == propertyId }
                if (editMode) {
                    list += TermDetailItem.EditPropertyField(
                        name = property?.name ?: "",
                        value = value.value ?: "",
                        entryId = entryId,
                        propertyId = propertyId,
                        type = property?.type,
                        valueId = value.id,
                        picklistValues = property?.picklistValues?.map { e -> e.value } ?: emptyList(),
                    )
                } else {
                    list += TermDetailItem.Property(
                        id = value.id,
                        name = property?.name ?: "",
                        value = value.value ?: "",
                        entryId = entryId,
                        propertyId = propertyId,
                        type = property?.type,
                    )
                }
            }
            if (editMode) {
                val mandatoryEntryProperties = allDefinitionProperties
                    .filter { e -> e.level == PropertyLevel.ENTRY }
                    .filter { e -> firstInputDescriptors.any { d -> d.propertyId == e.id } }
                for (property in mandatoryEntryProperties) {
                    list += TermDetailItem.EditPropertyField(
                        name = property.name,
                        value = "",
                        entryId = entryId,
                        propertyId = property.id,
                        type = property.type,
                        valueId = 0,
                        picklistValues = property.picklistValues.map { e -> e.value },
                    )
                }
                list += TermDetailItem.CreatePropertyButton(entryId = entryId)
            }

            // language block
            val languages = languages.map { it.copy(name = languageNameRepository.getName(it.code)) }
            for (language in languages) {
                list.addLanguage(
                    entryId = entryId,
                    language = language,
                    allDefinitionProperties = allDefinitionProperties,
                    firstInputDescriptors = firstInputDescriptors,
                )
            }

            list
        }

        val newList = reloadDeferred.await()
        items.value = newList
        loading.value = false
    }

    fun calculateAvailableEntryProperties() {
        viewModelScope.launch(dispatcherProvider.io) {
            val usedPropertyIds = items.value.mapNotNull {
                when {
                    it is TermDetailItem.Property && it.entryId == entryId -> it.propertyId
                    it is TermDetailItem.EditPropertyField && it.entryId == entryId -> it.propertyId
                    else -> null
                }
            }
            val properties = propertyRepository.getAll(termbaseId).filter { it.level == PropertyLevel.ENTRY }
            val availableProperties = properties.filter { it.id !in usedPropertyIds }
            entryLevelProperties.value = availableProperties
        }
    }

    fun calculateAvailableLanguageProperties(index: Int) {
        viewModelScope.launch(dispatcherProvider.io) {
            val usedPropertyIds = mutableListOf<Int>()
            if (index > 0) {
                for (i in (0 until index).reversed()) {
                    val item = items.value[i]
                    if (item is TermDetailItem.EditPropertyField && item.languageId != null) {
                        usedPropertyIds += item.propertyId
                    } else {
                        break
                    }
                }
            }
            val properties = propertyRepository.getAll(termbaseId).filter { it.level == PropertyLevel.LANGUAGE }
            val availableProperties = properties.filter { it.id !in usedPropertyIds }
            languageLevelProperties.value = availableProperties
        }
    }

    fun calculateAvailableTermProperties(index: Int) {
        viewModelScope.launch(dispatcherProvider.io) {
            val usedPropertyIds = mutableListOf<Int>()
            if (index > 0) {
                for (i in (0 until index).reversed()) {
                    val item = items.value[i]
                    if (item is TermDetailItem.EditPropertyField && item.termId != null) {
                        usedPropertyIds += item.propertyId
                    } else {
                        break
                    }
                }
            }
            val properties = propertyRepository.getAll(termbaseId).filter { it.level == PropertyLevel.TERM }
            val availableProperties = properties.filter { it.id !in usedPropertyIds }
            termLevelProperties.value = availableProperties
        }
    }

    fun save() {
        if (loading.value) {
            return
        }
        log.debug("Entry edit START")
        loading.value = true
        viewModelScope.launch {
            val termIdsToReplace = mutableMapOf<Int, Int>()
            val propertyIdsToReplace = mutableMapOf<Int, Int>()
            var lastTermId: Int? = null

            for (index in items.value.indices) {
                when (val item = items.value[index]) {
                    is TermDetailItem.EditTermField -> {
                        if (item.lemma.isEmpty()) {
                            lastTermId = null
                            if (item.termId > 0) {
                                termsToDelete += item.termId
                            }
                        } else {
                            lastTermId = saveTerm(
                                lemma = item.lemma,
                                termId = item.termId,
                                langCode = item.langCode,
                            )
                            if (item.termId == 0 && lastTermId > 0) {
                                termIdsToReplace[index] = lastTermId
                            }
                        }
                    }

                    is TermDetailItem.EditPropertyField -> {
                        val hasAtLeastOneReference = listOf(
                            item.termId ?: 0,
                            lastTermId ?: 0,
                            item.entryId ?: 0,
                            item.languageId ?: 0,
                        ).any { it > 0 }
                        if (item.value.isEmpty() || !hasAtLeastOneReference) {
                            if (item.valueId > 0 && item.propertyId > 0) {
                                propertiesToDelete += item.propertyId to item.valueId
                            }
                        } else {
                            val isReferenceValid = listOf(
                                item.entryId ?: 0,
                                item.languageId ?: 0,
                                item.termId ?: 0,
                                lastTermId ?: 0,
                            ).any { it > 0 }
                            if (isReferenceValid) {
                                val lastPropertyId = saveProperty(
                                    valueId = item.valueId,
                                    propertyId = item.propertyId,
                                    languageId = item.languageId,
                                    entryId = item.entryId,
                                    termId = if (item.termId != null) {
                                        lastTermId ?: 0
                                    } else {
                                        null
                                    },
                                    value = item.value,
                                )

                                if (item.valueId == 0 && lastPropertyId > 0) {
                                    propertyIdsToReplace[index] = lastPropertyId
                                }
                            }
                        }
                    }

                    else -> Unit
                }
            }

            // deletes stale data
            for (termId in termsToDelete) {
                deleteTerm(termId)
            }
            termsToDelete.clear()
            for ((propertyId, valueId) in propertiesToDelete) {
                deleteProperty(valueId = valueId, propertyId = propertyId)
            }
            propertiesToDelete.clear()

            // saves newly created IDs to correct indices
            items.updateAndGet { oldList ->
                val newList = mutableListOf<TermDetailItem>()
                for (i in oldList.indices) {
                    val termId = termIdsToReplace[i]
                    val valueId = propertyIdsToReplace[i]
                    when {
                        termId != null -> {
                            newList += (oldList[i] as TermDetailItem.EditTermField).copy(termId = termId)
                        }

                        valueId != null -> {
                            newList += (oldList[i] as TermDetailItem.EditPropertyField).copy(valueId = valueId)
                        }

                        else -> {
                            newList += oldList[i]
                        }
                    }
                }
                newList
            }

            loading.value = false
            notificationCenter.send(NotificationCenter.Event.CurrentLanguageTermsEdited)
            log.debug("Entry edit END")
        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // Language management
    // /////////////////////////////////////////////////////////////////////////

    private suspend fun MutableList<TermDetailItem>.addLanguage(
        entryId: Int,
        language: LanguageModel,
        allDefinitionProperties: List<PropertyModel> = emptyList(),
        firstInputDescriptors: List<InputDescriptorModel> = emptyList(),
    ) {
        // language title
        this += TermDetailItem.LanguageTitle(
            language = language,
            flag = flagsRepository.getFlag(language.code),
        )

        // language level properties
        val values = languagePropertyValueRepository.getAll(languageId = language.id, entryId = entryId)
        for (value in values) {
            val propertyId = value.propertyId
            val property = allDefinitionProperties.firstOrNull { e -> e.id == propertyId }
            if (editMode) {
                this += TermDetailItem.EditPropertyField(
                    name = property?.name ?: "",
                    value = value.value ?: "",
                    languageId = language.id,
                    propertyId = propertyId,
                    type = property?.type,
                    valueId = value.id,
                    picklistValues = property?.picklistValues?.map { e -> e.value } ?: emptyList(),
                )
            } else {
                this += TermDetailItem.Property(
                    id = value.id,
                    name = property?.name ?: "",
                    value = value.value ?: "",
                    languageId = language.id,
                    propertyId = propertyId,
                    type = property?.type,
                )
            }
        }
        if (editMode) {
            val mandatoryLanguageProperties = allDefinitionProperties
                .filter { e -> e.level == PropertyLevel.LANGUAGE }
                .filter { e -> firstInputDescriptors.any { d -> d.propertyId == e.id && d.lang == language.code } }
            for (property in mandatoryLanguageProperties) {
                this += TermDetailItem.EditPropertyField(
                    name = property.name,
                    value = "",
                    languageId = language.id,
                    propertyId = property.id,
                    type = property.type,
                    valueId = 0,
                    picklistValues = property.picklistValues.map { e -> e.value },
                )
            }
            this += TermDetailItem.CreatePropertyButton(languageId = language.id)
        }

        // terms
        val terms = termRepository.getAll(
            termbaseId = termbaseId,
            entryId = entryId,
            mainLang = language.code,
            criteria = searchCriteria
        )
        for (term in terms) {
            addTerm(
                term = term,
                allDefinitionProperties = allDefinitionProperties,
            )
        }

        if (editMode && firstInputDescriptors.isNotEmpty()) {
            val shouldTermInputBeAdded = firstInputDescriptors.any { e -> e.lemma == true && e.lang == language.code }
            if (shouldTermInputBeAdded) {
                this += TermDetailItem.EditTermField(
                    lemma = "",
                    termId = 0,
                    langCode = language.code,
                )
            }
            val mandatoryTermProperties = allDefinitionProperties
                .filter { e -> e.level == PropertyLevel.TERM }
                .filter { e -> firstInputDescriptors.any { d -> d.propertyId == e.id && d.lang == language.code } }
            for (property in mandatoryTermProperties) {
                this += TermDetailItem.EditPropertyField(
                    name = property.name,
                    value = "",
                    termId = 0,
                    propertyId = property.id,
                    type = property.type,
                    valueId = 0,
                    picklistValues = property.picklistValues.map { e -> e.value },
                )
            }

            this += TermDetailItem.CreatePropertyButton(termId = 0)

            this += TermDetailItem.CreateTermButton(language.code)
        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // Term management
    // /////////////////////////////////////////////////////////////////////////

    private suspend fun MutableList<TermDetailItem>.addTerm(
        term: TermModel,
        allDefinitionProperties: List<PropertyModel> = emptyList(),
    ) {
        // term title
        if (editMode) {
            this += TermDetailItem.EditTermField(
                lemma = term.lemma,
                termId = term.id,
                langCode = term.lang,
            )
        } else {
            this += TermDetailItem.Term(lemma = term.lemma, termId = term.id)
        }

        // term level properties
        val values = termPropertyValueRepository.getAll(termId = term.id)
        for (value in values) {
            val propertyId = value.propertyId
            val property = allDefinitionProperties.firstOrNull { e -> e.id == propertyId }
            if (editMode) {
                this += TermDetailItem.EditPropertyField(
                    name = property?.name ?: "",
                    value = value.value ?: "",
                    termId = term.id,
                    propertyId = propertyId,
                    type = property?.type,
                    valueId = value.id,
                    picklistValues = property?.picklistValues?.map { e -> e.value } ?: emptyList(),
                )
            } else {
                this += TermDetailItem.Property(
                    id = value.id,
                    name = property?.name ?: "",
                    value = value.value ?: "",
                    termId = term.id,
                    propertyId = propertyId,
                    type = property?.type,
                )
            }
        }

        if (editMode) {
            this += TermDetailItem.CreatePropertyButton(termId = term.id)
        }
    }

    fun startInsertTermAt(index: Int, langCode: String) {
        items.getAndUpdate {
            val newItems = mutableListOf<TermDetailItem>()
            for (i in it.indices) {
                if (i != index) {
                    newItems += it[i]
                } else {
                    newItems += TermDetailItem.EditTermField(lemma = "", termId = 0, langCode = langCode)
                    newItems += TermDetailItem.CreatePropertyButton(termId = -1)
                    newItems += TermDetailItem.CreateTermButton(langCode)
                }
            }
            newItems
        }
    }

    fun markTermFormDeletion(index: Int) {
        items.getAndUpdate {
            val newItems = mutableListOf<TermDetailItem>()
            var deletingPropertiesAfterTerm: Boolean
            for (i in it.indices) {
                if (i < index) {
                    newItems += it[i]
                } else if (i == index) {
                    val item = it[i] as TermDetailItem.EditTermField
                    if (item.termId > 0) {
                        termsToDelete.add(item.termId)
                    }
                } else {
                    val item = it[i]
                    deletingPropertiesAfterTerm = item is TermDetailItem.EditPropertyField
                    if (deletingPropertiesAfterTerm) {
                        val propertyItem = item as TermDetailItem.EditPropertyField
                        // item is skipped and property marked for deletion
                        if (propertyItem.valueId > 0) {
                            val pair = propertyItem.propertyId to propertyItem.valueId
                            propertiesToDelete.add(pair)
                        }
                    } else {
                        if (newItems.lastOrNull() !is TermDetailItem.CreatePropertyButton || item !is TermDetailItem.CreatePropertyButton) {
                            newItems += item
                        }
                    }
                }
            }
            newItems
        }
    }

    fun setTermLemma(index: Int, lemma: String) {
        items.getAndUpdate {
            val newItems = mutableListOf<TermDetailItem>()
            for (i in it.indices) {
                if (i != index) {
                    newItems += it[i]
                } else {
                    val item = it[i] as TermDetailItem.EditTermField
                    newItems += item.copy(lemma = lemma)
                }
            }
            newItems
        }
    }

    private suspend fun saveTerm(lemma: String, termId: Int, langCode: String): Int {
        val oldTerm = termRepository.getById(termId)
        val term = if (oldTerm == null) {
            TermModel(
                lemma = lemma,
                entryId = entryId,
                lang = langCode,
            )
        } else {
            TermModel(
                id = termId,
                lemma = lemma,
                entryId = oldTerm.entryId,
                lang = oldTerm.lang,
            )
        }
        return if (oldTerm == null) {
            termRepository.create(term)
        } else {
            termRepository.update(term)
            termId
        }
    }

    private suspend fun deleteTerm(termId: Int) {
        deleteTermUseCase(termId)
        reloadItems()
    }

    // /////////////////////////////////////////////////////////////////////////
    // Property management
    // /////////////////////////////////////////////////////////////////////////

    fun startInsertPropertyAt(
        index: Int,
        valueId: Int,
        propertyId: Int,
        entryId: Int?,
        languageId: Int?,
        termId: Int?,
        new: Boolean = false,
    ) {
        viewModelScope.launch(dispatcherProvider.io) {
            val property = propertyRepository.getById(propertyId)
            items.getAndUpdate {
                val newItems = mutableListOf<TermDetailItem>()
                for (i in it.indices) {
                    if (i != index) {
                        newItems += it[i]
                    } else {
                        val value = when {
                            entryId != null -> {
                                val value = entryPropertyValueRepository.getById(valueId)
                                value?.value
                            }

                            languageId != null -> {
                                val value = languagePropertyValueRepository.getById(valueId)
                                value?.value
                            }

                            termId != null -> {
                                val value = termPropertyValueRepository.getById(valueId)
                                value?.value
                            }

                            else -> null
                        }
                        newItems += TermDetailItem.EditPropertyField(
                            type = property?.type,
                            name = property?.name ?: "",
                            value = value ?: "",
                            propertyId = propertyId,
                            valueId = valueId,
                            entryId = entryId,
                            languageId = languageId,
                            termId = termId,
                            picklistValues = property?.picklistValues?.map { v -> v.value } ?: emptyList(),
                        )
                        if (new) {
                            newItems += TermDetailItem.CreatePropertyButton(
                                entryId = entryId,
                                languageId = languageId,
                                termId = termId,
                            )
                        }
                    }
                }
                newItems
            }
        }
    }

    fun markPropertyForDeletion(index: Int) {
        items.getAndUpdate {
            val newItems = mutableListOf<TermDetailItem>()
            for (i in it.indices) {
                if (i != index) {
                    newItems += it[i]
                } else {
                    val item = it[i] as TermDetailItem.EditPropertyField
                    if (item.valueId > 0) {
                        val pair = item.propertyId to item.valueId
                        propertiesToDelete.add(pair)
                    }
                }
            }
            newItems
        }
    }

    fun setPropertyValue(value: String, index: Int) {
        items.getAndUpdate {
            val newItems = mutableListOf<TermDetailItem>()
            for (i in it.indices) {
                if (i != index) {
                    newItems += it[i]
                } else {
                    val item = it[i] as TermDetailItem.EditPropertyField
                    newItems += item.copy(value = value)
                }
            }
            newItems
        }
    }

    private fun copyImageToNewFile(path: String): String? {
        return try {
            val file = File(path)
            val dir = File(fileManager.getFilePath(termbaseId.toString()))
            dir.mkdirs()
            val newFile = File(dir, UUID.randomUUID().toString())
            newFile.createNewFile()
            file.copyTo(newFile, overwrite = true)
            newFile.absolutePath
        } catch (e: Throwable) {
            e.printStackTrace()
            path
        }
    }

    private suspend fun saveProperty(
        valueId: Int,
        propertyId: Int,
        entryId: Int?,
        languageId: Int?,
        termId: Int?,
        value: String,
    ): Int {
        val res: Int
        val property = propertyRepository.getById(propertyId)
        when {
            entryId != null -> {
                val propertyValue = entryPropertyValueRepository.getById(valueId)
                val valueToSave = if (property?.type == PropertyType.IMAGE && value != propertyValue?.value) {
                    copyImageToNewFile(value)
                } else {
                    value
                }
                if (propertyValue != null) {
                    val newValue = propertyValue.copy(value = valueToSave)
                    entryPropertyValueRepository.update(newValue)
                    if (property?.type == PropertyType.IMAGE) {
                        val path = propertyValue.value
                        if (!path.isNullOrEmpty()) {
                            val oldFile = File(path)
                            oldFile.delete()
                        }
                    }
                    res = propertyValue.id
                } else {
                    val newValue = PropertyValueModel(
                        propertyId = propertyId,
                        value = valueToSave,
                    )
                    res = entryPropertyValueRepository.create(
                        model = newValue,
                        entryId = entryId,
                    )
                }
            }

            languageId != null -> {
                val propertyValue = languagePropertyValueRepository.getById(valueId)
                val valueToSave = if (property?.type == PropertyType.IMAGE && value != propertyValue?.value) {
                    copyImageToNewFile(value)
                } else {
                    value
                }
                if (propertyValue != null) {
                    val newValue = propertyValue.copy(value = valueToSave)
                    languagePropertyValueRepository.update(newValue)
                    if (property?.type == PropertyType.IMAGE) {
                        val path = propertyValue.value
                        if (!path.isNullOrEmpty()) {
                            val oldFile = File(path)
                            oldFile.delete()
                        }
                    }
                    res = propertyValue.id
                } else {
                    val newValue = PropertyValueModel(
                        value = valueToSave,
                        propertyId = propertyId,
                    )
                    res = languagePropertyValueRepository.create(
                        model = newValue,
                        languageId = languageId,
                        entryId = this@TermDetailViewModel.entryId,
                    )
                }
            }

            termId != null -> {
                val propertyValue = termPropertyValueRepository.getById(valueId)
                val valueToSave = if (property?.type == PropertyType.IMAGE && value != propertyValue?.value) {
                    copyImageToNewFile(value)
                } else {
                    value
                }
                if (propertyValue != null) {
                    val newValue = propertyValue.copy(value = valueToSave)
                    termPropertyValueRepository.update(newValue)
                    if (property?.type == PropertyType.IMAGE) {
                        val path = propertyValue.value
                        if (!path.isNullOrEmpty()) {
                            val oldFile = File(path)
                            oldFile.delete()
                        }
                    }
                    res = propertyValue.id
                } else {
                    val newValue = PropertyValueModel(
                        value = valueToSave,
                        propertyId = propertyId,
                    )
                    res = termPropertyValueRepository.create(
                        model = newValue,
                        termId = termId,
                    )
                }
            }

            else -> {
                res = 0
            }
        }
        return res
    }

    private suspend fun deleteProperty(valueId: Int, propertyId: Int) {
        val property = propertyRepository.getById(propertyId)
        when (property?.level) {
            PropertyLevel.ENTRY -> {
                val propertyValue = entryPropertyValueRepository.getById(valueId)
                if (propertyValue != null) {
                    entryPropertyValueRepository.delete(propertyValue)
                    if (property.type == PropertyType.IMAGE) {
                        propertyValue.value?.let { File(it) }?.delete()
                    }
                }
            }

            PropertyLevel.LANGUAGE -> {
                val propertyValue = languagePropertyValueRepository.getById(valueId)
                if (propertyValue != null) {
                    languagePropertyValueRepository.delete(propertyValue)
                    if (property.type == PropertyType.IMAGE) {
                        propertyValue.value?.let { File(it) }?.delete()
                    }
                }
            }

            PropertyLevel.TERM -> {
                val propertyValue = termPropertyValueRepository.getById(valueId)
                if (propertyValue != null) {
                    termPropertyValueRepository.delete(propertyValue)
                    if (property.type == PropertyType.IMAGE) {
                        propertyValue.value?.let { File(it) }?.delete()
                    }
                }
            }

            else -> Unit
        }
    }
}
