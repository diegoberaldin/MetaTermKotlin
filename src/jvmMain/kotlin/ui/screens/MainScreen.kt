package ui.screens

import TermsScreen
import TermsViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.ui.viewModel


@Composable
fun MainScreen() {
    val navigator = rememberNavigator()
    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        navigator = navigator,
        initialRoute = "terms"
    ) {
        scene(route = "terms") {
            val viewModel = viewModel {
                TermsViewModel()
            }
            TermsScreen(viewModel = viewModel)
        }
    }
}