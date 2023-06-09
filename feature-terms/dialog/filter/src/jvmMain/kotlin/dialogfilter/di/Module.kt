package dialogfilter.di

import dialogfilter.ui.DefaultTermFilterComponent
import org.koin.dsl.module

val dialogFilterModule = module {
    factory {
        DefaultTermFilterComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            propertyRepository = get(),
            languageRepository = get(),
            getCompleteLanguage = get(),
            languageNameRepository = get(),
        )
    }
}