package intro.di

import intro.ui.DefaultIntroComponent
import intro.ui.IntroComponent
import org.koin.dsl.module

val introModule = module {
    factory<IntroComponent> {
        DefaultIntroComponent(
            componentContext = it[0],
            coroutineContext = it[1],
            dispatcherProvider = get(),
            termbaseRepository = get(),
            notificationCenter = get(),
        )
    }
}