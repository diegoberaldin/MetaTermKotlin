package dialogcreate.di

import dialogcreate.ui.CreateTermbaseComponent
import dialogcreate.ui.DefaultCreateTermbaseComponent
import org.koin.dsl.module
import kotlin.coroutines.coroutineContext

val dialogCreateModule = module {
    factory<CreateTermbaseComponent> {
        DefaultCreateTermbaseComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            termbaseRepository = get(),
            languageRepository = get(),
            propertyRepository = get(),
            inputDescriptorRepository = get(),
            notificationCenter = get(),
        )
    }
}