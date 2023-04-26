package ui.dialog.manage

import data.TermbaseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import notification.NotificationCenter
import repository.TermbaseRepository
import usecase.DeleteTermbaseUseCase
import coroutines.CoroutineDispatcherProvider

class ManageTermbasesViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val termbaseRepository: TermbaseRepository,
    private val deleteTermbaseUseCase: DeleteTermbaseUseCase,
    private val notificationCenter: NotificationCenter,
) : ViewModel() {

    private val termbases = MutableStateFlow<List<TermbaseModel>>(emptyList())
    private val selectedTermbase = MutableStateFlow<TermbaseModel?>(null)
    private val loading = MutableStateFlow(false)

    val uiState = combine(
        termbases,
        selectedTermbase,
        loading,
    ) { termbases, selectedTermbase, loading ->
        ManageTermbasesUiState(
            termbases = termbases,
            selectedTermbase = selectedTermbase,
            loading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ManageTermbasesUiState(),
    )

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            launch {
                termbaseRepository.all.collect {
                    termbases.value = it
                }
            }
        }
    }

    fun selectTermbase(termbase: TermbaseModel) {
        selectedTermbase.getAndUpdate {
            if (it == termbase) {
                null
            } else {
                termbase
            }
        }
    }

    fun openCurrentTermbase() {
        val termbase = selectedTermbase.value ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            notificationCenter.send(NotificationCenter.Event.OpenTermbase(termbase.id))
        }
    }

    fun deleteCurrentTermbase() {
        val termbase = selectedTermbase.value ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            deleteTermbaseUseCase(termbase)
        }
    }
}
