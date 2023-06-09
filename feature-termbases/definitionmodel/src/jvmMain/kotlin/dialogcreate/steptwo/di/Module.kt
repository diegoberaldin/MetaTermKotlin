package dialogcreate.steptwo.di

import dialogcreate.steptwo.ui.CreateTermbaseWizardStepTwoComponent
import dialogcreate.steptwo.ui.DefaultCreateTermbaseWizardStepTwoComponent
import org.koin.dsl.module

val createTermbaseStepTwoModule = module {
    factory<CreateTermbaseWizardStepTwoComponent> {
        DefaultCreateTermbaseWizardStepTwoComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            propertyRepository = get(),
        )
    }
}