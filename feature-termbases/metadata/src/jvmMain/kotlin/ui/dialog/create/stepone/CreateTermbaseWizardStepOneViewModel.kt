package ui.dialog.create.stepone

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import coroutines.CoroutineDispatcherProvider
import data.LanguageModel
import data.TermbaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import localized
import repository.FlagsRepository
import repository.LanguageNameRepository
import repository.LanguageRepository

class CreateTermbaseWizardStepOneViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val languageRepository: LanguageRepository,
    private val languageNameRepository: LanguageNameRepository,
    private val flagsRepository: FlagsRepository,
) : InstanceKeeper.Instance {

    private val name = MutableStateFlow("")
    private val description = MutableStateFlow("")
    private val availableLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())
    private val currentAvailableLanguage = MutableStateFlow<LanguageModel?>(null)
    private val selectedLanguages = MutableStateFlow<List<LanguageModel>>(emptyList())
    private val currentSelectedLanguage = MutableStateFlow<LanguageModel?>(null)
    private val nameError = MutableStateFlow("")
    private val languagesError = MutableStateFlow("")

    private val _done = MutableSharedFlow<Pair<TermbaseModel, List<LanguageModel>>>()
    val done = _done.asSharedFlow()
    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = combine(
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

    val errorUiState = combine(
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

    val languagesUiState = combine(
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

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun reset() {
        val languages = languageRepository.getDefaultLanguages().map {
            val flag = flagsRepository.getFlag(it.code)
            val name = languageNameRepository.getName(it.code)
            it.copy(
                name = "$flag $name",
            )
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

    fun loadInitial(termbase: TermbaseModel) {
        name.value = termbase.name
        description.value = termbase.description

        viewModelScope.launch(dispatcherProvider.io) {
            val languages = languageRepository.getAll(termbase.id).map {
                val flag = flagsRepository.getFlag(it.code)
                val name = languageNameRepository.getName(it.code)
                it.copy(termbaseId = 0, id = 0, name = "$flag $name")
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

    fun setName(value: String) {
        name.value = value
    }

    fun setDescription(value: String) {
        description.value = value
    }

    fun onAvailableClick(language: LanguageModel) {
        currentAvailableLanguage.value = language
    }

    fun onArrowRight() {
        val language = currentAvailableLanguage.getAndUpdate { null } ?: return
        selectedLanguages.update {
            (it + language).sortedBy { e -> e.name }
        }
        availableLanguages.update {
            (it - language).sortedBy { e -> e.name }
        }
    }

    fun onSelectedClick(language: LanguageModel) {
        currentSelectedLanguage.value = language
    }

    fun onArrowLeft() {
        val language = currentSelectedLanguage.getAndUpdate { null } ?: return
        availableLanguages.update {
            (it + language).sortedBy { e -> e.name }
        }
        selectedLanguages.update {
            (it - language).sortedBy { e -> e.name }
        }
    }

    fun clearErrors() {
        nameError.value = ""
        languagesError.value = ""
    }

    fun submit() {
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
            _done.emit(termbase to selectedLanguages)
        }
    }
}
