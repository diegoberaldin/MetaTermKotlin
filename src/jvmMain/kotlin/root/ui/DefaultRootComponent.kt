package ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import data.TermbaseModel
import dialogcreate.ui.CreateTermbaseComponent
import dialogedit.ui.EditTermbaseComponent
import dialogmanage.ui.ManageTermbasesComponent
import dialogstatistics.ui.TermbaseStatisticsComponent
import intro.ui.IntroComponent
import common.keystore.TemporaryKeyStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import common.log.LogManager
import main.ui.MainComponent
import common.notification.NotificationCenter
import repo.EntryRepository
import repo.PropertyRepository
import repo.TermbaseRepository
import usecase.ExportCsvUseCase
import usecase.ExportTbxUseCase
import usecase.ImportCsvUseCase
import usecase.ImportTbxUseCase
import common.utils.getByInjection
import dialogsettings.ui.SettingsComponent
import java.io.File
import kotlin.coroutines.CoroutineContext

internal class DefaultRootComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val termbaseRepository: TermbaseRepository,
    entryRepository: EntryRepository,
    propertyRepository: PropertyRepository,
    private val exportTbxUseCase: ExportTbxUseCase,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val importCsvUseCase: ImportCsvUseCase,
    private val importTbxUseCase: ImportTbxUseCase,
    private val notificationCenter: NotificationCenter,
    private val temporaryKeystore: TemporaryKeyStore,
    private val log: LogManager,
) : RootComponent, ComponentContext by componentContext {

    companion object {
        private const val LAST_OPENED_TERMBASE_ID_KEY = "lastOpenTermbaseId"
    }

    override var shouldOpenNewTermbaseOnDialogClose: Boolean = false
    override var termbaseToEdit: TermbaseModel? = null

    private val currentTermbase = MutableStateFlow<TermbaseModel?>(null)
    private val openedTermbases = MutableStateFlow<List<TermbaseModel>>(emptyList())
    private lateinit var viewModelScope: CoroutineScope
    private val dialogNavigation = SlotNavigation<RootComponent.DialogConfig>()
    private val mainNavigation = SlotNavigation<RootComponent.MainConfig>()

    override lateinit var uiState: StateFlow<RootUiState>
    override val dialog: Value<ChildSlot<RootComponent.DialogConfig, *>> = childSlot(
        source = dialogNavigation,
        key = "RootDialogSlot",
        childFactory = { config, context ->
            when (config) {
                RootComponent.DialogConfig.NewTermbase -> getByInjection<CreateTermbaseComponent>(
                    context,
                    coroutineContext
                )

                RootComponent.DialogConfig.EditTermbase -> getByInjection<EditTermbaseComponent>(
                    context,
                    coroutineContext
                )

                RootComponent.DialogConfig.ManageTermbases -> getByInjection<ManageTermbasesComponent>(
                    context,
                    coroutineContext
                )

                RootComponent.DialogConfig.Statistics -> getByInjection<TermbaseStatisticsComponent>(
                    context,
                    coroutineContext
                )

                RootComponent.DialogConfig.Settings -> getByInjection<SettingsComponent>(
                    context,
                    coroutineContext
                )

                else -> Unit
            }
        }
    )
    override val main: Value<ChildSlot<RootComponent.MainConfig, *>> = childSlot(
        source = mainNavigation,
        key = "RootMainSlot",
        childFactory = { config, context ->
            when (config) {
                RootComponent.MainConfig.Intro -> getByInjection<IntroComponent>(context, coroutineContext)
                is RootComponent.MainConfig.Main -> getByInjection<MainComponent>(context, coroutineContext)

                else -> Unit
            }

        }
    )

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
                    currentTermbase,
                    entryRepository.currentEntry,
                    openedTermbases,
                    entryRepository.editMode,
                    propertyRepository.editMode,
                ) { currentTermbase, currentEntry, availableTermbases, entryEditMode, definitionModelEditMode ->
                    RootUiState(
                        currentTermbase = currentTermbase,
                        currentEntry = currentEntry,
                        openedTermbases = availableTermbases,
                        entryEditMode = entryEditMode,
                        definitionModelEditMode = definitionModelEditMode,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = RootUiState(),
                )

                viewModelScope.launch(dispatcherProvider.io) {
                    val lastOpenedTb = temporaryKeystore.get(LAST_OPENED_TERMBASE_ID_KEY, -1)
                    if (lastOpenedTb >= 0) {
                        openTermbase(lastOpenedTb)
                    } else {
                        setCurrentTermbase(null)
                    }

                    launch {
                        notificationCenter.events.filter { it is NotificationCenter.Event.OpenTermbase }.collect {
                            val termbaseId = (it as NotificationCenter.Event.OpenTermbase).termbaseId
                            temporaryKeystore.save(LAST_OPENED_TERMBASE_ID_KEY, termbaseId)
                            openTermbase(termbaseId)
                        }
                    }

                    launch {
                        termbaseRepository.all.collect { newTermbasesFromDb ->
                            openedTermbases.updateAndGet { oldList ->
                                val newList = mutableListOf<TermbaseModel>()
                                for (tb in oldList) {
                                    val newTb = newTermbasesFromDb.firstOrNull { e -> e.id == tb.id }
                                    newList += if (newTb != null && tb.name != newTb.name) {
                                        tb.copy(name = newTb.name)
                                    } else {
                                        tb
                                    }
                                }
                                newList
                            }
                        }
                    }
                }
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    private suspend fun openTermbase(termbaseId: Int) {
        val termbase = termbaseRepository.getById(termbaseId)
        if (termbase != null) {
            log.debug("Opening ${termbase.name}")
            openedTermbases.getAndUpdate { termbases ->
                if (termbases.isEmpty()) {
                    setCurrentTermbase(termbase)
                }
                if (termbases.none { e -> e.id == termbaseId }) {
                    termbases + termbase
                } else {
                    termbases
                }
            }
        }
    }

    override fun closeTermbase(termbase: TermbaseModel) {
        openedTermbases.getAndUpdate { termbases ->
            val oldIndex = termbases.indexOfFirst { it.id == termbase.id }
            val res = termbases.filter { it.id != termbase.id }
            if (currentTermbase.value?.id == termbase.id) {
                if (res.isNotEmpty()) {
                    val newIndex = Integer.min(oldIndex, res.size - 1)
                    setCurrentTermbase(res[newIndex])
                } else {
                    setCurrentTermbase(null)
                }
            }
            res
        }
    }

    override fun setCurrentTermbase(termbase: TermbaseModel?) {
        currentTermbase.value = termbase
        viewModelScope.launch(dispatcherProvider.io) {
            temporaryKeystore.save(LAST_OPENED_TERMBASE_ID_KEY, termbase?.id ?: -1)
        }
        viewModelScope.launch(dispatcherProvider.main) {
            if (termbase == null) {
                mainNavigation.activate(RootComponent.MainConfig.Intro)
            } else {
                mainNavigation.activate(RootComponent.MainConfig.Main(termbase))
            }
        }
    }

    override fun exportTbx(path: String) {
        val termbase = currentTermbase.value ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            runCatching {
                val destination = File(path)
                destination.createNewFile()

                exportTbxUseCase(
                    termbase = termbase,
                    destination = destination,
                )
            }
        }
    }

    override fun exportCsv(path: String) {
        val termbase = currentTermbase.value ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            runCatching {
                val destination = File(path)
                destination.createNewFile()

                exportCsvUseCase(
                    termbase = termbase,
                    destination = destination,
                )
            }
        }
    }

    override fun importCsv(path: String) {
        val termbase = currentTermbase.value ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                val source = File(path)

                importCsvUseCase(
                    termbase = termbase,
                    source = source,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun importTbx(path: String) {
        val termbase = currentTermbase.value ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                val source = File(path)

                importTbxUseCase(
                    termbase = termbase,
                    source = source,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun openDialog(config: RootComponent.DialogConfig) {
        viewModelScope.launch(dispatcherProvider.main) {
            dialogNavigation.activate(config)
        }
    }

    override fun closeDialog() {
        viewModelScope.launch(dispatcherProvider.main) {
            dialogNavigation.activate(RootComponent.DialogConfig.None)
        }
    }
}