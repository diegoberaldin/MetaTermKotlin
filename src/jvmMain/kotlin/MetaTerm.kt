import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.essenty.instancekeeper.getOrCreate
import di.*
import keystore.TemporaryKeyStore
import kotlinx.coroutines.runBlocking
import log.LogManager
import notification.NotificationCenter
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import ui.components.CustomOpenFileDialog
import ui.components.CustomSaveFileDialog
import ui.components.CustomTabBar
import ui.dialog.create.CreateTermbaseWizardDialog
import ui.dialog.create.stepone.CreateTermbaseWizardStepOneViewModel
import ui.dialog.create.stepthree.CreateTermbaseWizardStepThreeViewModel
import ui.dialog.create.steptwo.CreateTermbaseWizardStepTwoViewModel
import ui.dialog.edit.EditTermbaseDialog
import ui.dialog.edit.EditTermbaseViewModel
import ui.dialog.manage.ManageTermbasesDialog
import ui.dialog.statistics.TermbaseStatisticsDialog
import ui.screens.intro.IntroScreen
import ui.screens.main.MainScreen
import ui.theme.MetaTermTheme
import ui.theme.SelectedBackground
import ui.theme.Spacing
import utils.AppBusiness
import java.util.*

fun initKoin() {
    startKoin {
        modules(
            commonKoinModule,
            databaseKoinModule,
            repositoryKoinModule,

            mainKoinModule,
            termbasesKoinModule,
            termsKoinModule,
        )
    }
}


