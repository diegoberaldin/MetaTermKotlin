package common.notification

import kotlinx.coroutines.flow.SharedFlow

interface NotificationCenter {

    sealed interface Event {
        data class OpenTermbase(val termbaseId: Int) : Event
        object CreateEntry : Event
        object CurrentLanguageTermsEdited : Event
        object CurrentLanguagesEdited : Event
        object OpenEntryEditMode : Event
        object CloseEntryEditMode : Event
        object DeleteEntry : Event
        object SaveEntry : Event
    }

    val events: SharedFlow<Event>

    fun send(event: Event)
}

