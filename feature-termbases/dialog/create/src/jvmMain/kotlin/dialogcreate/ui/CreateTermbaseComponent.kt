package dialogcreate.ui

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import data.InputDescriptorModel
import data.LanguageModel
import data.PropertyModel
import data.TermbaseModel
import kotlinx.coroutines.flow.StateFlow

interface CreateTermbaseComponent {
    val uiState: StateFlow<CreateTermbaseUiState>
    val content: Value<ChildSlot<ContentConfig, *>>

    fun reset()
    fun setOpenNewlyCreated(value: Boolean)
    fun setTermbase(termbase: TermbaseModel)
    fun setSelectedLanguages(value: List<LanguageModel>)
    fun setProperties(value: List<PropertyModel>)
    fun getProperties(): List<PropertyModel>
    fun getLanguages(): List<LanguageModel>
    fun getInputModelDescriptors(): List<InputDescriptorModel>
    fun setInputModelDescriptors(value: List<InputDescriptorModel>)
    fun previous()
    fun next()
    fun submit()

    sealed interface ContentConfig : Parcelable {
        @Parcelize
        object Step1 : ContentConfig

        @Parcelize
        object Step2 : ContentConfig

        @Parcelize
        object Step3 : ContentConfig
    }
}