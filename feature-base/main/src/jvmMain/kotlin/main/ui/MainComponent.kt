package main.ui

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import data.TermbaseModel
import kotlinx.coroutines.flow.StateFlow

interface MainComponent {
    val uiState: StateFlow<MainUiState>
    val terms: Value<ChildSlot<TermsConfig, *>>

    fun load(termbase: TermbaseModel)

    @Parcelize
    object TermsConfig: Parcelable
}