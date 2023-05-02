package ui.dialog.create.stepthree

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import coroutines.CoroutineDispatcherProvider
import data.InputDescriptorModel
import data.LanguageModel
import data.PropertyLevel
import data.PropertyModel
import data.TermbaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import repository.FlagsRepository
import repository.InputDescriptorRepository
import repository.LanguageNameRepository
import repository.LanguageRepository
import repository.PropertyRepository

class CreateTermbaseWizardStepThreeViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val propertyRepository: PropertyRepository,
    private val languageRepository: LanguageRepository,
    private val languageNameRepository: LanguageNameRepository,
    private val flagsRepository: FlagsRepository,
    private val inputDescriptorRepository: InputDescriptorRepository,
) : InstanceKeeper.Instance {

    private val items = MutableStateFlow<List<CreateTermbaseWizardStepThreeItem>>(emptyList())
    private val _done = MutableSharedFlow<List<InputDescriptorModel>>()
    val done = _done.asSharedFlow()
    private var termbaseId: Int = 0
    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = combine(items) {
        CreateTermbaseWizardStepThreeUiState(
            items = it.first(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateTermbaseWizardStepThreeUiState(),
    )

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun reset() {
        items.value = emptyList()
        termbaseId = 0
    }

    fun loadInitial(termbase: TermbaseModel) {
        termbaseId = termbase.id
        viewModelScope.launch(dispatcherProvider.io) {
            val properties = propertyRepository.getAll(termbaseId)
            val languages = languageRepository.getAll(termbaseId)
            val existingInputModel = inputDescriptorRepository.getAll(termbaseId)
            loadItems(
                properties = properties,
                languages = languages,
                oldInputModel = existingInputModel,
            )
        }
    }

    fun loadItems(
        properties: List<PropertyModel>,
        languages: List<LanguageModel>,
        oldInputModel: List<InputDescriptorModel>,
    ) {
        items.update {
            buildList {
                this += CreateTermbaseWizardStepThreeItem.SectionHeader(level = PropertyLevel.ENTRY)
                val entryProperties = properties.filter { it.level == PropertyLevel.ENTRY }.sortedBy { it.id }
                for (property in entryProperties) {
                    val preselected = oldInputModel.firstOrNull { e -> e.propertyId == property.id } != null
                    this += CreateTermbaseWizardStepThreeItem.Property(property = property, selected = preselected)
                }
                this += CreateTermbaseWizardStepThreeItem.SectionHeader(level = PropertyLevel.LANGUAGE)
                for (language in languages) {
                    val code = language.code
                    val name = "${flagsRepository.getFlag(code)} ${languageNameRepository.getName(code)}"
                    this += CreateTermbaseWizardStepThreeItem.LanguageHeader(name = name, lang = code)
                    val languageProperties = properties.filter { it.level == PropertyLevel.LANGUAGE }.sortedBy { it.id }
                    for (property in languageProperties) {
                        val preselected =
                            oldInputModel.firstOrNull { e -> e.propertyId == property.id && e.lang == code } != null
                        this += CreateTermbaseWizardStepThreeItem.Property(
                            property = property,
                            lang = code,
                            selected = preselected,
                        )
                    }

                    this += CreateTermbaseWizardStepThreeItem.SectionHeader(level = PropertyLevel.TERM, lang = code)
                    val preselectedLemma = oldInputModel.firstOrNull { e -> e.lemma == true && e.lang == code } != null
                    this += CreateTermbaseWizardStepThreeItem.Lemma(lang = code, selected = preselectedLemma)
                    val termProperties = properties.filter { it.level == PropertyLevel.TERM }.sortedBy { it.id }
                    for (property in termProperties) {
                        val preselected =
                            oldInputModel.firstOrNull { e -> e.propertyId == property.id && e.lang == code } != null
                        this += CreateTermbaseWizardStepThreeItem.Property(
                            property = property,
                            lang = code,
                            selected = preselected,
                        )
                    }
                }
            }
        }
    }

    fun toggleSelection(item: CreateTermbaseWizardStepThreeItem, selected: Boolean) {
        items.updateAndGet {
            val updatedSelection = it.map { e ->
                when (e) {
                    item -> {
                        when (e) {
                            is CreateTermbaseWizardStepThreeItem.Property -> e.copy(selected = selected)
                            is CreateTermbaseWizardStepThreeItem.Lemma -> e.copy(selected = selected)
                            else -> e
                        }
                    }

                    else -> e
                }
            }
            val lemmataToSelect = updatedSelection.filterIndexed { i, e ->
                if (e is CreateTermbaseWizardStepThreeItem.Lemma) {
                    val lang = e.lang
                    val termProperties = mutableListOf<CreateTermbaseWizardStepThreeItem.Property>()
                    for (index in i + 1 until updatedSelection.size) {
                        val nextItem = updatedSelection[index]
                        if (nextItem is CreateTermbaseWizardStepThreeItem.Property && nextItem.lang == lang) {
                            termProperties += nextItem
                        } else {
                            break
                        }
                    }
                    e.selected || termProperties.any { p -> p.selected }
                } else {
                    false
                }
            }
            val newList = updatedSelection.map { e ->
                if (e is CreateTermbaseWizardStepThreeItem.Lemma && e in lemmataToSelect) {
                    e.copy(selected = true)
                } else {
                    e
                }
            }
            newList
        }
    }

    fun submit() {
        viewModelScope.launch(dispatcherProvider.io) {
            val existingInputModel = inputDescriptorRepository.getAll(termbaseId)

            val result = items.value.mapNotNull {
                val inputDescriptorModel = when {
                    it is CreateTermbaseWizardStepThreeItem.Property && it.selected -> {
                        val existingId = existingInputModel.firstOrNull { e ->
                            e.propertyId == it.property.id && e.lang == it.lang
                        }?.id ?: 0
                        InputDescriptorModel(
                            propertyId = it.property.id,
                            lang = it.lang,
                            termbaseId = termbaseId,
                            id = existingId,
                        )
                    }

                    it is CreateTermbaseWizardStepThreeItem.Lemma && it.selected -> {
                        val existingId = existingInputModel.firstOrNull { e ->
                            e.lemma == true && e.lang == it.lang
                        }?.id ?: 0
                        InputDescriptorModel(
                            lemma = true,
                            lang = it.lang,
                            termbaseId = termbaseId,
                            id = existingId,
                        )
                    }

                    else -> {
                        null
                    }
                }
                inputDescriptorModel
            }
            _done.emit(result)
        }
    }
}
