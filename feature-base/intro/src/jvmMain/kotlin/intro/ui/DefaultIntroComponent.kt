package intro.ui

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
import kotlin.coroutines.CoroutineContext

internal class DefaultIntroComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val termbaseRepository: TermbaseRepository,
    private val notificationCenter: NotificationCenter,
) : IntroComponent, ComponentContext by componentContext {

    private var termbases = MutableStateFlow<List<TermbaseModel>>(emptyList())
    private lateinit var viewModelScope: CoroutineScope

    override lateinit var uiState: StateFlow<IntroUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(termbases) {
                    IntroUiState(
                        termbases = it.first()
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = IntroUiState()
                )
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun load() {
        viewModelScope.launch(dispatcherProvider.io) {
            val values = termbaseRepository.getAll()
            termbases.value = values
        }
    }

    override fun openTermbase(termbase: TermbaseModel) {
        val termbaseId = termbase.id
        notificationCenter.send(NotificationCenter.Event.OpenTermbase(termbaseId))
    }
}

