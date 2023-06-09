package intro.ui

import data.TermbaseModel

data class IntroUiState(
    val termbases: List<TermbaseModel> = emptyList(),
)