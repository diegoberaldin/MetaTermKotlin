package ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import data.TermbaseModel
import ui.TermsScreen
import ui.TermsViewModel
import ui.detail.TermDetailViewModel
import ui.dialog.filter.TermFilterViewModel

@Composable
fun MainScreen(
    termbase: TermbaseModel,
    mainViewModel: MainViewModel,
    termsViewModel: TermsViewModel,
    termDetailViewModel: TermDetailViewModel,
    termFilterViewModel: TermFilterViewModel,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(termbase) {
        mainViewModel.load(termbase)
        termsViewModel.load(termbase)
    }

    val uiState by mainViewModel.uiState.collectAsState()

    Column(
        modifier = modifier,
    ) {
        TermsScreen(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            viewModel = termsViewModel,
            termDetailViewModel = termDetailViewModel,
            termFilterViewModel = termFilterViewModel,
        )

        StatusBar(
            modifier = Modifier.fillMaxWidth(),
            termbaseName = uiState.termbase?.name ?: "",
            entryCount = uiState.entryCount,
        )
    }
}
