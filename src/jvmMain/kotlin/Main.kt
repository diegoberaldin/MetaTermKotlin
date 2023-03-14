import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.application
import moe.tlaster.precompose.PreComposeWindow
import ui.screens.MainScreen
import ui.theme.MetaTermTheme

@Composable
@Preview
fun App() {
    MetaTermTheme {
        MainScreen()
    }
}

fun main() = application {
    PreComposeWindow(onCloseRequest = ::exitApplication) {
        App()
    }
}
