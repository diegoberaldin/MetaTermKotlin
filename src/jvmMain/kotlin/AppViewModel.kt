import coroutines.CoroutineDispatcherProvider
import data.TermbaseModel
import keystore.TemporaryKeyStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import log.LogManager
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import notification.NotificationCenter
import repository.EntryRepository
import repository.PropertyRepository
import repository.TermbaseRepository
import usecase.ExportCsvUseCase
import usecase.ExportTbxUseCase
import usecase.ImportCsvUseCase
import usecase.ImportTbxUseCase
import java.io.File
import java.lang.Integer.min

class AppViewModel(
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
) : ViewModel() {

    companion object {
        private const val LAST_OPENED_TERMBASE_ID_KEY = "lastOpenTermbaseId"
    }

    var shouldOpenNewTermbaseOnDialogClose: Boolean = false
    var termbaseToEdit: TermbaseModel? = null

    private val currentTermbase = MutableStateFlow<TermbaseModel?>(null)
    private val openedTermbases = MutableStateFlow<List<TermbaseModel>>(emptyList())

    val uiState = combine(
        currentTermbase,
        entryRepository.currentEntry,
        openedTermbases,
        entryRepository.editMode,
        propertyRepository.editMode,
    ) { currentTermbase, currentEntry, availableTermbases, entryEditMode, definitionModelEditMode ->
        AppUiState(
            currentTermbase = currentTermbase,
            currentEntry = currentEntry,
            openedTermbases = availableTermbases,
            entryEditMode = entryEditMode,
            definitionModelEditMode = definitionModelEditMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppUiState(),
    )

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            val lastOpenedTb = temporaryKeystore.get(LAST_OPENED_TERMBASE_ID_KEY, -1)
            if (lastOpenedTb >= 0) {
                openTermbase(lastOpenedTb)
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

    fun closeTermbase(termbase: TermbaseModel) {
        openedTermbases.getAndUpdate { termbases ->
            val oldIndex = termbases.indexOfFirst { it.id == termbase.id }
            val res = termbases.filter { it.id != termbase.id }
            if (currentTermbase.value?.id == termbase.id) {
                if (res.isNotEmpty()) {
                    val newIndex = min(oldIndex, res.size - 1)
                    setCurrentTermbase(res[newIndex])
                } else {
                    setCurrentTermbase(null)
                }
            }
            res
        }
    }

    fun setCurrentTermbase(termbase: TermbaseModel?) {
        currentTermbase.value = termbase
        viewModelScope.launch(dispatcherProvider.io) {
            temporaryKeystore.save(LAST_OPENED_TERMBASE_ID_KEY, termbase?.id ?: -1)
        }
    }

    fun exportTbx(path: String) {
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

    fun exportCsv(path: String) {
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

    fun importCsv(path: String) {
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

    fun importTbx(path: String) {
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
}
