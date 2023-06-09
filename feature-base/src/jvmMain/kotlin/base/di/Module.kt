package base.di

import dialogsettings.di.dialogSettingsModule
import intro.di.introModule
import main.di.mainModule
import org.koin.dsl.module

val baseModule = module {
    includes(
        introModule,
        mainModule,
        dialogSettingsModule,
    )
}