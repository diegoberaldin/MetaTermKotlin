package di

import org.koin.dsl.module
import ui.dialog.create.CreateTermbaseViewModel
import ui.dialog.create.stepone.CreateTermbaseWizardStepOneViewModel
import ui.dialog.create.stepthree.CreateTermbaseWizardStepThreeViewModel
import ui.dialog.create.steptwo.CreateTermbaseWizardStepTwoViewModel
import ui.dialog.edit.EditTermbaseViewModel
import ui.dialog.manage.ManageTermbasesViewModel
import ui.dialog.statistics.TermbaseStatisticsViewModel

val termbasesKoinModule = module {
    factory {
        CreateTermbaseViewModel(
            dispatcherProvider = get(),
            termbaseRepository = get(),
            languageRepository = get(),
            propertyRepository = get(),
            inputDescriptorRepository = get(),
            notificationCenter = get(),
        )
    }
    factory {
        CreateTermbaseWizardStepOneViewModel(
            dispatcherProvider = get(),
            languageRepository = get(),
            languageNameRepository = get(),
            flagsRepository = get(),
        )
    }
    factory {
        CreateTermbaseWizardStepTwoViewModel(
            dispatcherProvider = get(),
            propertyRepository = get(),
        )
    }
    factory {
        CreateTermbaseWizardStepThreeViewModel(
            dispatcherProvider = get(),
            propertyRepository = get(),
            languageRepository = get(),
            languageNameRepository = get(),
            flagsRepository = get(),
            inputDescriptorRepository = get(),
        )
    }
    factory {
        EditTermbaseViewModel(
            dispatcherProvider = get(),
            termbaseRepository = get(),
            languageRepository = get(),
            propertyRepository = get(),
            deleteTermbaseLanguage = get(),
            inputDescriptorRepository = get(),
            notificationCenter = get(),
        )
    }
    factory {
        ManageTermbasesViewModel(
            dispatcherProvider = get(),
            termbaseRepository = get(),
            deleteTermbaseUseCase = get(),
            notificationCenter = get(),
        )
    }
    factory {
        TermbaseStatisticsViewModel(
            dispatcherProvider = get(),
            entryRepository = get(),
            languageRepository = get(),
            languageNameRepository = get(),
            flagsRepository = get(),
            termRepository = get(),
        )
    }
}