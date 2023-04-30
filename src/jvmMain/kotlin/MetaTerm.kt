import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import di.commonKoinModule
import di.databaseKoinModule
import di.mainKoinModule
import di.repositoryKoinModule
import di.termbasesKoinModule
import di.termsKoinModule
import keystore.TemporaryKeyStore
import kotlinx.coroutines.runBlocking
import log.LogManager
import moe.tlaster.precompose.PreComposeWindow
import moe.tlaster.precompose.ui.viewModel
import notification.NotificationCenter
import org.koin.core.context.startKoin
import ui.TermsViewModel
import ui.detail.TermDetailViewModel
import ui.dialog.create.CreateTermbaseViewModel
import ui.dialog.create.CreateTermbaseWizardDialog
import ui.dialog.create.stepone.CreateTermbaseWizardStepOneViewModel
import ui.dialog.create.stepthree.CreateTermbaseWizardStepThreeViewModel
import ui.dialog.create.steptwo.CreateTermbaseWizardStepTwoViewModel
import ui.dialog.edit.EditTermbaseDialog
import ui.dialog.edit.EditTermbaseViewModel
import ui.dialog.manage.ManageTermbasesDialog
import ui.dialog.manage.ManageTermbasesViewModel
import ui.dialog.statistics.TermbaseStatisticsDialog
import ui.dialog.statistics.TermbaseStatisticsViewModel
import ui.components.CustomOpenFileDialog
import ui.components.CustomSaveFileDialog
import ui.components.CustomTabBar
import ui.dialog.filter.TermFilterViewModel
import ui.screens.intro.IntroScreen
import ui.screens.intro.IntroViewModel
import ui.screens.main.MainScreen
import ui.screens.main.MainViewModel
import ui.theme.MetaTermTheme
import ui.theme.SelectedBackground
import ui.theme.Spacing
import java.util.Locale

private val koin = startKoin {
    modules(
        commonKoinModule,
        databaseKoinModule,
        repositoryKoinModule,

        mainKoinModule,
        termbasesKoinModule,
        termsKoinModule,
    )
}.koin


