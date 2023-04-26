package di

import files.DefaultFileManager
import files.FileManager
import keystore.DefaultTemporaryKeyStore
import keystore.TemporaryKeyStore
import log.DefaultLogManager
import log.LogManager
import notification.DefaultNotificationCenter
import notification.NotificationCenter
import org.koin.dsl.module
import coroutines.CoroutineDispatcherProvider
import coroutines.CoroutineDispatcherProviderImpl

val commonKoinModule = module {
    single<CoroutineDispatcherProvider> {
        CoroutineDispatcherProviderImpl
    }
    single<NotificationCenter> {
        DefaultNotificationCenter
    }
    single<TemporaryKeyStore> {
        DefaultTemporaryKeyStore(fileManager = get())
    }
    single<FileManager> {
        DefaultFileManager
    }
    single<LogManager> {
        DefaultLogManager(fileManager = get())
    }
}
