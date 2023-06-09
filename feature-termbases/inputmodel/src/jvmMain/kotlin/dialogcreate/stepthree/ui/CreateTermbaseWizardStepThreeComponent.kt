package dialogcreate.stepthree.ui

import data.InputDescriptorModel
import data.LanguageModel
import data.PropertyModel
import data.TermbaseModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CreateTermbaseWizardStepThreeComponent {
    val done: SharedFlow<List<InputDescriptorModel>>
    val uiState: StateFlow<CreateTermbaseWizardStepThreeUiState>
    fun reset()
    fun loadInitial(termbase: TermbaseModel)
    fun loadItems(
        properties: List<PropertyModel>,
        languages: List<LanguageModel>,
        oldInputModel: List<InputDescriptorModel>,
    )

    fun toggleSelection(item: CreateTermbaseWizardStepThreeItem, selected: Boolean)
    fun submit()
}