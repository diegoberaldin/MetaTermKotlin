package coroutines

import kotlinx.coroutines.Dispatchers

internal object CoroutineDispatcherProviderImpl : CoroutineDispatcherProvider {
    override val main = Dispatchers.Main
    override val io = Dispatchers.IO
    override val default = Dispatchers.Default
    override val unconfined = Dispatchers.Unconfined
}