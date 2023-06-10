package dialogcreate.stepthree.ui

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
import repo.InputDescriptorRepository
import repo.LanguageRepository
import repo.PropertyRepository
import usecase.GetCompleteLanguageUseCase
import kotlin.coroutines.CoroutineContext

internal class DefaultCreateTermbaseWizardStepThreeComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val propertyRepository: PropertyRepository,
    private val languageRepository: LanguageRepository,
    private val getCompleteLanguage: GetCompleteLanguageUseCase,
    private val inputDescriptorRepository: InputDescriptorRepository,
) : CreateTermbaseWizardStepThreeComponent, ComponentContext by componentContext {

    private val items = MutableStateFlow<List<CreateTermbaseWizardStepThreeItem>>(emptyList())
    override val done = MutableSharedFlow<List<InputDescriptorModel>>()
    private var termbaseId: Int = 0
    private lateinit var viewModelScope: CoroutineScope

    override lateinit var uiState: StateFlow<CreateTermbaseWizardStepThreeUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(items) {
                    CreateTermbaseWizardStepThreeUiState(
                        items = it.first(),
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = CreateTermbaseWizardStepThreeUiState(),
                )
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun reset() {
        items.value = emptyList()
        termbaseId = 0
    }

    override fun loadInitial(termbase: TermbaseModel) {
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

    override fun loadItems(
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
                    val name = getCompleteLanguage(language).name
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

    override fun toggleSelection(item: CreateTermbaseWizardStepThreeItem, selected: Boolean) {
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

    override fun submit() {
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
            done.emit(result)
        }
    }
}
