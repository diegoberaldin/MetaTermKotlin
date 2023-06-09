package root.di

import intro.di.introModule
import main.di.mainModule
import org.koin.dsl.module
import ui.DefaultRootComponent
import ui.RootComponent

val rootModule = module {
    includes(
        introModule,
        mainModule,
    )

    factory<RootComponent> {
        DefaultRootComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            termbaseRepository = get(),
            entryRepository = get(),
            propertyRepository = get(),
            exportTbxUseCase = get(),
            exportCsvUseCase = get(),
            importCsvUseCase = get(),
            importTbxUseCase = get(),
            notificationCenter = get(),
            temporaryKeystore = get(),
            log = get(),
        )
    }
}