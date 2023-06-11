package dialogsettings.di

import dialogsettings.ui.DefaultSettingsComponent
import dialogsettings.ui.SettingsComponent
import org.koin.dsl.module

val dialogSettingsModule = module {
    factory<SettingsComponent> {
        DefaultSettingsComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatchers = get(),
            completeLanguage = get(),
            keyStore = get(),
        )
    }
}