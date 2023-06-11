package dialogsettings.ui

import data.LanguageModel
import kotlinx.coroutines.flow.StateFlow

interface SettingsComponent {
    val uiState: StateFlow<SettingsUiState>

    fun setLanguage(value: LanguageModel)
    fun setSpellcheckEnabled(value: Boolean)
}
