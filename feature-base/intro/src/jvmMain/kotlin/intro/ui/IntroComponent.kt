package intro.ui

import data.TermbaseModel
import kotlinx.coroutines.flow.StateFlow

interface IntroComponent {
    val uiState: StateFlow<IntroUiState>

    fun load()
    fun openTermbase(termbase: TermbaseModel)
}