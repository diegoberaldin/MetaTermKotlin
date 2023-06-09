package dialogcreate.steptwo.ui

import data.PropertyLevel
import data.PropertyModel
import data.PropertyType

data class CreateTermbaseStepTwoUiState(
    val items: List<CreateTermbaseStepTwoItem> = emptyList(),
    val selectedProperty: PropertyModel? = null,
)

data class CreteTermbaseStepTwoEditPropertyState(
    val name: String = "",
    val level: PropertyLevel? = null,
    val type: PropertyType? = null,
    val picklistValues: List<String> = emptyList(),
)

sealed interface CreateTermbaseStepTwoItem {
    data class SectionHeader(val level: PropertyLevel) : CreateTermbaseStepTwoItem
    data class Property(val property: PropertyModel) : CreateTermbaseStepTwoItem
}
