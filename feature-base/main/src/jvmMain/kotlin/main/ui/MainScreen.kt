package main.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import data.TermbaseModel
import terms.ui.TermsComponent
import terms.ui.TermsScreen

@Composable
fun MainScreen(
    component: MainComponent,
    termbase: TermbaseModel,
    modifier: Modifier = Modifier,
) {
    val termsContent by component.terms.subscribeAsState()
    LaunchedEffect(termbase) {
        component.load(termbase)
    }

    val uiState by component.uiState.collectAsState()

    Column(
        modifier = modifier,
    ) {
        when (termsContent.child?.configuration) {
            MainComponent.TermsConfig -> {
                val childComponent = termsContent.child?.instance as TermsComponent
                LaunchedEffect(childComponent) {
                    childComponent.load(termbase)
                }
                TermsScreen(
                    component = childComponent,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }

            else -> Unit
        }

        StatusBar(
            modifier = Modifier.fillMaxWidth(),
            termbaseName = uiState.termbase?.name ?: "",
            entryCount = uiState.entryCount,
        )
    }
}
