package dialogcreate.ui

data class CreateTermbaseUiState(
    val step: Int = 0,
    val loading: Boolean = false,
    val done: Boolean = false,
)
