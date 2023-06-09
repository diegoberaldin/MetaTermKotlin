package terms.ui

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import data.LanguageModel
import data.SearchCriterion
import data.TermModel
import data.TermbaseModel
import kotlinx.coroutines.flow.StateFlow

interface TermsComponent {
    val uiState: StateFlow<TermsUiState>
    val searchUiState: StateFlow<TermsSearchUiState>
    val toolbarUiState: StateFlow<TermsToolbarUiState>
    val termDetail: Value<ChildSlot<TermDetailConfig, *>>
    val dialog: Value<ChildSlot<DialogConfig, *>>

    fun load(termbase: TermbaseModel)
    fun changeSourceLanguage(language: LanguageModel)
    fun changeTargetLanguage(language: LanguageModel)
    fun switchLanguages()
    fun selectTerm(value: TermModel?)
    fun sendCreateEntryEvent()
    fun deleteCurrentEntry()
    fun setEntryEditMode(value: Boolean)
    fun setSearch(value: String)
    fun setSearchCriteria(value: List<SearchCriterion>)
    fun searchTerms()

    fun openDialog(config: DialogConfig)
    fun closeDialog()

    @Parcelize
    object TermDetailConfig : Parcelable

    sealed interface DialogConfig : Parcelable {

        @Parcelize
        object None : DialogConfig

        @Parcelize
        object Filter : DialogConfig
    }
}