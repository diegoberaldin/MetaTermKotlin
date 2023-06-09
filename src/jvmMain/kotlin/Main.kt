import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import common.di.commonModule
import data.ExportType
import dialogcreate.ui.CreateTermbaseComponent
import dialogcreate.ui.CreateTermbaseWizardDialog
import dialogedit.ui.EditTermbaseComponent
import dialogedit.ui.EditTermbaseDialog
import dialogmanage.ui.ManageTermbasesComponent
import dialogmanage.ui.ManageTermbasesDialog
import dialogstatistics.ui.TermbaseStatisticsComponent
import dialogstatistics.ui.TermbaseStatisticsDialog
import intro.ui.IntroComponent
import intro.ui.IntroScreen
import common.keystore.TemporaryKeyStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import common.log.LogManager
import main.ui.MainComponent
import main.ui.MainScreen
import common.notification.NotificationCenter
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import persistence.di.databaseModule
import repository.di.repositoryModule
import root.di.rootModule
import termbases.di.termbasesModule
import terms.di.termsModule
import ui.RootComponent
import common.ui.components.CustomOpenFileDialog
import common.ui.components.CustomSaveFileDialog
import common.ui.components.CustomTabBar
import common.ui.theme.MetaTermTheme
import common.ui.theme.SelectedBackground
import common.ui.theme.Spacing
import common.utils.getByInjection
import common.utils.runOnUiThread
import java.util.*

fun initKoin() {
    startKoin {
        modules(
            commonModule,
            databaseModule,
            repositoryModule,

            rootModule,
            termbasesModule,
            termsModule,
        )
    }
}

