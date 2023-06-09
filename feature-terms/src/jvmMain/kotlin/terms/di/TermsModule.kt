package terms.di

import dialogfilter.di.dialogFilterModule
import org.koin.dsl.module
import termdetail.di.termDetailModule
import terms.ui.DefaultTermsComponent
import terms.ui.TermsComponent


val termsModule = module {
    includes(termDetailModule, dialogFilterModule)

    factory<TermsComponent> {
        DefaultTermsComponent(
            componentContext = it[0],
            coroutineContext = it[1],
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
}