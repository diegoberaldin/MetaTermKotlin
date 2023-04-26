package ui.dialog.manage

import data.TermbaseModel

data class ManageTermbasesUiState(
    val termbases: List<TermbaseModel> = emptyList(),
    val selectedTermbase: TermbaseModel? = null,
    val loading: Boolean = false,
)