@OptIn(ExperimentalDecomposeApi::class, ExperimentalComposeUiApi::class)
fun main() {
    // init DI
    initKoin()

    val log: LogManager = getByInjection()

    log.debug("App initialized")
    Thread.setDefaultUncaughtExceptionHandler { t, e ->
        log.exception("Exception in ${t.name}", cause = e)
    }
    // init root component in the main thread outside the application lifecycle
    val lifecycle = LifecycleRegistry()
    val mainScope = CoroutineScope(SupervisorJob())
    val rootComponent = runOnUiThread {
        getByInjection<RootComponent>(
            DefaultComponentContext(lifecycle = lifecycle),
            mainScope.coroutineContext,
        )
    }

    // init l10n
    runBlocking {
        val keystore: TemporaryKeyStore = getByInjection()
        val systemLanguage = Locale.getDefault().language
        val lang = keystore.get("lang", "")
        L10n.setLanguage(lang.ifEmpty { systemLanguage })
        if (lang.isEmpty()) {
            keystore.save("lang", "lang".localized())
        }
    }

    application {
        log.debug("Application starting")

        // ties component lifecycle to the window
        val windowState = rememberWindowState()
        LifecycleController(lifecycle, windowState)

        Window(
            onCloseRequest = ::exitApplication,
            title = "app_name".localized(),
            state = rememberWindowState(size = DpSize.Unspecified),
        ) {
            val uiState by rootComponent.uiState.collectAsState()
            val dialog by rootComponent.dialog.subscribeAsState()

            val notificationCenter: NotificationCenter by inject(NotificationCenter::class.java)

            MenuBar {
                Menu("menu_termbase".localized()) {
                    Item(
                        text = "menu_termbase_new".localized(),
                        shortcut = KeyShortcut(key = Key.N, meta = true),
                    ) {
                        rootComponent.shouldOpenNewTermbaseOnDialogClose = true
                        rootComponent.openDialog(RootComponent.DialogConfig.NewTermbase)
                    }
                    Item(
                        text = "menu_termbase_edit".localized(),
                        shortcut = KeyShortcut(key = Key.D, meta = true),
                        enabled = uiState.currentTermbase != null,
                    ) {
                        rootComponent.termbaseToEdit = uiState.currentTermbase
                        rootComponent.openDialog(RootComponent.DialogConfig.EditTermbase)
                    }
                    Item(
                        text = "menu_termbase_manage".localized(),
                        shortcut = KeyShortcut(key = Key.O, meta = true),
                    ) {
                        rootComponent.openDialog(RootComponent.DialogConfig.ManageTermbases)
                    }
                    Separator()
                    Item(
                        text = "menu_termbase_settings".localized(),
                        shortcut = KeyShortcut(key = Key.Comma, meta = true),
                    ) {
                        rootComponent.openDialog(RootComponent.DialogConfig.Settings)
                    }
                    Item(
                        text = "menu_termbase_statistics".localized(),
                        shortcut = KeyShortcut(key = Key.Period, meta = true),
                        enabled = uiState.currentTermbase != null,
                    ) {
                        rootComponent.openDialog(RootComponent.DialogConfig.Statistics)
                    }
                    Separator()
                    Menu("menu_termbase_import".localized()) {
                        Item(
                            text = "menu_termbase_import_csv".localized(),
                            enabled = uiState.currentTermbase != null,
                        ) {
                            rootComponent.openDialog(RootComponent.DialogConfig.Import(ExportType.CSV))
                        }
                        Item(
                            text = "menu_termbase_import_tbx".localized(),
                            enabled = uiState.currentTermbase != null,
                        ) {
                            rootComponent.openDialog(RootComponent.DialogConfig.Import(ExportType.TBX))
                        }
                    }
                    Menu("menu_termbase_export".localized()) {
                        Item(
                            text = "menu_termbase_export_csv".localized(),
                            enabled = uiState.currentTermbase != null,
                        ) {
                            rootComponent.openDialog(RootComponent.DialogConfig.Export(ExportType.CSV))
                        }
                        Item(
                            text = "menu_termbase_export_tbx".localized(),
                            enabled = uiState.currentTermbase != null,
                        ) {
                            rootComponent.openDialog(RootComponent.DialogConfig.Export(ExportType.TBX))
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

            App(rootComponent = rootComponent)

            when (val dialogConfig = dialog.child?.configuration) {
                RootComponent.DialogConfig.NewTermbase -> {
                    val childComponent = dialog.child?.instance as CreateTermbaseComponent
                    CreateTermbaseWizardDialog(
                        component = childComponent,
                        openNewlyCreated = rootComponent.shouldOpenNewTermbaseOnDialogClose,
                        onClose = {
                            rootComponent.closeDialog()
                            rootComponent.shouldOpenNewTermbaseOnDialogClose = false
                        },
                    )
                }

                RootComponent.DialogConfig.EditTermbase -> {
                    val termbase = rootComponent.termbaseToEdit
                    val childComponent = dialog.child?.instance as EditTermbaseComponent
                    if (termbase != null) {
                        EditTermbaseDialog(
                            component = childComponent,
                            initialTermbase = termbase,
                            onClose = {
                                rootComponent.termbaseToEdit = null
                                rootComponent.closeDialog()
                            },
                        )
                    }
                }

                RootComponent.DialogConfig.ManageTermbases -> {
                    val childComponent = dialog.child?.instance as ManageTermbasesComponent
                    ManageTermbasesDialog(
                        component = childComponent,
                        onNew = {
                            rootComponent.openDialog(RootComponent.DialogConfig.NewTermbase)
                        },
                        onClose = {
                            rootComponent.closeDialog()
                        },
                        onEdit = {
                            rootComponent.termbaseToEdit = it
                            rootComponent.openDialog(RootComponent.DialogConfig.EditTermbase)
                        },
                    )
                }

                is RootComponent.DialogConfig.Export -> {
                    when (dialogConfig.type) {
                        ExportType.CSV -> {
                            CustomSaveFileDialog(
                                title = "dialog_title_export".localized(),
                                initialFileName = "termbase.csv",
                                nameFilter = { it.endsWith(".csv") },
                                onCloseRequest = { dest ->
                                    if (!dest.isNullOrEmpty()) {
                                        rootComponent.exportCsv(dest)
                                    }
                                    rootComponent.closeDialog()
                                },
                            )
                        }

                        ExportType.TBX -> {
                            CustomSaveFileDialog(
                                title = "dialog_title_export".localized(),
                                initialFileName = "termbase.tbx",
                                nameFilter = { it.endsWith(".tbx") },
                                onCloseRequest = { dest ->
                                    if (!dest.isNullOrEmpty()) {
                                        rootComponent.exportTbx(dest)
                                    }
                                    rootComponent.closeDialog()
                                },
                            )
                        }
                    }
                }

                is RootComponent.DialogConfig.Import -> {
                    when (dialogConfig.type) {
                        ExportType.CSV -> {
                            CustomOpenFileDialog(
                                title = "dialog_title_import".localized(),
                                nameFilter = { it.endsWith(".csv") },
                                onCloseRequest = { path ->
                                    if (!path.isNullOrEmpty()) {
                                        rootComponent.importCsv(path)
                                    }
                                    rootComponent.closeDialog()
                                },
                            )
                        }

                        ExportType.TBX -> {
                            CustomOpenFileDialog(
                                title = "dialog_title_import".localized(),
                                nameFilter = { it.endsWith(".tbx") },
                                onCloseRequest = { path ->
                                    if (!path.isNullOrEmpty()) {
                                        rootComponent.importTbx(path)
                                    }
                                    rootComponent.closeDialog()
                                },
                            )
                        }
                    }
                }

                RootComponent.DialogConfig.Statistics -> {
                    val childComponent = dialog.child?.instance as TermbaseStatisticsComponent
                    uiState.currentTermbase?.also {
                        TermbaseStatisticsDialog(
                            component = childComponent,
                            termbase = it,
                            onClose = {
                                rootComponent.closeDialog()
                            },
                        )
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun App(rootComponent: RootComponent) {
    MetaTermTheme {
        val uiState by rootComponent.uiState.collectAsState()
        val openedTermbases = uiState.openedTermbases
        val termbase = uiState.currentTermbase
        val currentIndex = openedTermbases.indexOfFirst { it.id == termbase?.id }.takeIf { it >= 0 }
        val mainConfig by rootComponent.main.subscribeAsState()
        Column(
            modifier = Modifier.background(
                color = MaterialTheme.colors.background,
            ).padding(top = Spacing.xs).fillMaxSize(),
        ) {
            CustomTabBar(
                modifier = Modifier.fillMaxWidth(),
                tabs = openedTermbases.map { it.name },
                current = currentIndex,
                onTabSelected = {
                    val tb = openedTermbases[it]
                    rootComponent.setCurrentTermbase(tb)
                },
                rightIcon = Icons.Default.Close,
                onRightIconClicked = {
                    val tb = openedTermbases[it]
                    rootComponent.closeTermbase(tb)
                },
            )
            when (val config = mainConfig.child?.configuration) {
                RootComponent.MainConfig.Intro -> {
                    val childComponent = mainConfig.child?.instance as IntroComponent
                    IntroScreen(
                        component = childComponent
                    )
                }

                is RootComponent.MainConfig.Main -> {
                    val childComponent = mainConfig.child?.instance as MainComponent
                    MainScreen(
                        component = childComponent,
                        modifier = Modifier.fillMaxWidth()
                            .weight(1f)
                            .background(color = SelectedBackground, shape = RoundedCornerShape(4.dp))
                            .padding(Spacing.xs),
                        termbase = config.termbase,
                    )
                }
            }
        }
    }
}