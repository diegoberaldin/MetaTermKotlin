package ui

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import data.ExportType
import data.TermbaseModel
import kotlinx.coroutines.flow.StateFlow

interface RootComponent {
    val uiState: StateFlow<RootUiState>
    var shouldOpenNewTermbaseOnDialogClose: Boolean
    var termbaseToEdit: TermbaseModel?
    val dialog: Value<ChildSlot<DialogConfig, *>>
    val main: Value<ChildSlot<MainConfig, *>>

    fun closeTermbase(termbase: TermbaseModel)
    fun setCurrentTermbase(termbase: TermbaseModel?)
    fun exportTbx(path: String)
    fun exportCsv(path: String)
    fun importCsv(path: String)
    fun importTbx(path: String)
    fun openDialog(config: DialogConfig)
    fun closeDialog()

    interface DialogConfig : Parcelable {
        @Parcelize
        object None : DialogConfig

        @Parcelize
        object NewTermbase : DialogConfig

        @Parcelize
        object EditTermbase : DialogConfig

        @Parcelize
        object ManageTermbases : DialogConfig

        @Parcelize
        data class Export(val type: ExportType) : DialogConfig

        @Parcelize
        data class Import(val type: ExportType) : DialogConfig

        @Parcelize
        object Statistics : DialogConfig

        @Parcelize
        object Settings : DialogConfig
    }

    interface MainConfig : Parcelable {
        @Parcelize
        object Intro : MainConfig

        @Parcelize
        data class Main(val termbase: TermbaseModel) : MainConfig

    }
}