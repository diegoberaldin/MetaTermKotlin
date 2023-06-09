package dialogcreate.stepone.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import data.LanguageModel
import data.TermbaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import localized
import repo.LanguageRepository
import usecase.GetCompleteLanguageUseCase
import kotlin.coroutines.CoroutineContext

internal class DefaultCreateTermbaseWizardStepOneComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val languageRepository: LanguageRepository,
    private val getCompleteLanguage: GetCompleteLanguageUseCase,
) : CreateTermbaseWizardStepOneComponent, ComponentContext by componentContext {

    private val name = MutableStateFlow("")
    private val description = MutableStateFlow("")
    private val availableLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())
    private val currentAvailableLanguage = MutableStateFlow<LanguageModel?>(null)
    private val selectedLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())
    private val currentSelectedLanguage = MutableStateFlow<LanguageModel?>(null)
    private val nameError = MutableStateFlow("")
    private val languagesError = MutableStateFlow("")

    override val done = MutableSharedFlow<Pair<TermbaseModel, List<LanguageModel>>>()
    private lateinit var viewModelScope: CoroutineScope

    override lateinit var uiState: StateFlow<CreateTermbaseStepOneUiState>
    override lateinit var errorUiState: StateFlow<CreateTermbaseUiErrorState>
    override lateinit var languagesUiState: StateFlow<CreateTermbaseLanguageUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
                    name,
                    description,
                ) { name, description ->
                    CreateTermbaseStepOneUiState(
                        name = name,
                        description = description,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = CreateTermbaseStepOneUiState(),
                )
                errorUiState = combine(
                    nameError,
                    languagesError,
                ) { nameError, languagesError ->
                    CreateTermbaseUiErrorState(
                        nameError = nameError,
                        languagesError = languagesError,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = CreateTermbaseUiErrorState(),
                )
                languagesUiState = combine(
                    availableLanguages,
                    currentAvailableLanguage,
                    selectedLanguages,
                    currentSelectedLanguage,
                ) { availableLanguages, currentAvailableLanguage, selectedLanguages, currentSelectedLanguage ->
                    CreateTermbaseLanguageUiState(
                        availableLanguages = availableLanguages,
                        currentAvailableLanguage = currentAvailableLanguage,
                        selectedLanguages = selectedLanguages,
                        currentSelectedLanguage = currentSelectedLanguage,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = CreateTermbaseLanguageUiState(),
                )

                reset()
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun reset() {
        val languages = languageRepository.getDefaultLanguages().map {
            getCompleteLanguage(it)
        }.sortedBy { e -> e.name }
        availableLanguages.value = languages
        name.value = ""
        description.value = ""
        currentAvailableLanguage.value = null
        selectedLanguages.value = emptyList()
        currentSelectedLanguage.value = null
        nameError.value = ""
        languagesError.value = ""
    }

    override fun loadInitial(termbase: TermbaseModel) {
        name.value = termbase.name
        description.value = termbase.description

        viewModelScope.launch(dispatcherProvider.io) {
            val languages = languageRepository.getAll(termbase.id).map {
                getCompleteLanguage(it)
            }
            for (language in languages) {
                selectedLanguages.update {
                    (it + language).sortedBy { e -> e.name }
                }
                availableLanguages.update {
                    (it - language).sortedBy { e -> e.name }
                }
            }
        }
    }

    override fun setName(value: String) {
        name.value = value
    }

    override fun setDescription(value: String) {
        description.value = value
    }

    override fun onAvailableClick(language: LanguageModel) {
        currentAvailableLanguage.value = language
    }

    override fun onArrowRight() {
        val language = currentAvailableLanguage.getAndUpdate { null } ?: return
        selectedLanguages.update {
            (it + language).sortedBy { e -> e.name }
        }
        availableLanguages.update {
            (it - language).sortedBy { e -> e.name }
        }
    }

    override fun onSelectedClick(language: LanguageModel) {
        currentSelectedLanguage.value = language
    }

    override fun onArrowLeft() {
        val language = currentSelectedLanguage.getAndUpdate { null } ?: return
        availableLanguages.update {
            (it + language).sortedBy { e -> e.name }
        }
        selectedLanguages.update {
            (it - language).sortedBy { e -> e.name }
        }
    }

    override fun clearErrors() {
        nameError.value = ""
        languagesError.value = ""
    }

    override fun submit() {
        clearErrors()
        val name = name.value
        if (name.trim().isEmpty()) {
            nameError.value = "error_missing_name".localized()
            return
        }
        val selectedLanguages = selectedLanguages.value
        if (selectedLanguages.isEmpty()) {
            languagesError.value = "error_missing_language".localized()
            return
        }

        val termbase = TermbaseModel(
            name = name,
            description = description.value,
        )

        viewModelScope.launch {
            done.emit(termbase to selectedLanguages)
        }
    }
}
