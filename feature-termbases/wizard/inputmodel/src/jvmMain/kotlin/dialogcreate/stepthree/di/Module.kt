package dialogcreate.stepthree.di

import dialogcreate.stepthree.ui.CreateTermbaseWizardStepThreeComponent
import dialogcreate.stepthree.ui.DefaultCreateTermbaseWizardStepThreeComponent
import org.koin.dsl.module

val createTermbaseStepThreeModule = module {
    factory<CreateTermbaseWizardStepThreeComponent> {
        DefaultCreateTermbaseWizardStepThreeComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            propertyRepository = get(),
            languageRepository = get(),
            getCompleteLanguage = get(),
            inputDescriptorRepository = get(),
        )
    }
}