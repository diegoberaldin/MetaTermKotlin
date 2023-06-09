package dialogfilter.di

import dialogfilter.ui.DefaultTermFilterComponent
import org.koin.dsl.module
import kotlin.coroutines.coroutineContext

val dialogFilterModule = module {
    factory {
        DefaultTermFilterComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            propertyRepository = get(),
            languageRepository = get(),
            flagsRepository = get(),
            languageNameRepository = get(),
        )
    }
}