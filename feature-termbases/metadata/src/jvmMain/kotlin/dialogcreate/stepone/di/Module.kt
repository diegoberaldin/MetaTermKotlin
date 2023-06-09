package dialogcreate.stepone.di

import dialogcreate.stepone.ui.CreateTermbaseWizardStepOneComponent
import dialogcreate.stepone.ui.DefaultCreateTermbaseWizardStepOneComponent
import org.koin.dsl.module

val createTermbaseStepOneModule = module {
    factory<CreateTermbaseWizardStepOneComponent> {
        DefaultCreateTermbaseWizardStepOneComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            languageRepository = get(),
            languageNameRepository = get(),
            flagsRepository = get(),
        )
    }
}