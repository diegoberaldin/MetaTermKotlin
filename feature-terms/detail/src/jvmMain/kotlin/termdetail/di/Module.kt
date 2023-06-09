package termdetail.di

import org.koin.dsl.module
import termdetail.ui.DefaultTermDetailComponent
import termdetail.ui.TermDetailComponent

val termDetailModule = module {
    factory<TermDetailComponent> {
        DefaultTermDetailComponent(
            componentContext = it[0],
            coroutineContext = it[1],
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
}