package di

import org.koin.dsl.module
import ui.TermsViewModel
import ui.detail.TermDetailViewModel
import ui.dialog.filter.TermFilterViewModel


val termsKoinModule = module {
    factory {
        TermsViewModel(
            dispatcherProvider = get(),
            languageRepository = get(),
            languageNameRepository = get(),
            entryRepository = get(),
            deleteEntryUseCase = get(),
            searchTermsUseCase = get(),
            notificationCenter = get(),
            log = get(),
        )
    }
    factory {
        TermDetailViewModel(
            dispatcherProvider = get(),
            fileManager = get(),
            termRepository = get(),
            flagsRepository = get(),
            entryPropertyValueRepository = get(),
            languagePropertyValueRepository = get(),
            termPropertyValueRepository = get(),
            languageNameRepository = get(),
            notificationCenter = get(),
            deleteTermUseCase = get(),
            propertyRepository = get(),
            inputDescriptorRepository = get(),
            log = get(),
        )
    }
    factory {
        TermFilterViewModel(
            dispatcherProvider = get(),
            propertyRepository = get(),
            languageRepository = get(),
            flagsRepository = get(),
            languageNameRepository = get(),
        )
    }
}