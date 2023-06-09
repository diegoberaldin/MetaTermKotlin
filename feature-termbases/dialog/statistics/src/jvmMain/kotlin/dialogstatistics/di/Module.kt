package dialogstatistics.di

import dialogstatistics.ui.DefaultTermbaseStatisticsComponent
import dialogstatistics.ui.TermbaseStatisticsComponent
import org.koin.dsl.module

val dialogStatisticsModule = module {
    factory<TermbaseStatisticsComponent> {
        DefaultTermbaseStatisticsComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            entryRepository = get(),
            languageRepository = get(),
            languageNameRepository = get(),
            flagsRepository = get(),
            termRepository = get(),
        )
    }
}