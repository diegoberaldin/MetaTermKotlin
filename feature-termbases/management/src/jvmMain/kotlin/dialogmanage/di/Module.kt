package dialogmanage.di

import dialogmanage.ui.DefaultManageTermbasesComponent
import dialogmanage.ui.ManageTermbasesComponent
import org.koin.dsl.module

val dialogManageModule = module {
    factory<ManageTermbasesComponent> {
        DefaultManageTermbasesComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            termbaseRepository = get(),
            deleteTermbaseUseCase = get(),
            notificationCenter = get(),
        )
    }
}