@Composable
@Preview
private fun App() {
    val viewModel: AppViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: AppViewModel by inject(AppViewModel::class.java)
        res
    }
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

                MainScreen(
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                        .background(color = SelectedBackground, shape = RoundedCornerShape(4.dp))
                        .padding(Spacing.xs),
                    termbase = termbase,
                )
            } else {

                IntroScreen()
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    // init DI
    initKoin()

    // init log
    val log: LogManager by inject(LogManager::class.java)
    log.debug("App initialized")
    Thread.setDefaultUncaughtExceptionHandler { t, e ->
        log.exception("Exception in ${t.name}", cause = e)
    }

    // init l10n
    val keyStore: TemporaryKeyStore by inject(TemporaryKeyStore::class.java)
    runBlocking {
        val currentLang = keyStore.get("lang", Locale.getDefault().language)
        L10n.setLanguage(currentLang)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "app_name".localized(),
        state = rememberWindowState(size = DpSize.Unspecified),
    ) {
        val appViewModel: AppViewModel = AppBusiness.instanceKeeper.getOrCreate {
            val res: AppViewModel by inject(AppViewModel::class.java)
            res
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

        val notificationCenter: NotificationCenter by inject(NotificationCenter::class.java)

        MenuBar {
            Menu("menu_termbase".localized()) {
                Item(
                    text = "menu_termbase_new".localized(),
                    shortcut = KeyShortcut(key = Key.N, meta = true),
                ) {
                    val viewModel: EditTermbaseViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: EditTermbaseViewModel by inject(EditTermbaseViewModel::class.java)
                        res
                    }
                    viewModel.reset()
                    val stepOneViewModel: CreateTermbaseWizardStepOneViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: CreateTermbaseWizardStepOneViewModel by inject(CreateTermbaseWizardStepOneViewModel::class.java)
                        res
                    }
                    stepOneViewModel.reset()
                    val stepTwoViewModel: CreateTermbaseWizardStepTwoViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: CreateTermbaseWizardStepTwoViewModel by inject(CreateTermbaseWizardStepTwoViewModel::class.java)
                        res
                    }
                    stepTwoViewModel.reset()
                    val stepThreeViewModel: CreateTermbaseWizardStepThreeViewModel =
                        AppBusiness.instanceKeeper.getOrCreate {
                            val res: CreateTermbaseWizardStepThreeViewModel by inject(
                                CreateTermbaseWizardStepThreeViewModel::class.java)
                            res
                        }
                    stepThreeViewModel.reset()
                    appViewModel.shouldOpenNewTermbaseOnDialogClose = true
                    newTermbaseWizardOpen = true
                }
                Item(
                    text = "menu_termbase_edit".localized(),
                    shortcut = KeyShortcut(key = Key.D, meta = true),
                    enabled = uiState.currentTermbase != null,
                ) {
                    appViewModel.termbaseToEdit = uiState.currentTermbase
                    val viewModel: EditTermbaseViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: EditTermbaseViewModel by inject(EditTermbaseViewModel::class.java)
                        res
                    }
                    viewModel.reset()
                    val stepOneViewModel: CreateTermbaseWizardStepOneViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: CreateTermbaseWizardStepOneViewModel by inject(CreateTermbaseWizardStepOneViewModel::class.java)
                        res
                    }
                    stepOneViewModel.reset()
                    val stepTwoViewModel: CreateTermbaseWizardStepTwoViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: CreateTermbaseWizardStepTwoViewModel by inject(CreateTermbaseWizardStepTwoViewModel::class.java)
                        res
                    }
                    stepTwoViewModel.reset()
                    val stepThreeViewModel: CreateTermbaseWizardStepThreeViewModel =
                        AppBusiness.instanceKeeper.getOrCreate {
                            val res: CreateTermbaseWizardStepThreeViewModel by inject(
                                CreateTermbaseWizardStepThreeViewModel::class.java)
                            res
                        }
                    stepThreeViewModel.reset()
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
                    enabled = uiState.currentTermbase != null,
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
                    enabled = uiState.currentTermbase != null,
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

        App()

        if (manageTermbasesDialogOpen) {
            ManageTermbasesDialog(
                onNew = {
                    val viewModel: EditTermbaseViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: EditTermbaseViewModel by inject(EditTermbaseViewModel::class.java)
                        res
                    }
                    viewModel.reset()
                    val stepOneViewModel: CreateTermbaseWizardStepOneViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: CreateTermbaseWizardStepOneViewModel by inject(CreateTermbaseWizardStepOneViewModel::class.java)
                        res
                    }
                    stepOneViewModel.reset()
                    val stepTwoViewModel: CreateTermbaseWizardStepTwoViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: CreateTermbaseWizardStepTwoViewModel by inject(CreateTermbaseWizardStepTwoViewModel::class.java)
                        res
                    }
                    stepTwoViewModel.reset()
                    val stepThreeViewModel: CreateTermbaseWizardStepThreeViewModel =
                        AppBusiness.instanceKeeper.getOrCreate {
                            val res: CreateTermbaseWizardStepThreeViewModel by inject(
                                CreateTermbaseWizardStepThreeViewModel::class.java)
                            res
                        }
                    stepThreeViewModel.reset()
                    newTermbaseWizardOpen = true
                },
                onClose = {
                    manageTermbasesDialogOpen = false
                },
                onEdit = {
                    appViewModel.termbaseToEdit = it
                    val viewModel: EditTermbaseViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: EditTermbaseViewModel by inject(EditTermbaseViewModel::class.java)
                        res
                    }
                    viewModel.reset()
                    val stepOneViewModel: CreateTermbaseWizardStepOneViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: CreateTermbaseWizardStepOneViewModel by inject(CreateTermbaseWizardStepOneViewModel::class.java)
                        res
                    }
                    stepOneViewModel.reset()
                    val stepTwoViewModel: CreateTermbaseWizardStepTwoViewModel = AppBusiness.instanceKeeper.getOrCreate {
                        val res: CreateTermbaseWizardStepTwoViewModel by inject(CreateTermbaseWizardStepTwoViewModel::class.java)
                        res
                    }
                    stepTwoViewModel.reset()
                    val stepThreeViewModel: CreateTermbaseWizardStepThreeViewModel =
                        AppBusiness.instanceKeeper.getOrCreate {
                            val res: CreateTermbaseWizardStepThreeViewModel by inject(
                                CreateTermbaseWizardStepThreeViewModel::class.java)
                            res
                        }
                    stepThreeViewModel.reset()
                    editTermbaseWizardOpen = true
                },
            )
        }

        if (newTermbaseWizardOpen) {
            CreateTermbaseWizardDialog(
                openNewlyCreated = appViewModel.shouldOpenNewTermbaseOnDialogClose,
                onClose = {
                    newTermbaseWizardOpen = false
                    appViewModel.shouldOpenNewTermbaseOnDialogClose = false
                },
            )
        }

        if (editTermbaseWizardOpen) {
            val termbase = appViewModel.termbaseToEdit
            if (termbase != null) {
                EditTermbaseDialog(
                    initialTermbase = termbase,
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
            uiState.currentTermbase?.also {
                TermbaseStatisticsDialog(
                    termbase = it,
                    onClose = {
                        statisticsDialogOpen = false
                    },
                )
            }
        }
    }
}