@Composable
@Preview
private fun App(viewModel: AppViewModel) {
    MetaTermTheme {
        val uiState by viewModel.uiState.collectAsState()
        val openedTermbases = uiState.openedTermbases
        val termbase = uiState.currentTermbase
        val currentIndex = openedTermbases.indexOfFirst { it.id == termbase?.id }.takeIf { it >= 0 }
        Column(
            modifier = Modifier.background(
                color = MaterialTheme.colors.background,
            ).padding(top = Spacing.xs),
        ) {
            CustomTabBar(
                modifier = Modifier.fillMaxWidth(),
                tabs = openedTermbases.map { it.name },
                current = currentIndex,
                onTabSelected = {
                    val tb = openedTermbases[it]
                    viewModel.setCurrentTermbase(tb)
                },
                rightIcon = Icons.Default.Close,
                onRightIconClicked = {
                    val tb = openedTermbases[it]
                    viewModel.closeTermbase(tb)
                },
            )
            if (termbase != null) {
                val mainViewModel: MainViewModel = viewModel {
                    koin.get()
                }
                val termsViewModel: TermsViewModel = viewModel {
                    koin.get()
                }
                val termDetailViewModel: TermDetailViewModel = viewModel {
                    koin.get()
                }
                val termFilterViewModel: TermFilterViewModel = viewModel {
                    koin.get()
                }
                MainScreen(
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                        .background(color = SelectedBackground, shape = RoundedCornerShape(4.dp))
                        .padding(Spacing.xs),
                    termbase = termbase,
                    mainViewModel = mainViewModel,
                    termsViewModel = termsViewModel,
                    termDetailViewModel = termDetailViewModel,
                    termFilterViewModel = termFilterViewModel,
                )
            } else {
                val introViewModel: IntroViewModel = viewModel {
                    koin.get()
                }
                introViewModel.reset()
                IntroScreen(
                    viewModel = introViewModel,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    // init log
    val log: LogManager = koin.get()
    log.debug("App initialized")
    Thread.setDefaultUncaughtExceptionHandler { t, e ->
        log.exception("Exception in ${t.name}", cause = e)
    }

    // init l10n
    val keyStore: TemporaryKeyStore = koin.get()
    runBlocking {
        val currentLang = keyStore.get("lang", Locale.getDefault().language)
        L10n.setLanguage(currentLang)
    }

    PreComposeWindow(
        onCloseRequest = ::exitApplication,
        title = "app_name".localized(),
        state = rememberWindowState(size = DpSize.Unspecified),
    ) {
        val appViewModel: AppViewModel = viewModel {
            koin.get()
        }

        val uiState by appViewModel.uiState.collectAsState()
        var newTermbaseWizardOpen by remember {
            mutableStateOf(false)
        }
        var manageTermbasesDialogOpen by remember {
            mutableStateOf(false)
        }
        var editTermbaseWizardOpen by remember {
            mutableStateOf(false)
        }
        var exportTbxDialogOpen by remember {
            mutableStateOf(false)
        }
        var exportCsvDialogOpen by remember {
            mutableStateOf(false)
        }
        var importTbxDialogOpen by remember {
            mutableStateOf(false)
        }
        var importCsvDialogOpen by remember {
            mutableStateOf(false)
        }
        var statisticsDialogOpen by remember {
            mutableStateOf(false)
        }

        val notificationCenter: NotificationCenter = koin.get()

        MenuBar {
            Menu("menu_termbase".localized()) {
                Item(
                    text = "menu_termbase_new".localized(),
                    shortcut = KeyShortcut(key = Key.N, meta = true),
                ) {
                    appViewModel.shouldOpenNewTermbaseOnDialogClose = true
                    newTermbaseWizardOpen = true
                }
                Item(
                    text = "menu_termbase_edit".localized(),
                    shortcut = KeyShortcut(key = Key.D, meta = true),
                ) {
                    appViewModel.termbaseToEdit = uiState.currentTermbase
                    editTermbaseWizardOpen = true
                }
                Item(
                    text = "menu_termbase_manage".localized(),
                    shortcut = KeyShortcut(key = Key.O, meta = true),
                ) {
                    manageTermbasesDialogOpen = true
                }
                Separator()
                Item(
                    text = "menu_termbase_settings".localized(),
                    shortcut = KeyShortcut(key = Key.Comma, meta = true),
                ) {}
                Item(
                    text = "menu_termbase_statistics".localized(),
                    shortcut = KeyShortcut(key = Key.Period, meta = true),
                ) {
                    statisticsDialogOpen = true
                }
                Separator()
                Menu("menu_termbase_import".localized()) {
                    Item(
                        text = "menu_termbase_import_csv".localized(),
                        enabled = uiState.currentTermbase != null,
                    ) {
                        importCsvDialogOpen = true
                    }
                    Item(
                        text = "menu_termbase_import_tbx".localized(),
                        enabled = uiState.currentTermbase != null,
                    ) {
                        importTbxDialogOpen = true
                    }
                }
                Menu("menu_termbase_export".localized()) {
                    Item(
                        text = "menu_termbase_export_csv".localized(),
                        enabled = uiState.currentTermbase != null,
                    ) {
                        exportCsvDialogOpen = true
                    }
                    Item(
                        text = "menu_termbase_export_tbx".localized(),
                        enabled = uiState.currentTermbase != null,
                    ) {
                        exportTbxDialogOpen = true
                    }
                }
                Separator()
                Item(
                    text = "button_close".localized(),
                    shortcut = KeyShortcut(key = Key.W, meta = true),
                ) {
                    exitApplication()
                }
            }
            Menu(
                text = "menu_entry".localized(),
            ) {
                Item(
                    text = "menu_entry_new".localized(),
                    enabled = uiState.currentTermbase != null,
                    shortcut = KeyShortcut(key = Key.Plus, meta = true),
                ) {
                    notificationCenter.send(NotificationCenter.Event.CreateEntry)
                }
                if (uiState.entryEditMode) {
                    Item(
                        text = "menu_entry_close_edit".localized(),
                        enabled = uiState.currentTermbase != null && uiState.currentEntry != null,
                        shortcut = KeyShortcut(key = Key.E, meta = true),
                    ) {
                        notificationCenter.send(NotificationCenter.Event.CloseEntryEditMode)
                    }
                } else {
                    Item(
                        text = "menu_entry_edit".localized(),
                        enabled = uiState.currentTermbase != null && uiState.currentEntry != null,
                        shortcut = KeyShortcut(key = Key.E, meta = true),
                    ) {
                        notificationCenter.send(NotificationCenter.Event.OpenEntryEditMode)
                    }
                }
                Item(
                    text = "menu_entry_save".localized(),
                    enabled = uiState.currentTermbase != null && uiState.currentEntry != null && uiState.entryEditMode,
                    shortcut = KeyShortcut(key = Key.S, meta = true),
                ) {
                    notificationCenter.send(NotificationCenter.Event.SaveEntry)
                }
                Item(
                    text = "menu_entry_delete".localized(),
                    enabled = uiState.currentTermbase != null && uiState.currentEntry != null,
                    shortcut = KeyShortcut(key = Key.Backspace, meta = true),
                ) {
                    notificationCenter.send(NotificationCenter.Event.DeleteEntry)
                }
            }
        }

        App(viewModel = appViewModel)

        if (manageTermbasesDialogOpen) {
            val viewModel: ManageTermbasesViewModel = viewModel {
                koin.get()
            }
            ManageTermbasesDialog(
                viewModel = viewModel,
                onNew = {
                    newTermbaseWizardOpen = true
                },
                onClose = {
                    manageTermbasesDialogOpen = false
                },
                onEdit = {
                    appViewModel.termbaseToEdit = it
                    editTermbaseWizardOpen = true
                },
            )
        }

        if (newTermbaseWizardOpen) {
            val viewModel: CreateTermbaseViewModel = viewModel {
                koin.get()
            }
            viewModel.reset()
            val stepOneViewModel: CreateTermbaseWizardStepOneViewModel = viewModel {
                koin.get()
            }
            stepOneViewModel.reset()
            val stepTwoViewModel: CreateTermbaseWizardStepTwoViewModel = viewModel {
                koin.get()
            }
            stepTwoViewModel.reset()
            val stepThreeViewModel: CreateTermbaseWizardStepThreeViewModel = viewModel {
                koin.get()
            }
            stepThreeViewModel.reset()
            CreateTermbaseWizardDialog(
                openNewlyCreated = appViewModel.shouldOpenNewTermbaseOnDialogClose,
                viewModel = viewModel,
                stepOneViewModel = stepOneViewModel,
                stepTwoViewModel = stepTwoViewModel,
                stepThreeViewModel = stepThreeViewModel,
                onClose = {
                    newTermbaseWizardOpen = false
                    appViewModel.shouldOpenNewTermbaseOnDialogClose = false
                },
            )
        }

        if (editTermbaseWizardOpen) {
            val termbase = appViewModel.termbaseToEdit
            if (termbase != null) {
                val viewModel: EditTermbaseViewModel = viewModel {
                    koin.get()
                }
                viewModel.reset()
                val stepOneViewModel: CreateTermbaseWizardStepOneViewModel = viewModel {
                    koin.get()
                }
                stepOneViewModel.reset()
                val stepTwoViewModel: CreateTermbaseWizardStepTwoViewModel = viewModel {
                    koin.get()
                }
                stepTwoViewModel.reset()
                val stepThreeViewModel: CreateTermbaseWizardStepThreeViewModel = viewModel {
                    koin.get()
                }
                stepThreeViewModel.reset()
                EditTermbaseDialog(
                    initialTermbase = termbase,
                    viewModel = viewModel,
                    stepOneViewModel = stepOneViewModel,
                    stepTwoViewModel = stepTwoViewModel,
                    stepThreeViewModel = stepThreeViewModel,
                    onClose = {
                        appViewModel.termbaseToEdit = null
                        editTermbaseWizardOpen = false
                    },
                )
            }
        }

        if (exportTbxDialogOpen) {
            CustomSaveFileDialog(
                title = "dialog_title_export".localized(),
                initialFileName = "termbase.tbx",
                nameFilter = { it.endsWith(".tbx") },
                onCloseRequest = { dest ->
                    if (!dest.isNullOrEmpty()) {
                        appViewModel.exportTbx(dest)
                    }
                    exportTbxDialogOpen = false
                },
            )
        }

        if (exportCsvDialogOpen) {
            CustomSaveFileDialog(
                title = "dialog_title_export".localized(),
                initialFileName = "termbase.csv",
                nameFilter = { it.endsWith(".csv") },
                onCloseRequest = { dest ->
                    if (!dest.isNullOrEmpty()) {
                        appViewModel.exportCsv(dest)
                    }
                    exportTbxDialogOpen = false
                },
            )
        }

        if (importCsvDialogOpen) {
            CustomOpenFileDialog(
                title = "dialog_title_import".localized(),
                nameFilter = { it.endsWith(".csv") },
                onCloseRequest = { path ->
                    if (!path.isNullOrEmpty()) {
                        appViewModel.importCsv(path)
                    }
                    importCsvDialogOpen = false
                },
            )
        }

        if (importTbxDialogOpen) {
            CustomOpenFileDialog(
                title = "dialog_title_import".localized(),
                nameFilter = { it.endsWith(".tbx") },
                onCloseRequest = { path ->
                    if (!path.isNullOrEmpty()) {
                        appViewModel.importTbx(path)
                    }
                    importTbxDialogOpen = false
                },
            )
        }

        if (statisticsDialogOpen) {
            val viewModel: TermbaseStatisticsViewModel = viewModel {
                koin.get()
            }
            uiState.currentTermbase?.also {
                TermbaseStatisticsDialog(
                    termbase = it,
                    viewModel = viewModel,
                    onClose = {
                        statisticsDialogOpen = false
                    },
                )
            }
        }
    }
}
