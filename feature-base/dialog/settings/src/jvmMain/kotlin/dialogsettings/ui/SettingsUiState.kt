package dialogsettings.ui

import data.LanguageModel

data class SettingsUiState(
    val availableLanguages: List<LanguageModel> = emptyList(),
    val currentLanguage: LanguageModel? = null,
    val spellcheckEnabled: Boolean = false,
    val appVersion: String = "",
)
