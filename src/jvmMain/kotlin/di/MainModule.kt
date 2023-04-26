package di

import AppViewModel
import org.koin.dsl.module
import ui.screens.intro.IntroViewModel
import ui.screens.main.MainViewModel

val mainKoinModule = module {
    factory {
        MainViewModel(
            dispatcherProvider = get(),
            entryRepository = get(),
        )
    }
    factory {
        IntroViewModel(
            dispatcherProvider = get(),
            termbaseRepository = get(),
            notificationCenter = get(),
        )
    }
    factory {
        AppViewModel(
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