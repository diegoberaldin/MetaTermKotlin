package dialogfilter.di

import dialogfilter.ui.DefaultTermFilterComponent
import dialogfilter.ui.TermFilterComponent
import org.koin.dsl.module

val dialogFilterModule = module {
    factory<TermFilterComponent> {
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