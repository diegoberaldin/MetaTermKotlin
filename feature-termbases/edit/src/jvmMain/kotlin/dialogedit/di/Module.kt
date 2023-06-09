package dialogedit.di

import dialogedit.ui.DefaultEditTermbaseComponent
import dialogedit.ui.EditTermbaseComponent
import org.koin.dsl.module
import kotlin.coroutines.coroutineContext

val dialogEditModule = module {
    factory<EditTermbaseComponent> {
        DefaultEditTermbaseComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            termbaseRepository = get(),
            languageRepository = get(),
            propertyRepository = get(),
            deleteTermbaseLanguage = get(),
            inputDescriptorRepository = get(),
            notificationCenter = get(),
        )
    }
}