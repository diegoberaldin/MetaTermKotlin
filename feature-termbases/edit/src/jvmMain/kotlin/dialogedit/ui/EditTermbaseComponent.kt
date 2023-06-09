package dialogedit.ui

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import data.InputDescriptorModel
import data.LanguageModel
import data.PropertyModel
import data.TermbaseModel
import kotlinx.coroutines.flow.StateFlow

interface EditTermbaseComponent {
    val uiState: StateFlow<EditTermbaseUiState>
    val content: Value<ChildSlot<ContentConfig, *>>

    fun reset()
    fun setTermbase(value: TermbaseModel)
    fun changeStep(index: Int)
    fun submitStep1(name: String, description: String, selectedLanguages: List<LanguageModel>)
    fun submitStep2(properties: List<PropertyModel>)
    fun submitStep3(descriptors: List<InputDescriptorModel>)

    sealed interface ContentConfig : Parcelable {
        @Parcelize
        object Step1 : ContentConfig

        @Parcelize
        object Step2 : ContentConfig

        @Parcelize
        object Step3 : ContentConfig
    }
}