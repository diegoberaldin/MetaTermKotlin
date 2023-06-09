package common.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object DefaultNotificationCenter : NotificationCenter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _events = MutableSharedFlow<NotificationCenter.Event>()
    override val events = _events.asSharedFlow()

    override fun send(event: NotificationCenter.Event) {
        scope.launch {
            _events.emit(event)
        }
    }
}
