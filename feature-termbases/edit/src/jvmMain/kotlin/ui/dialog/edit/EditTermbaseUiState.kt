package ui.dialog.edit

data class EditTermbaseUiState(
    val step: Int = 0,
    val loading: Boolean = false,
    val done: Boolean = false,
)
