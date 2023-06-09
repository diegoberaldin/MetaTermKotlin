package usecase.di

import org.koin.dsl.module
import repo.*
import usecase.*

val usecaseModule = module {
    single {
        DeleteTermbaseLanguageUseCase(
            entryRepository = get(),
            languageRepository = get(),
            languagePropertyValueRepository = get(),
            termRepository = get(),
            termPropertyValueRepository = get(),
        )
    }
    single {
        DeleteTermUseCase(
            termRepository = get(),
            termPropertyValueRepository = get(),
        )
    }
    single {
        DeleteTermbaseUseCase(
            entryRepository = get(),
            entryPropertyValueRepository = get(),
            propertyRepository = get(),
            languageRepository = get(),
            languagePropertyValueRepository = get(),
            termRepository = get(),
            termPropertyValueRepository = get(),
            termbaseRepository = get(),
        )
    }
    single {
        DeleteEntryUseCase(
            entryRepository = get(),
            entryPropertyValueRepository = get(),
            propertyRepository = get(),
            languageRepository = get(),
            languagePropertyValueRepository = get(),
            termRepository = get(),
            termPropertyValueRepository = get(),
        )
    }
    single {
        ExportTbxUseCase(
            termRepository = get(),
            entryRepository = get(),
            propertyRepository = get(),
            termPropertyValueRepository = get(),
        )
    }
    single {
        ExportCsvUseCase(
            termRepository = get(),
            entryRepository = get(),
            languageRepository = get(),
        )
    }
    single {
        ImportCsvUseCase(
            termRepository = get(),
            entryRepository = get(),
            languageRepository = get(),
        )
    }
    single {
        ImportTbxUseCase(
            termRepository = get(),
            entryRepository = get(),
            languageRepository = get(),
            propertyRepository = get(),
            termPropertyValueRepository = get(),
        )
    }
    single {
        SearchTermsUseCase(termRepository = get())
    }
    single {
        GetCompleteLanguageUseCase(
            languageNameRepository = get(),
            flagsRepository = get(),
        )
    }
}
