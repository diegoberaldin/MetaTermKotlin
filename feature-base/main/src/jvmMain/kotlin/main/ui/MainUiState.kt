package main.ui

import data.TermbaseModel


data class MainUiState(
    val termbase: TermbaseModel? = null,
    val entryCount: Int = 0,
)