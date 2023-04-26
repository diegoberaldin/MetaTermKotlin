package ui.dialog.create.stepthree

import data.PropertyLevel
import data.PropertyModel

sealed interface CreateTermbaseWizardStepThreeItem {
    data class SectionHeader(val level: PropertyLevel, val lang: String? = null) : CreateTermbaseWizardStepThreeItem
    data class LanguageHeader(val name: String, val lang: String) : CreateTermbaseWizardStepThreeItem

    data class Lemma(val lang: String, val selected: Boolean = false) : CreateTermbaseWizardStepThreeItem

    data class Property(val property: PropertyModel, val lang: String? = null, val selected: Boolean = false) :
        CreateTermbaseWizardStepThreeItem
}
