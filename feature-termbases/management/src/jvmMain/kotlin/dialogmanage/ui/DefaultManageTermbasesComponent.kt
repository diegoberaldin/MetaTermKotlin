package dialogmanage.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import data.TermbaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import common.notification.NotificationCenter
import repo.TermbaseRepository
import usecase.DeleteTermbaseUseCase
import kotlin.coroutines.CoroutineContext

class DefaultManageTermbasesComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val termbaseRepository: TermbaseRepository,
    private val deleteTermbaseUseCase: DeleteTermbaseUseCase,
    private val notificationCenter: NotificationCenter,
) : ManageTermbasesComponent, ComponentContext by componentContext {

    private val termbases = MutableStateFlow<List<TermbaseModel>>(emptyList())
    private val selectedTermbase = MutableStateFlow<TermbaseModel?>(null)
    private val loading = MutableStateFlow(false)
    private lateinit var viewModelScope: CoroutineScope

    override lateinit var uiState: StateFlow<ManageTermbasesUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
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
                viewModelScope.launch(dispatcherProvider.io) {
                    launch {
                        termbaseRepository.all.collect {
                            termbases.value = it
                        }
                    }
                }
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun selectTermbase(termbase: TermbaseModel) {
        selectedTermbase.getAndUpdate {
            if (it == termbase) {
                null
            } else {
                termbase
            }
        }
    }

    override fun openCurrentTermbase() {
        val termbase = selectedTermbase.value ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            notificationCenter.send(NotificationCenter.Event.OpenTermbase(termbase.id))
        }
    }

    override fun deleteCurrentTermbase() {
        val termbase = selectedTermbase.value ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            deleteTermbaseUseCase(termbase)
        }
    }
}
