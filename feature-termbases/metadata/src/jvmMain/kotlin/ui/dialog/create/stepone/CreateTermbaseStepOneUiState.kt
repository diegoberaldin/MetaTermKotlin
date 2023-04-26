package ui.dialog.create.stepone

import data.LanguageModel

data class CreateTermbaseStepOneUiState(
    val step: Int = 0,
    val name: String = "",
    val description: String = "",
    val loading: Boolean = false,
    val done: Boolean = false,
)

data class CreateTermbaseUiErrorState(
    val nameError: String = "",
    val languagesError: String = "",
    val genericError: String = "",
)

data class CreateTermbaseLanguageUiState(
    val availableLanguages: List<LanguageModel> = emptyList(),
    val currentAvailableLanguage: LanguageModel? = null,
    val selectedLanguages: List<LanguageModel> = emptyList(),
    val currentSelectedLanguage: LanguageModel? = null,
)
