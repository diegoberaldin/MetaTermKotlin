package dialogcreate.stepone.ui

import data.LanguageModel
import data.TermbaseModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CreateTermbaseWizardStepOneComponent {
    val done: SharedFlow<Pair<TermbaseModel, List<LanguageModel>>>
    val uiState: StateFlow<CreateTermbaseStepOneUiState>
    val errorUiState: StateFlow<CreateTermbaseUiErrorState>
    val languagesUiState: StateFlow<CreateTermbaseLanguageUiState>
    fun reset()
    fun loadInitial(termbase: TermbaseModel)
    fun setName(value: String)
    fun setDescription(value: String)
    fun onAvailableClick(language: LanguageModel)
    fun onArrowRight()
    fun onSelectedClick(language: LanguageModel)
    fun onArrowLeft()
    fun clearErrors()
    fun submit()
}