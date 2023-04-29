package di

import org.koin.dsl.module

val repositoryKoinModule = module {
    includes(listOf(repoKoinModule, useCaseKoinModule))
}