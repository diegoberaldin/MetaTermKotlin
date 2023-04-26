package ui.screens.intro

import data.TermbaseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import notification.NotificationCenter
import repository.TermbaseRepository
import coroutines.CoroutineDispatcherProvider

class IntroViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val termbaseRepository: TermbaseRepository,
    private val notificationCenter: NotificationCenter,
) : ViewModel() {

    private var termbases = MutableStateFlow<List<TermbaseModel>>(emptyList())

    val uiState = combine(termbases) {
        IntroUiState(
            termbases = it.first()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IntroUiState()
    )

    fun reset() {
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