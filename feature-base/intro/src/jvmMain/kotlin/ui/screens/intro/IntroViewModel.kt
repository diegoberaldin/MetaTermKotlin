package ui.screens.intro

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import data.TermbaseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import notification.NotificationCenter
import repository.TermbaseRepository
import coroutines.CoroutineDispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class IntroViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val termbaseRepository: TermbaseRepository,
    private val notificationCenter: NotificationCenter,
) : InstanceKeeper.Instance {

    private var termbases = MutableStateFlow<List<TermbaseModel>>(emptyList())
    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = combine(termbases) {
        IntroUiState(
            termbases = it.first()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IntroUiState()
    )

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun load() {
        viewModelScope.launch(dispatcherProvider.io) {
            val values = termbaseRepository.getAll()
            termbases.value = values
        }
    }

    fun openTermbase(termbase: TermbaseModel) {
        val termbaseId = termbase.id
        notificationCenter.send(NotificationCenter.Event.OpenTermbase(termbaseId))
    }
}

data class IntroUiState(
    val termbases: List<TermbaseModel> = emptyList(),
)