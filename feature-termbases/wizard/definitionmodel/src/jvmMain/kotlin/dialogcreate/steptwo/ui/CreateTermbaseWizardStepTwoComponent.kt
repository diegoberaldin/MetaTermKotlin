package dialogcreate.steptwo.ui

import data.PropertyLevel
import data.PropertyModel
import data.PropertyType
import data.TermbaseModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CreateTermbaseWizardStepTwoComponent {
    val uiState: StateFlow<CreateTermbaseStepTwoUiState>
    val currentPropertyState: StateFlow<CreteTermbaseStepTwoEditPropertyState>
    val done: SharedFlow<List<PropertyModel>>
    fun reset()
    fun loadInitial(termbase: TermbaseModel)
    fun selectProperty(value: PropertyModel)
    fun addProperty(level: PropertyLevel)
    fun removeProperty()
    fun setCurrentPropertyName(value: String)
    fun setCurrentPropertyLevel(value: PropertyLevel)
    fun setCurrentPropertyType(value: PropertyType)
    fun addPicklistValue(value: String)
    fun removePicklistValue(index: Int)
    fun submit()
}