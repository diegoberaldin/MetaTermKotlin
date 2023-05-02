package ui.dialog.manage

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import coroutines.CoroutineDispatcherProvider
import data.TermbaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import notification.NotificationCenter
import repository.TermbaseRepository
import usecase.DeleteTermbaseUseCase

class ManageTermbasesViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val termbaseRepository: TermbaseRepository,
    private val deleteTermbaseUseCase: DeleteTermbaseUseCase,
    private val notificationCenter: NotificationCenter,
) : InstanceKeeper.Instance {

    private val termbases = MutableStateFlow<List<TermbaseModel>>(emptyList())
    private val selectedTermbase = MutableStateFlow<TermbaseModel?>(null)
    private val loading = MutableStateFlow(false)
    private val viewModelScope = CoroutineScope(SupervisorJob())

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

    override fun onDestroy() {
        viewModelScope.cancel()
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
