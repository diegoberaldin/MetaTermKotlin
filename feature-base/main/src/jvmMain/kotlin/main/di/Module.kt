package main.di

import main.ui.MainComponent
import main.ui.DefaultMainComponent
import org.koin.dsl.module

val mainModule = module {
    factory<MainComponent> {
        DefaultMainComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            entryRepository = get(),
        )
    }
}