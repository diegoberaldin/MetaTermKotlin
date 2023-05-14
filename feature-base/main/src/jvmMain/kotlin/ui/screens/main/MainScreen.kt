package ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.instancekeeper.getOrCreate
import data.TermbaseModel
import org.koin.java.KoinJavaComponent.inject
import ui.TermsScreen
import ui.TermsViewModel
import ui.detail.TermDetailViewModel
import ui.dialog.filter.TermFilterViewModel
import utils.AppBusiness

@Composable
fun MainScreen(
    termbase: TermbaseModel,
    modifier: Modifier = Modifier,
) {
    val mainViewModel: MainViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: MainViewModel by inject(MainViewModel::class.java)
        res
    }
    val termsViewModel: TermsViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: TermsViewModel by inject(TermsViewModel::class.java)
        res
    }
    val termDetailViewModel: TermDetailViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: TermDetailViewModel by inject(TermDetailViewModel::class.java)
        res
    }
    val termFilterViewModel: TermFilterViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: TermFilterViewModel by inject(TermFilterViewModel::class.java)
        res
    }
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
        )

        StatusBar(
            modifier = Modifier.fillMaxWidth(),
            termbaseName = uiState.termbase?.name ?: "",
            entryCount = uiState.entryCount,
        )
    }
}
